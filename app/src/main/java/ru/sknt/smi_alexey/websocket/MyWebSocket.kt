package ru.sknt.smi_alexey.websocket

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import ru.smi_alexey.quizserver.app.serverHost
import ru.smi_alexey.quizserver.app.serverPort
import ru.smi_alexey.quizserver.app.vs_suffix
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
                // Соединение установлено
                Toast.makeText(activity, "Соединение установлено", Toast.LENGTH_SHORT)
                    .show()
            }
            // Отправляем тестовое сообщение
            webSocket.send("Привет от Android-клиента!")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            activity.runOnUiThread {
                // Получено текстовое сообщение от сервера
                Toast.makeText(activity, "Ответ от сервера: $text", Toast.LENGTH_LONG)
                    .show()
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
            Log.d("onTest", "Соединение закрывается: $reason" )
            activity.runOnUiThread {
                Toast.makeText(
                    activity,
                    "Соединение закрывается: $reason",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            activity.runOnUiThread {
                Log.d("onTest", "Ошибка подключения: ${t.message}" )
                Toast.makeText(
                    activity,
                    "Ошибка подключения: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    })

}