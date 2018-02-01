package de.torbilicious.diewithme.mod

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableBooleanValue
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.util.Duration
import tornadofx.*


class ChatWindowView : View("DIE WITH ME Chat") {
    override val root = BorderPane()
    private val serverConnection: ServerConnection
    private var textFlow: TextFlow by singleAssign()
    private var scrollPane: ScrollPane by singleAssign()

    private var usernameField: TextField by singleAssign()
    private var batteryLevelField: TextField by singleAssign()

    private var chatArea: TextArea by singleAssign()

    private var usernameEmtpyProperty: ObservableBooleanValue by singleAssign()
    private var batteryLevelEmptyProperty: ObservableBooleanValue by singleAssign()

    init {
        serverConnection = ServerConnection(this::onMessage)

        with(root) {
            prefHeight = 600.0
            prefWidth = 500.0

            paddingAll = 10

            top {
                prefHeight = 50.0

                gridpane {
                    row {
                        label("Username: ")
                        usernameField = textfield("AwesomeGuy") {
                            usernameEmtpyProperty = textProperty().isEmpty
                        }
                    }

                    row {

                        label("Battery Level: ")
                        batteryLevelField = textfield("-1337") {
                            batteryLevelEmptyProperty = textProperty().isEmpty
                        }
                    }
                }
            }

            center {
                scrollPane = scrollpane {
                    vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED

                    textFlow = textflow {
                        lineSpacing = 10.0
                    }
                }
            }

            bottom {
                vbox {
                    chatArea = textarea()

                    button {
                        text = "Send"

                        disableWhen {
                            Bindings.or(
                                    usernameEmtpyProperty,
                                    batteryLevelEmptyProperty
                            )
                        }

                        action {
                            val message = chatArea.text
                            val username = usernameField.text
                            val batteryLevel = batteryLevelField.text

                            serverConnection.send(
                                    message,
                                    username,
                                    batteryLevel
                            )

                            onMessage("", username, message, batteryLevel)

                            chatArea.clear()
                            chatArea.requestFocus()
                        }
                    }
                }
            }
        }

        chatArea.requestFocus()
    }

    override fun onUndock() {
        exit()
    }

    private fun onMessage(uid: String, user: String, message: String, batteryLevel: String) {
        val text = "($batteryLevel%)$user: $message\n"
        print(text)

        appendText(text)
    }

    private fun appendText(text: String) {
        Platform.runLater {
            textFlow.children.add(Text(text))

            if (!scrollPane.isPressed) {
                Timeline(KeyFrame(Duration.seconds(0.5), KeyValue(scrollPane.vvalueProperty(), 1))).play()
            }
        }
    }

    private fun exit() {
        serverConnection.stop()
    }
}
