#!/usr/bin/env python3
import argparse
import json
import math
import statistics
from dataclasses import dataclass
from typing import Dict, List, Optional

from perfetto.trace_processor import TraceProcessor


@dataclass
class Summary:
    trace_path: str
    process: str
    frame_count: int
    frame_avg_ms: Optional[float]
    frame_p50_ms: Optional[float]
    frame_p90_ms: Optional[float]
    frame_p95_ms: Optional[float]
    frame_p99_ms: Optional[float]
    frame_max_ms: Optional[float]
    jank_frames_over_16_67: int
    jank_rate_over_16_67_pct: Optional[float]
    severe_frames_over_33_33: int
    severe_rate_over_33_33_pct: Optional[float]
    traversal_avg_ms: Optional[float]
    traversal_p95_ms: Optional[float]
    on_measure_avg_ms: Optional[float]
    on_measure_p95_ms: Optional[float]
    recompose_avg_ms: Optional[float]
    recompose_p95_ms: Optional[float]


def percentile(sorted_values: List[float], p: float) -> Optional[float]:
    if not sorted_values:
        return None
    if p <= 0:
        return sorted_values[0]
    if p >= 100:
        return sorted_values[-1]
    k = (len(sorted_values) - 1) * (p / 100.0)
    lo = math.floor(k)
    hi = math.ceil(k)
    if lo == hi:
        return sorted_values[lo]
    weight = k - lo
    return sorted_values[lo] * (1.0 - weight) + sorted_values[hi] * weight


def round_or_none(value: Optional[float], digits: int = 3) -> Optional[float]:
    if value is None:
        return None
    return round(value, digits)

