package net.marvk.ets2overlay

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle

class AdditionalInfoStage : Stage(StageStyle.TRANSPARENT) {
    init {
        val fxmlLoader = FXMLLoader(AdditionalInfoStage::class.java.getResource("additional-info.fxml"))
        scene = Scene(fxmlLoader.load())
        scene.fill = Color.TRANSPARENT
        isAlwaysOnTop = true
    }
}
