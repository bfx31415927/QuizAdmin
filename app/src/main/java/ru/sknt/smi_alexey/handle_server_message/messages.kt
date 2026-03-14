package ru.sknt.smi_alexey.handle_server_message

import android.util.Log
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import ru.sknt.smi_alexey.websocket.MyWebSocket
import ru.smi_alexey.serialization.ClientResponse
import ru.smi_alexey.serialization.CommandMessage
import ru.smi_alexey.serialization.MessageWrapper
import ru.smi_alexey.serialization.ServerResponse
import ru.smi_alexey.serialization.StatusUpdate
import ru.smi_alexey.serialization.TextMessage
import ru.smi_alexey.serialization.WebSocketMessage
import ru.smi_alexey.serialization.json

// Обработка прямого экземпляра sealed-класса
fun handleWebSocketMessage(
    myWebSocket: MyWebSocket,
    message: WebSocketMessage
) {
    when (message) {
        is TextMessage -> {
            val mess = "Получено сообщение TextMessage: $message"
            Log.d("MyWebSocket", mess)
            sendDirectMessage(myWebSocket,ClientResponse(success = true,message = mess))
        }
        is CommandMessage -> {
            Log.d("MyWebSocket", "Получена команда: $message")
            sendDirectMessage(myWebSocket, processCommand(message))
        }
        is StatusUpdate -> {
            val mess = "Статус пользователя обновлен: ${message.status}"
            Log.d("MyWebSocket", mess)
            updateUserStatus(message.userId ?: "unknown", message.status)
            sendDirectMessage(myWebSocket,ClientResponse(success = true, message = mess))
        }
        is ServerResponse -> {
            Log.d("MyWebSocket", "Получено сообщение ServerResponse: $message")
        }
        is ClientResponse -> {
            val mess = "Ошибка! Получено сообщение ClientResponse: $message"
            Log.e("MyWebSocket", mess)
            sendDirectMessage(myWebSocket,ClientResponse(success = false,message = mess))
        }
    }
}


fun handleWrapperMessage(
    myWebSocket: MyWebSocket,
    wrapper: MessageWrapper
) {
    when (wrapper.wr_type) {
        "text" -> {
            val message = json.decodeFromJsonElement(TextMessage.serializer(),
                wrapper.data)
            val mess = "Получено сообщение TextMessage (в обёртке): $message"
            Log.d("MyWebSocket", mess)
            sendWrapperMessage(myWebSocket,ClientResponse(success = true,message = mess))
        }
        "command" -> {
            val command = json.decodeFromJsonElement(CommandMessage.serializer(),
                wrapper.data)
            Log.d("MyWebSocket", "Получена команда (в обёртке): $command")
            sendWrapperMessage(myWebSocket, processCommand(command))
        }
        "status" -> {
            val statusUpdate = json.decodeFromJsonElement(StatusUpdate.serializer(),
                wrapper.data)
            val mess = "Статус пользователя обновлен (в обёртке): $statusUpdate"
            Log.d("MyWebSocket", mess)
            updateUserStatus(statusUpdate.userId ?: "unknown", statusUpdate.status)
            sendWrapperMessage(myWebSocket,ClientResponse(success = true, message = mess))

        }
        "server_response" -> {
            val serverResponse = json.decodeFromJsonElement(ServerResponse.serializer(),
                wrapper.data)
            Log.d("MyWebSocket", "Получен ServerResponse (в обёртке): $serverResponse")
        }
        else -> {
            val mess = "Неизвестный/неверный тип сообщения в обёртке: ${wrapper.wr_type}"
            Log.e("MyWebSocket", mess)
            sendWrapperMessage(myWebSocket,ClientResponse(success = false, message = mess))
        }
    }
}

private fun processCommand(command: CommandMessage): ClientResponse {
    return when (command.command) {
        "start_game" -> ClientResponse(success = true, message = "Игра начата")
        "stop_game" -> ClientResponse(success = true, message = "Игра остановлена")
        else -> ClientResponse(success = false, message = "Неизвестная команда: ${command.command}")
    }
}

private fun updateUserStatus(userId: String, status: String) {
    Log.d("MyWebSocket", "У пользователя $userId теперь статус: '$status'")
}

inline fun <reified T : WebSocketMessage> sendWrapperMessage(myWebSocket: MyWebSocket, message: T) {
    try {
        val data = json.encodeToJsonElement(serializer<T>(), message).jsonObject
        val wrapper = MessageWrapper(
            wr_type = message._type,
            version = "1.0",
            data = data
        )
//        Log.d("MyWebSocket", "sendWrapperMessage готовит к отправке сообщение: $wrapper")
        val jsonString = json.encodeToString(wrapper)

        myWebSocket.webSocket.send(jsonString)

        Log.d("MyWebSocket", "sendWrapperMessage отправил сообщение: $jsonString")
    } catch (e: Exception) {
        Log.e("MyWebSocket", "Ошибка в sendWrapperMessage: ${e.message}")
    }
}

inline fun <reified T : WebSocketMessage> sendDirectMessage(myWebSocket: MyWebSocket, message: T) {
    try {
        val jsonString = json.encodeToString(WebSocketMessage.serializer(),
            message)

        myWebSocket.webSocket.send(jsonString)

        Log.d("MyWebSocket", "sendDirectMessage отправил сообщение: $jsonString")
    } catch (e: Exception) {
        Log.e("MyWebSocket", "Ошибка в sendDirectMessage: ${e.message}")
    }
}
