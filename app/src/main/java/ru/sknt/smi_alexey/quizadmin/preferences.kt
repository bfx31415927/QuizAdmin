package ru.smi_alexey.quizserver.app

import android.content.Context
import java.util.UUID

val serverHost = "188.243.20.65" //хост сервера
val serverPort = 16999 //порт прослушивания подключений
val vs_suffix = "ws"

fun getOrGenerateDeviceId(context: Context): String {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var deviceId = prefs.getString("device_id", null)

    if (deviceId == null) {
        deviceId = UUID.randomUUID().toString()
        prefs.edit().putString("device_id", deviceId).apply()
    }
    return deviceId
}
