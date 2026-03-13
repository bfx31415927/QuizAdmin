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
import ru.smi_alexey.serialization.MessageType
import ru.smi_alexey.serialization.MessageWrapper
import ru.smi_alexey.serialization.ServerResponse
import ru.smi_alexey.serialization.StatusUpdate
import ru.smi_alexey.serialization.TextMessage
import ru.smi_alexey.serialization.WebSocketMessage
import ru.smi_alexey.serialization.analyzeMessageType
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
//            sendWrapperMessage(
//                TextMessage(
//                    content = "Привет от Android-клиента!",
//                    userId = "1"
//                )
//            )
//            sendWrapperMessage(
//                CommandMessage(
//                    command = "start_game",
//                    params = mapOf("round" to "1"),
//                    target = "all"
//                )
//            )
//            sendWrapperMessage(
//                StatusUpdate(
//                    status = "status",
//                    userId = "2",
//                )
//            )
//            sendDirectMessage(
//                TextMessage(
//                    content = "Привет от Android-клиента!",
//                    userId = "1"
//                )
//            )
//            sendDirectMessage(
//                CommandMessage(
//                    command = "start_game",
//                    params = mapOf("round" to "1"),
//                    target = "all"
//                )
//            )
            sendDirectMessage(
                StatusUpdate(
                    status = "status",
                    userId = "2",
                )
            )
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
//            activity.runOnUiThread {
//            }
            Log.d("MyWebSocket", "Получен JSON: $text")
            try {
                val messageType = analyzeMessageType(text)
                when (messageType) {
                    MessageType.DIRECT -> {
                        // Прямой экземпляр sealed-класса
                        val message = json.decodeFromString(
                            WebSocketMessage.serializer(),
                            text
                        )
                        handleWebSocketMessage(this, message)
                    }

                    MessageType.WRAPPED -> {
                        // Сообщение в обёртке
                        val wrapper = json.decodeFromString<MessageWrapper>(text)
                        handleWrapperMessage(this, wrapper)
                    }

                    MessageType.UNKNOWN -> {
                        val mess = "Получено сообщения неподдерживаемого формата: $text"
                        Log.d("MyWebSocket", mess)
                        sendWrapperMessage(
                            ServerResponse( success = false, message = mess)
                        )
                    }
                }
            } catch (e: Exception) {
                val mess = "Ошибка обработки сообщения: $text"
                Log.e("MyWebSocket", mess, e)
                sendWrapperMessage(
                    ServerResponse(success = false, message = mess))
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
}