package api

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.Request
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

/**
 * StringRequest always decodes response bytes as UTF-8, regardless of what
 * charset the server's Content-Type header claims. The alquran.cloud API
 * returns Arabic text as UTF-8 but sometimes omits/mislabels the charset,
 * which makes Volley's default (ISO-8859-1) fallback mangle the Arabic text
 * into garbled characters.
 */
class Utf8StringRequest(
    method: Int,
    url: String,
    listener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) : StringRequest(method, url, listener, errorListener) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
        val parsed = try {
            String(response.data, Charset.forName("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            String(response.data)
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response))
    }
}
