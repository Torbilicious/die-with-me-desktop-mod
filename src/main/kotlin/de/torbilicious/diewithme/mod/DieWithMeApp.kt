package de.torbilicious.diewithme.mod

import javafx.application.Application
import javafx.stage.Stage
import tornadofx.App

class DieWithMeApp : App(ChatWindowView::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        stage.isResizable = false

        stage.width = 550.0
        stage.height = 600.0
    }
}

fun main(args: Array<String>) {
    Application.launch(DieWithMeApp::class.java, * args)
}
