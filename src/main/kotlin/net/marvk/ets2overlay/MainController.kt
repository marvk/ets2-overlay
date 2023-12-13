package net.marvk.ets2overlay

import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import kotlin.system.exitProcess


private const val BUTTON_RADIUS = 10.0
private const val GROUP_TO_CENTER_OFFSET = 250.0
private const val GROUP_RADIUS = 35.0

private val BINDINGS = listOf(
    listOf(
        "Blinker links",
        "Blinker rechts",
        "Lichthupe",
        "Fanfare",
        "Hupe",
        "Motor anlassen/abstellen",
        "Aktivieren",
        "Herunterschalten",
        "Hochschalten",
    ),
    listOf(
        "Rundumleuchte",
        "Lichtmodi",
        "Fernlichtscheinwerfer",
        "Warnblinkanlage",
        "Scheibenwischer",
        "Tempomat",
        "Tempomat-Wiederaufnahme",
        "Tempomat-Geschwindigkeit verringern",
        "Tempomat-Geschwindigkeit erhöhen",
    ),
    listOf(
        "Overlay aktivieren",
        "Nächste ansicht",
        "Anzeigemodus Armaturenbrett",
        "Anzeigemodus des Infotainment",
        "Zoommodus Navigation",
        "Route Advisor: Navigation herauszoomen",
        "Route Advisor: Navigation hereinzoomen",
        "Route Advisor vorherige Seite",
        "Route Advisor nächste Seite",
    ),
    listOf(
        null,
        "Fotomodus aktivieren",
        "Weltkarte",
        null,
        "Menü",
        "Audio-Player Lautstärke erhöhen",
        "Audio-Player Lautstärke verringern",
        null,
        null,
    ),
    listOf(
        "Differentialsperre",
        "Feststellbremse",
        "Auflieger an-/abkuppeln",
        null,
        null,
        "Lkw-Achse heben/senken",
        "Aufliegerachse heben/senken",
        null,
        null,
    ),
)

class MainController {
    @FXML
    private lateinit var container: AnchorPane

    @FXML
    private lateinit var welcomeText: Label

    private val gamepadEventReader = GamepadEventReader(deviceName = "FANATEC Wheel")

    private val pressedButtons = FXCollections.observableArrayList<Int>()

    private val layerProperty = SimpleIntegerProperty(0)

    private val layerButtons = List(4) { ButtonGroup() }

    private val actionButtons = List(9) { ButtonGroup() }

    private val additionalInfoStage = AdditionalInfoStage()

    fun initialize() {
        createButtons()

        pressedButtons.addListener(ListChangeListener {
            val index = when (pressedButtons.lastOrNull()) {
                null -> 0
                12 -> 1
                8 -> 2
                9 -> 3
                10 -> 4
                else -> throw IllegalStateException()
            }

            layerProperty.value = index
        })

        Thread {
            listOf(8, 9, 10, 12).forEach { id ->
                gamepadEventReader.isButtonPressedProperty(id).addListener { _, _, newValue ->
                    Platform.runLater {
                        if (newValue) {
                            pressedButtons.add(id)
                        } else {
                            while (pressedButtons.contains(id)) {
                                pressedButtons.remove(id)
                            }
                        }
                    }
                }
            }

            gamepadEventReader.isButtonPressedProperty(25).addListener { _, _, pressed ->
                Platform.runLater {
                    if (pressed) {
                        additionalInfoStage.show()
                        (container.scene.window as? Stage)?.toFront()
                    } else {
                        additionalInfoStage.hide()
                    }
                }
            }

            gamepadEventReader.start()
        }.apply(Thread::start)
    }


