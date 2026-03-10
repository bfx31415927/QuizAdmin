04.03.2026
----------
1) Добавил пинги через 15 сек на сервер
   2) Сделал, чтобы экран активности никогда не гас
       (иначе разрывается соединение с сервером):
       2.1. Прописал строку в activity_main.xml
       2.2. Можно также в коде активности, 
            см. закомментир. строку:
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
10.03.2026
----------
1) Добавил файл serialization.kt (аналогичен файлу на сервере)
2) Добавил библиотеку json-сериализации (libs.versions.toml, build.gradle.kts (модуля))
3) Добавил на всякий случай библиотеку logging-interceptor(libs.versions.toml, build.gradle.kts (модуля))
			Зачем:
			Это библиотека, которая позволяет логировать все HTTP/WebSocket-запросы и ответы в Logcat.
			Пример вывода:
						--> SEND: {"type":"text","content":"Привет"}
						--> RCV: {"type":"pong"}


