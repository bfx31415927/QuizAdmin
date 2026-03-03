package ru.sknt.smi_alexey.quizadmin

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // === Подключение к WebSocket через OkHttp ===
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("ws://188.243.20.65:16999/ws") // Адрес вашего WebSocket-сервера
            .build()

        val webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                runOnUiThread {
                    // Соединение установлено
                    Toast.makeText(this@MainActivity, "Соединение установлено", Toast.LENGTH_SHORT)
                        .show()
                }
                // Отправляем тестовое сообщение
                webSocket.send("Привет от Android-клиента!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                runOnUiThread {
                    // Получено текстовое сообщение от сервера
                    Toast.makeText(this@MainActivity, "Ответ от сервера: $text", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Соединение закрывается: $reason",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                runOnUiThread {
                    Log.d("onFailure", "Ошибка подключения: ${t.message}" )
                    Toast.makeText(
                        this@MainActivity,
                        "Ошибка подключения: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        })
    }
}