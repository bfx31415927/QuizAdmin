package ru.sknt.smi_alexey.websocket

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.builtins.serializer
//import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import ru.smi_alexey.quizserver.app.serverHost
import ru.smi_alexey.quizserver.app.serverPort
import ru.smi_alexey.quizserver.app.vs_suffix
import ru.smi_alexey.serialization.CommandMessage
import ru.smi_alexey.serialization.MessageWrapper
import ru.smi_alexey.serialization.ServerResponse
import ru.smi_alexey.serialization.StatusUpdate
import ru.smi_alexey.serialization.TextMessage
import ru.smi_alexey.serialization.WebSocketMessage
import ru.smi_alexey.serialization.json
import java.util.concurrent.TimeUnit

fun send(text: String){
    myWebSocket?.webSocket?.send(text)
}

var myWebSocket: MyWebSocket? = null
class MyWebSocket(private val activity: AppCompatActivity) {
    val client = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS) // Ping каждые 15 с
        .build()

    val request = Request.Builder()
        .url("ws://" + serverHost + ':' + serverPort.toString() + '/' + vs_suffix)
        .build()
    val webSocket = client.newWebSocket(request, object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            activity.runOnUiThread {
                Log.d("MyWebSocket", "Соединение установлено")
            }
//            sendMessageWrapper(
//                TextMessage(
//                    content = "Привет от Android-клиента!",
//                    userId = "1"
//                )
//            )
//            sendMessageWrapper(
//                CommandMessage(
//                    command = "start_game",
//                    params = mapOf("round" to "1"),
//                    target = "all"
//                )
//            )
            sendMessageWrapper(
                StatusUpdate(
                    status = "status",
                    userId = "2",
                )
            )
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            activity.runOnUiThread {
                val response = json.decodeFromString<ServerResponse>(text)
                Log.d("MyWebSocket", "Ответ от сервера: response.success: ${response.success}" +
                    " response.message: ${response.message}")
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
            activity.runOnUiThread {
                Log.d("MyWebSocket", "Соединение закрывается: $reason" )
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            activity.runOnUiThread {
                Log.d("MyWebSocket", "Ошибка подключения: ${t.message}" )
            }
        }

    })

    inline fun <reified T : WebSocketMessage> sendMessageWrapper(message: T) {
        try {
            val data = json.encodeToJsonElement(serializer<T>(), message).jsonObject
            val wrapper = MessageWrapper(
                wr_type = message.type,
                version = "1.0",
                data = data
            )
            Log.d("MyWebSocket", "sendMessageWrapper готовит к отправке сообщение: $wrapper")
            val jsonString = json.encodeToString(wrapper)
            webSocket.send(jsonString)
            Log.d("MyWebSocket", "sendMessageWrapper отправил сообщение: $jsonString")
        } catch (e: Exception) {
            Log.e("MyWebSocket", "Ошибка в sendMessageWrapper: ${e.message}")
        }
    }
}