    private fun createButtons() {
        val width = container.prefWidth
        val height = container.prefHeight

        val cx = (width / 2) - 100
        val cy = height / 2

//        add(cx - GROUP_TO_CENTER_OFFSET - GROUP_RADIUS, cy - GROUP_RADIUS, layerButtons[0].apply { textAlignment = TextAlignment.LEFT; })
//        add(cx - GROUP_TO_CENTER_OFFSET - GROUP_RADIUS, cy + GROUP_RADIUS, layerButtons[1].apply { textAlignment = TextAlignment.LEFT; })
//        add(cx - GROUP_TO_CENTER_OFFSET + GROUP_RADIUS, cy - GROUP_RADIUS, layerButtons[2].apply { textAlignment = TextAlignment.RIGHT; })
//        add(cx - GROUP_TO_CENTER_OFFSET + GROUP_RADIUS, cy + GROUP_RADIUS, layerButtons[3].apply { textAlignment = TextAlignment.RIGHT; })

        add(actionButtons[0].apply { textAlignment = TextAlignment.LEFT; text = "1" }, cx + GROUP_TO_CENTER_OFFSET - GROUP_RADIUS, cy + GROUP_RADIUS)
        add(actionButtons[1].apply { textAlignment = TextAlignment.RIGHT; text = "2" }, cx + GROUP_TO_CENTER_OFFSET + GROUP_RADIUS, cy + GROUP_RADIUS)
        add(actionButtons[2].apply { textAlignment = TextAlignment.RIGHT; text = "3" }, cx + GROUP_TO_CENTER_OFFSET + GROUP_RADIUS, cy - GROUP_RADIUS)
        add(actionButtons[3].apply { textAlignment = TextAlignment.LEFT; text = "4" }, cx + GROUP_TO_CENTER_OFFSET - GROUP_RADIUS, cy - GROUP_RADIUS)
        add(actionButtons[4].apply { textAlignment = TextAlignment.RIGHT; text = "4" }, cx + GROUP_TO_CENTER_OFFSET, cy)
        add(actionButtons[5].apply { textAlignment = TextAlignment.LEFT; text = "6" }, cx + GROUP_TO_CENTER_OFFSET - 3 * GROUP_RADIUS, cy)
        add(actionButtons[6].apply { textAlignment = TextAlignment.LEFT; text = "7" }, cx + GROUP_TO_CENTER_OFFSET - 3 * GROUP_RADIUS, cy + 2 * GROUP_RADIUS)
        add(actionButtons[7].apply { textAlignment = TextAlignment.LEFT; text = "8" }, cx - GROUP_TO_CENTER_OFFSET + 5 * GROUP_RADIUS, cy - 2 * GROUP_RADIUS)
        add(actionButtons[8].apply { textAlignment = TextAlignment.RIGHT; text = "9" }, cx + GROUP_TO_CENTER_OFFSET - 5 * GROUP_RADIUS, cy - 2 * GROUP_RADIUS)

        layerProperty.addListener { _, _, _ -> updateButtons() }
        updateButtons()
    }

    private fun updateButtons() {
        val layer = layerProperty.value

        val color = when (layer) {
            0 -> Color.web("e0e447")
            1 -> Color.web("a5e244")
            2 -> Color.web("56c7e3")
            3 -> Color.web("da752e")
            4 -> Color.web("d63fb2")
            else -> throw IllegalStateException()
        }

        BINDINGS[layer].forEachIndexed { index, label ->
            actionButtons[index].text = label
        }

        actionButtons.forEach { it.color = color }
        layerButtons.forEach { it.color = color }
    }

    private fun add(node: ButtonGroup, x: Double, y: Double) {
        fun setConstraints(alignment: TextAlignment) {
            AnchorPane.clearConstraints(node)

            if (alignment == TextAlignment.LEFT) {
                AnchorPane.setRightAnchor(node, container.prefWidth - x - BUTTON_RADIUS)
            } else {
                AnchorPane.setLeftAnchor(node, x - BUTTON_RADIUS)
            }

            AnchorPane.setTopAnchor(node, y - BUTTON_RADIUS)
        }

        node.textAlignmentProperty.addListener { _, _, newValue -> setConstraints(newValue) }
        setConstraints(node.textAlignmentProperty.value)
        container.children += node
    }

    @FXML
    fun exit(unused: ActionEvent) {
        Platform.exit()
        exitProcess(0)
    }
}

private class ButtonGroup : HBox() {
    val colorProperty = SimpleObjectProperty(Color.RED)
    var color
        get() = colorProperty.get()
        set(value) = colorProperty.set(value)
    val textProperty = SimpleStringProperty("")
    var text
        get() = textProperty.get()
        set(value) = textProperty.set(value)
    val textAlignmentProperty = SimpleObjectProperty(TextAlignment.RIGHT)
    var textAlignment
        get() = textAlignmentProperty.get()
        set(value) = textAlignmentProperty.set(value)

    init {
        val circle = Circle(BUTTON_RADIUS)
        circle.fillProperty().bind(colorProperty)
        val label = Label().apply {
            effect = DropShadow().apply {
                offsetX = 0.0
                offsetY = 0.0
                radius = 3.0
                spread = 0.5
            }
            isCache = true
            font = Font.font("Consolas", FontWeight.BLACK, FontPosture.REGULAR, 16.0)
            textFill = Color.WHITE
        }
        label.textProperty().bind(this.textProperty)
        label.textAlignmentProperty().bind(textAlignmentProperty)
        spacing = 5.0

        textAlignmentProperty.addListener { _, _, newValue ->
            children.clear()
            when (newValue) {
                TextAlignment.LEFT -> {
                    children.addAll(label, circle)
                    alignment = Pos.CENTER_RIGHT
                }

                TextAlignment.RIGHT -> {
                    children.addAll(circle, label)
                    alignment = Pos.CENTER_LEFT
                }

                else -> throw IllegalArgumentException(newValue.toString())
            }
        }
        textAlignmentProperty.set(TextAlignment.LEFT)
    }
}
