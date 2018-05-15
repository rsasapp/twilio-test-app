import com.twilio.http.HttpMethod
import com.twilio.twiml.VoiceResponse
import com.twilio.twiml.voice.Gather
import com.twilio.twiml.voice.Say
import spark.Spark
import java.net.URLDecoder

class VoiceApp {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Spark.get("/hello", { r, s ->
                System.out.println("Received request $r, $s")
                return@get "Hello"
            })

            Spark.post("/", { request, response ->
                val say: Say = Say.Builder("Hi there! How can we help?").build()
                val gather: Gather = Gather.Builder()
                        .inputs(Gather.Input.SPEECH)
                        .say(say)
                        .action("/speech")
                        .partialResultCallback("/speechPartial")
                        .partialResultCallbackMethod(HttpMethod.POST)
                        .method(HttpMethod.POST)
                        .timeout(3)
                        .build()
                val voiceResponse = VoiceResponse.Builder().gather(gather).build()
                System.out.println("Received request $request, $response")
                return@post voiceResponse.toXml()
            })

            Spark.post("/speech", { request, response ->
                System.out.println("Complete speech")
                printSpeechToLogs(request.body())
                val say: Say = Say.Builder("Thank you. Have a nice day").build()
                return@post VoiceResponse.Builder().say(say).build().toXml()
            })

            Spark.post("/speechPartial", { request, response ->
                System.out.println("Partial speech")
                printSpeechToLogs(request.body())
            })
        }

        fun printSpeechToLogs(body: String) {
            val params = body.split("&")
            params.forEach { param ->
                val index = param.indexOf("=")
                val pair = Pair(URLDecoder.decode(param.substring(0, index), "UTF-8"),
                        URLDecoder.decode(param.substring(index + 1, param.length), "UTF-8"))
                if (pair.first.contains("Speech", true)) {
                    System.out.println("Key: ${pair.first}, Value: ${pair.second}")
                }
            }
        }
    }
}