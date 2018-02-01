package de.torbilicious.diewithme.mod

import io.socket.client.IO
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class ServerConnection(
        private val onMessageCallback: (
                uid: String,
                user: String,
                message: String,
                batteryLevel: String
        ) -> Unit
) {
    private val socket = IO.socket("https://api.diewithme.online")
    private val uid = UUID.randomUUID().toString()

    init {
        socket.connect()

        socket.on("set_messages") {
            println("set_messages:")

            println(it)
        }

        socket.on("set_message") {
            try {
                val event = ((it.first() as JSONArray).get(0) as JSONObject)

                val uid = event["uid"] as String
                val user = event["user"] as String
                val message = event["message"] as String

                val inBl = event["bl"]

                val batteryLevel: String = when (inBl) {
                    is String -> inBl
                    is Int -> inBl.toString()

                    else -> ""
                }

                onMessageCallback(uid, user, message, batteryLevel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        socket.disconnect()
    }

    fun send(message: String, user: String, batteryLevel: String) {
        val jsonObject = JSONObject()

        with(jsonObject) {
            put("message", message)
            put("user", user)
            put("time", getTime())
            put("chargin", false)
            put("bl", batteryLevel)
            put("uid", uid)
        }

        socket.emit("new_message", jsonObject)
    }

    private fun getTime(): String = java.lang.Long.valueOf(System.currentTimeMillis() / 1000).toString()

}


