package de.torbilicious.diewithme.mod

import io.socket.client.IO
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.scene.shape.Circle
import javafx.stage.Stage
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class DieWithMeApp : Application() {
    override fun start(primaryStage: Stage?) {
        val circ = Circle(40.0, 40.0, 30.0)
        val root = Group(circ)
        val scene = Scene(root, 400.0, 300.0)

        primaryStage?.title = "My JavaFX Application"
        primaryStage?.scene = scene
        primaryStage?.show()
    }

    private val socket = IO.socket("https://api.diewithme.online")
    private val uid = UUID.randomUUID().toString()

    init {
        println("Starting up...")
        println()

        socket.open()

        setupListeners()

        Thread.sleep(2500)

//        exit()

        idle()
    }

    private fun setupListeners() {
        socket.on("set_messages") {
            println("set_messages:")

            println(it)
        }

        socket.on("set_message") {
            //            println("set_message:")

            val event = ((it.first() as JSONArray).get(0) as JSONObject)

            val uid = event["uid"] as String
            val user = event["user"] as String
            val message = event["message"] as String
            val batteryLevel = event["bl"] as String

            print("($batteryLevel%)$user: $message\n")
        }
    }

    private fun send(message: String, user: String = "AwesomeGuy", batteryLevel: String) {
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

    private fun getInput(): Message? {
        val dialog = Dialog<Message>()

        dialog.title = "Message"
        dialog.contentText = "Please enter a message"

        val messageBox = TextArea("Message")
        val userNameBox = TextField("AwesomeGuy")
        val batteryLevelBox = TextField("1337")

        val dialogPane = dialog.dialogPane
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        dialogPane.content = VBox(8.0, messageBox, userNameBox, batteryLevelBox)

        dialog.setResultConverter {
            if (it == ButtonType.OK) {
                return@setResultConverter Message(
                        messageBox.text,
                        userNameBox.text,
                        batteryLevelBox.text
                )
            } else {
                return@setResultConverter null
            }
        }

        val result = dialog.showAndWait()

        return if (result.isPresent) {
            result.get()
        } else {
            null
        }
    }


    private fun getTime(): String = java.lang.Long.valueOf(System.currentTimeMillis() / 1000).toString()


    private fun idle() {
        while (true) {
            Thread.sleep(250)

            val message = getInput()
            message?.let { send(message.message, message.user, message.batteryLevel) }
        }
    }

    private fun exit() {
        socket.disconnect()

        System.exit(0)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
//            DieWithMeApp()
            Application.launch(DieWithMeApp::class.java, *args)
        }

    }
}

data class Message(val message: String,
                   val user: String,
                   val batteryLevel: String)


