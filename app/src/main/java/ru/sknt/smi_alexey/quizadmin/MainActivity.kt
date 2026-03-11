package ru.sknt.smi_alexey.quizadmin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.sknt.smi_alexey.websocket.MyWebSocket
import ru.sknt.smi_alexey.websocket.myWebSocket

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Принудительно удерживаем экран включённым
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        myWebSocket = MyWebSocket(this)
    }
}