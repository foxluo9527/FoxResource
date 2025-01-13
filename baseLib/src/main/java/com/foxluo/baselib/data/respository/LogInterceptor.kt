import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.StandardCharsets

class LogInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        // 拿到请求参数
        val request = chain.request()
        // 拿到请求 body
        val body = request.body()
        var bodyString = ""
        var responseBodyString: String
        //对请求进行加密
        if (body != null) {
            val buffer = Buffer()
            body.writeTo(buffer)
            //将body 转成字符串,然后进行加密
            bodyString = buffer.readString(StandardCharsets.UTF_8)
        }
        //对响应进行解密
        val response = chain.proceed(request)
        run {
            val responseBody = response.body()
            val source = responseBody!!.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer
            responseBodyString = buffer.clone().readString(StandardCharsets.UTF_8)
        }
        printLog(request, bodyString, responseBodyString)
        return response
    }

    private fun printLog(request: Request, reqBody: String, respBody: String) {
        //判断是否是json格式
        if (!reqBody.startsWith("{")) {
            val log = String.format(
                """
                        请求成功：%s
                        RequestBody:%s
                        ResponseBody:%s
                        
                        """.trimIndent(),
                request.url(),
                reqBody,
                respBody
            )
            println(log)
            return
        }
        val log = java.lang.String.format(
            """
                        请求成功：%s
                        RequestBody:%s
                        ResponseBody:%s
                        
                        """.trimIndent(),
            request.url(),
            GsonUtils.toJson(reqBody),
            GsonUtils.toJson(respBody)
        )
        LogUtils.i(log)
    }
}