def sql_quote(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def query_durations_ms(tp: TraceProcessor, process: str, slice_name: str) -> List[float]:
    q = f"""
    select s.dur / 1e6 as dur_ms
    from slice s
    join thread_track tt on s.track_id = tt.id
    join thread th on tt.utid = th.utid
    join process p on th.upid = p.upid
    where p.name = {sql_quote(process)}
      and s.name = {sql_quote(slice_name)}
      and s.dur > 0
    order by s.ts;
    """
    return [float(r.dur_ms) for r in tp.query(q)]


def query_frame_durations_ms(tp: TraceProcessor, process: str) -> List[float]:
    q = f"""
    select s.dur / 1e6 as dur_ms
    from slice s
    join thread_track tt on s.track_id = tt.id
    join thread th on tt.utid = th.utid
    join process p on th.upid = p.upid
    where p.name = {sql_quote(process)}
      and s.name glob 'Choreographer#doFrame*'
      and s.dur > 0
    order by s.ts;
    """
    return [float(r.dur_ms) for r in tp.query(q)]


def build_summary(trace_path: str, process: str) -> Summary:
    tp = TraceProcessor(trace=trace_path)
    frames = query_frame_durations_ms(tp, process)
    traversal = query_durations_ms(tp, process, "traversal")
    on_measure = query_durations_ms(tp, process, "AndroidOwner:onMeasure")
    recompose = query_durations_ms(tp, process, "Recomposer:recompose")

    frames_sorted = sorted(frames)
    traversal_sorted = sorted(traversal)
    on_measure_sorted = sorted(on_measure)
    recompose_sorted = sorted(recompose)

    frame_count = len(frames)
    jank = sum(1 for x in frames if x > 16.67)
    severe = sum(1 for x in frames if x > 33.33)

    return Summary(
        trace_path=trace_path,
        process=process,
        frame_count=frame_count,
        frame_avg_ms=round_or_none(statistics.mean(frames) if frames else None),
        frame_p50_ms=round_or_none(percentile(frames_sorted, 50)),
        frame_p90_ms=round_or_none(percentile(frames_sorted, 90)),
        frame_p95_ms=round_or_none(percentile(frames_sorted, 95)),
        frame_p99_ms=round_or_none(percentile(frames_sorted, 99)),
        frame_max_ms=round_or_none(frames_sorted[-1] if frames_sorted else None),
        jank_frames_over_16_67=jank,
        jank_rate_over_16_67_pct=round_or_none((jank / frame_count * 100.0) if frame_count else None),
        severe_frames_over_33_33=severe,
        severe_rate_over_33_33_pct=round_or_none((severe / frame_count * 100.0) if frame_count else None),
        traversal_avg_ms=round_or_none(statistics.mean(traversal) if traversal else None),
        traversal_p95_ms=round_or_none(percentile(traversal_sorted, 95)),
        on_measure_avg_ms=round_or_none(statistics.mean(on_measure) if on_measure else None),
        on_measure_p95_ms=round_or_none(percentile(on_measure_sorted, 95)),
        recompose_avg_ms=round_or_none(statistics.mean(recompose) if recompose else None),
        recompose_p95_ms=round_or_none(percentile(recompose_sorted, 95)),
    )


def compare(before: Dict, after: Dict) -> Dict:
    lower_is_better_keys = [
        "frame_avg_ms",
        "frame_p95_ms",
        "frame_p99_ms",
        "frame_max_ms",
        "jank_frames_over_16_67",
        "jank_rate_over_16_67_pct",
        "severe_frames_over_33_33",
        "severe_rate_over_33_33_pct",
        "traversal_avg_ms",
        "traversal_p95_ms",
        "on_measure_avg_ms",
        "on_measure_p95_ms",
        "recompose_avg_ms",
        "recompose_p95_ms",
    ]
    out = {}
    for key in lower_is_better_keys:
        b = before.get(key)
        a = after.get(key)
        if b is None or a is None:
            out[key] = {"before": b, "after": a, "delta": None, "change_pct": None, "verdict": "unknown"}
            continue
        delta = round(a - b, 3)
        change_pct = round(((a - b) / b * 100.0), 2) if b != 0 else None
        if delta < 0:
            verdict = "improved"
        elif delta > 0:
            verdict = "worse"
        else:
            verdict = "unchanged"
        out[key] = {"before": b, "after": a, "delta": delta, "change_pct": change_pct, "verdict": verdict}
    return out
def grade_lower_is_better(value: Optional[float], good_max: float, ok_max: float) -> str:
    if value is None:
        return "unknown"
    if value <= good_max:
        return "good"
    if value <= ok_max:
        return "ok"
    return "bad"


def pct_change(before: Optional[float], after: Optional[float]) -> Optional[float]:
    if before is None or after is None or before == 0:
        return None
    return round(((after - before) / before) * 100.0, 2)


def fmt_num(value: Optional[float], suffix: str = "") -> str:
    if value is None:
        return "n/a"
    return f"{value}{suffix}"


def print_metric_guide() -> None:
    print("Metric guide (plain English):")
    print("- frame_p95_ms: 95% of frames are faster than this. Aim: <= 16.67ms (60fps budget).")
    print("- frame_p99_ms: worst 1% frame behavior. Useful for stutter spikes.")
    print("- jank_frames_over_16_67: number of frames that missed the 60fps budget.")
    print("- severe_frames_over_33_33: very slow frames (visible stutter).")
    print("- on_measure_*: time spent measuring UI size; high values can mean expensive layout.")
    print("- recompose_*: time Compose spends recalculating UI state.")
    print("- traversal_*: layout + draw pipeline time on the main thread.")


def print_human_summary(summary: Dict) -> None:
    print("=== Trace Summary ===")
    print(f"Trace: {summary['trace_path']}")
    print(f"Process: {summary['process']}")
    print(f"Frames analyzed: {summary['frame_count']}")
    print("")
    print("Smoothness health (lower is better):")
    p95_grade = grade_lower_is_better(summary.get("frame_p95_ms"), 16.67, 24.0)
    jank_rate_grade = grade_lower_is_better(summary.get("jank_rate_over_16_67_pct"), 1.0, 3.0)
    severe_grade = grade_lower_is_better(summary.get("severe_rate_over_33_33_pct"), 0.2, 1.0)
    print(
        f"- frame_p95_ms: {fmt_num(summary.get('frame_p95_ms'), 'ms')} "
        f"(target <=16.67ms) [{p95_grade}]"
    )
    print(
        f"- frame_p99_ms: {fmt_num(summary.get('frame_p99_ms'), 'ms')} "
        f"(spike detector) [{grade_lower_is_better(summary.get('frame_p99_ms'), 24.0, 40.0)}]"
    )
    print(
        f"- frame_max_ms: {fmt_num(summary.get('frame_max_ms'), 'ms')} "
        f"(worst single frame) [{grade_lower_is_better(summary.get('frame_max_ms'), 33.33, 60.0)}]"
    )
    print(
        f"- jank_frames_over_16_67: {summary.get('jank_frames_over_16_67')} "
        f"({fmt_num(summary.get('jank_rate_over_16_67_pct'), '%')}) [{jank_rate_grade}]"
    )
    print(
        f"- severe_frames_over_33_33: {summary.get('severe_frames_over_33_33')} "
        f"({fmt_num(summary.get('severe_rate_over_33_33_pct'), '%')}) [{severe_grade}]"
    )
    print("")
    print("Where time is spent:")
    print(
        f"- on_measure_p95_ms: {fmt_num(summary.get('on_measure_p95_ms'), 'ms')} "
        f"[{grade_lower_is_better(summary.get('on_measure_p95_ms'), 8.0, 16.0)}]"
    )
    print(
        f"- recompose_p95_ms: {fmt_num(summary.get('recompose_p95_ms'), 'ms')} "
        f"[{grade_lower_is_better(summary.get('recompose_p95_ms'), 4.0, 8.0)}]"
    )
    print(
        f"- traversal_p95_ms: {fmt_num(summary.get('traversal_p95_ms'), 'ms')} "
        f"[{grade_lower_is_better(summary.get('traversal_p95_ms'), 8.0, 16.0)}]"
    )
    print("")
    print_metric_guide()


def print_human_comparison(before: Dict, after: Dict) -> None:
    print("=== Before vs After (lower is better) ===")
    keys = [
        "frame_p95_ms",
        "frame_p99_ms",
        "frame_max_ms",
        "jank_frames_over_16_67",
        "jank_rate_over_16_67_pct",
        "severe_frames_over_33_33",
        "severe_rate_over_33_33_pct",
        "on_measure_p95_ms",
        "recompose_p95_ms",
        "traversal_p95_ms",
    ]
    for key in keys:
        b = before.get(key)
        a = after.get(key)
        delta = None if b is None or a is None else round(a - b, 3)
        change = pct_change(b, a) if isinstance(b, (int, float)) and isinstance(a, (int, float)) else None
        if delta is None:
            verdict = "unknown"
        elif delta < 0:
            verdict = "improved"
        elif delta > 0:
            verdict = "worse"
        else:
            verdict = "unchanged"
        change_txt = "n/a" if change is None else f"{change}%"
        print(f"- {key}: {b} -> {a} | delta={delta} | change={change_txt} | {verdict}")

    print("")
    print("How to read this:")
    print("- For almost all metrics here, smaller numbers are better.")
    print("- If frame_p95_ms and jank_rate_over_16_67_pct go down, your UX is usually smoother.")
    print("- Compare traces using the exact same user flow and similar device conditions.")


def main() -> None:
    parser = argparse.ArgumentParser(description="Compute stable Compose/Perfetto performance metrics from trace(s).")
    parser.add_argument("--before", required=True, help="Path to baseline (before) trace.")
    parser.add_argument("--after", help="Path to comparison (after) trace.")
    parser.add_argument("--process", default="com.raibbl.ayabelquran", help="Android process name to analyze.")
    parser.add_argument("--output", choices=["human", "json"], default="human", help="Output format.")
    args = parser.parse_args()

    before_summary = build_summary(args.before, args.process)
    result = {"before": before_summary.__dict__}

    if args.after:
        after_summary = build_summary(args.after, args.process)
        result["after"] = after_summary.__dict__
        result["comparison"] = compare(result["before"], result["after"])
    if args.output == "json":
        print(json.dumps(result, indent=2, sort_keys=True))
        return

    print_human_summary(result["before"])
    if args.after:
        print("")
        print_human_comparison(result["before"], result["after"])


if __name__ == "__main__":
    main()
