# Trace Metrics Script Guide
This folder contains `trace_metrics.py`, a helper script that reads Perfetto traces and prints simple performance metrics for your app.

## What this is for
Use it to compare **before vs after** performance when you change Compose code.

It helps answer: “Did this change make scrolling/swiping smoother?”

## One-time setup
Install the Perfetto Python package:

```bash path=null start=null
python3 -m pip install --user perfetto
```

## Basic usage
Run a baseline report from one trace:

```bash path=null start=null
python3 scripts/trace_metrics.py --before traces/your.trace
```

Compare two traces (before and after):

```bash path=null start=null
python3 scripts/trace_metrics.py --before traces/before.trace --after traces/after.trace
```

Ignore startup noise (recommended for interaction-only comparisons):

```bash path=null start=null
python3 scripts/trace_metrics.py --before traces/before.trace --after traces/after.trace --skip-startup-seconds 1.0
```

If you want JSON output for automation:

```bash path=null start=null
python3 scripts/trace_metrics.py --before traces/before.trace --after traces/after.trace --skip-startup-seconds 1.0 --output json
```

## How to read the output (quick)
- Lower numbers are usually better.
- Key metrics:
  - `frame_p95_ms`: should be around or below **16.67ms** (60fps budget).
  - `jank_rate_over_16_67_pct`: lower is better.
  - `severe_frames_over_33_33`: very slow frames; keep this as low as possible.
  - `on_measure_p95_ms`: high value means layout measuring is expensive.
  - `recompose_p95_ms`: high value means recomposition work is expensive.

The script also labels values as `[good]`, `[ok]`, or `[bad]`.

## Recommended test process (important)
For fair before/after comparison:
1. Use the **same device**.
2. Use the **same interaction flow** (example: open app -> swipe left to list -> swipe right).
3. Record traces of similar duration.
4. Close background apps if possible.
5. Run the same script command for both traces.

## Example with this project

```bash path=null start=null
python3 scripts/trace_metrics.py --before traces/cpu-perfetto-20260321T121222.trace
python3 scripts/trace_metrics.py --before traces/cpu-perfetto-20260321T121222.trace --after traces/cpu-perfetto-20260321T125152.trace
```

