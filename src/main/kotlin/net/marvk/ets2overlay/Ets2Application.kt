package net.marvk.ets2overlay

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle

class Ets2Application : Application() {
    override fun start(stage:  Stage) {
        val fxmlLoader = FXMLLoader(Ets2Application::class.java.getResource("main.fxml"))
        val scene = Scene(fxmlLoader.load())
        stage.scene = scene
        stage.x = 2800.0
        stage.y = 800.0
        stage.initStyle(StageStyle.TRANSPARENT)
        scene.fill = Color.TRANSPARENT
        stage.isAlwaysOnTop = true
        stage.show()
    }
}

fun main() {
    Application.launch(Ets2Application::class.java)
}
