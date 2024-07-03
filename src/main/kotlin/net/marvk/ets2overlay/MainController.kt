package net.marvk.ets2overlay

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
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
        "Nächste Ansicht",
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

private val LAYER_BUTTON_IDS = listOf(8, 9, 10, 12)
private val ACTION_BUTTON_IDS = listOf(1, 2, 3, 4, 5, 6, 7, 11, 27)

class MainController {
    @FXML
    private lateinit var container: AnchorPane

    @FXML
    private lateinit var welcomeText: Label

    private val gamepadEventReader = GamepadEventReader(deviceName = "FANATEC Wheel")

    private val pressedLayerButtons: ObservableList<Int> = FXCollections.observableArrayList()

    private val pressedActionButtons: ObservableList<Int> = FXCollections.observableArrayList()

    private val layerProperty = SimpleIntegerProperty(0)

    private val layerButtons = List(4) { ButtonGroup() }

    private val actionButtons = List(9) { ButtonGroup() }

    private val additionalInfoStage = AdditionalInfoStage()

    fun initialize() {
        createButtons()

        pressedLayerButtons.addListener(ListChangeListener {
            val index = when (pressedLayerButtons.lastOrNull()) {
                null -> 0
                12 -> 1
                8 -> 2
                9 -> 3
                10 -> 4
                else -> throw IllegalStateException()
            }

            layerProperty.value = index
        })

        pressedActionButtons.addListener(ListChangeListener {

            actionButtons.forEach {
                it.state = when {
                    pressedActionButtons.isEmpty() -> ButtonState.BASE
                    pressedActionButtons.contains(it.identifier) -> ButtonState.ACTIVE
                    else -> ButtonState.FADED
                }
            }
        })

        Thread {
            createButtonBindings(LAYER_BUTTON_IDS, pressedLayerButtons)
            createButtonBindings(ACTION_BUTTON_IDS, pressedActionButtons)

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

    private fun createButtonBindings(ids: List<Int>, pressedButtons: ObservableList<Int>) {
        ids.forEach { id ->
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

        add(actionButtons[0].apply { textAlignment = TextAlignment.LEFT; identifier = 1 }, cx + GROUP_TO_CENTER_OFFSET - GROUP_RADIUS, cy + GROUP_RADIUS)
        add(actionButtons[1].apply { textAlignment = TextAlignment.RIGHT; identifier = 2 }, cx + GROUP_TO_CENTER_OFFSET + GROUP_RADIUS, cy + GROUP_RADIUS)
        add(actionButtons[2].apply { textAlignment = TextAlignment.RIGHT; identifier = 3 }, cx + GROUP_TO_CENTER_OFFSET + GROUP_RADIUS, cy - GROUP_RADIUS)
        add(actionButtons[3].apply { textAlignment = TextAlignment.LEFT; identifier = 4 }, cx + GROUP_TO_CENTER_OFFSET - GROUP_RADIUS, cy - GROUP_RADIUS)
        add(actionButtons[4].apply { textAlignment = TextAlignment.RIGHT; identifier = 27 }, cx + GROUP_TO_CENTER_OFFSET, cy)
        add(actionButtons[5].apply { textAlignment = TextAlignment.LEFT; identifier = 11 }, cx + GROUP_TO_CENTER_OFFSET - 3 * GROUP_RADIUS, cy)
        add(actionButtons[6].apply { textAlignment = TextAlignment.LEFT; identifier = 7 }, cx + GROUP_TO_CENTER_OFFSET - 3 * GROUP_RADIUS, cy + 2 * GROUP_RADIUS)
        add(actionButtons[7].apply { textAlignment = TextAlignment.LEFT; identifier = 6 }, cx - GROUP_TO_CENTER_OFFSET + 5 * GROUP_RADIUS, cy - 2 * GROUP_RADIUS)
        add(actionButtons[8].apply { textAlignment = TextAlignment.RIGHT; identifier = 5 }, cx + GROUP_TO_CENTER_OFFSET - 5 * GROUP_RADIUS, cy - 2 * GROUP_RADIUS)

        layerProperty.addListener { _, _, _ -> updateButtons() }
        updateButtons()
    }

    private fun updateButtons() {
        val layer: Int = layerProperty.value

        val color: Color = when (layer) {
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

private enum class ButtonState {
    ACTIVE,
    FADED,
    BASE,
    ;
}

private class ButtonGroup : HBox() {
    val colorProperty: SimpleObjectProperty<Color> = SimpleObjectProperty(Color.RED)
    var color: Color
        get() = colorProperty.get()
        set(value) = colorProperty.set(value)
    val identifierProperty = SimpleObjectProperty<Int>(null)
    var identifier: Int?
        get() = identifierProperty.get()
        set(value) = identifierProperty.set(value)
    val textProperty = SimpleStringProperty("")
    var text: String?
        get() = textProperty.get()
        set(value) = textProperty.set(value)
    val textAlignmentProperty: SimpleObjectProperty<TextAlignment> = SimpleObjectProperty(TextAlignment.RIGHT)
    var textAlignment: TextAlignment
        get() = textAlignmentProperty.get()
        set(value) = textAlignmentProperty.set(value)
    val stateProperty: SimpleObjectProperty<ButtonState> = SimpleObjectProperty(ButtonState.BASE)
    var state: ButtonState
        get() = stateProperty.get()
        set(value) = stateProperty.set(value)


    init {
        val circle = Circle(BUTTON_RADIUS).apply {
            fillProperty().bind(Bindings.createObjectBinding({ if (state == ButtonState.FADED) Color.LIGHTGREY else color }, stateProperty, colorProperty))
        }
        val circleLabel = Label().apply {
            textProperty().bind(identifierProperty.asString())
            alignment = Pos.CENTER
            isVisible = false
        }
        val circlePane = StackPane(circle, circleLabel)

        val label = Label().apply {
            effect = DropShadow().apply {
                offsetX = 0.0
                offsetY = 0.0
                radius = 3.0
                spread = 0.5
            }
            isCache = true
            font = Font.font("Consolas", FontWeight.BLACK, FontPosture.REGULAR, 16.0)
            textFillProperty().bind(Bindings.createObjectBinding({ if (state == ButtonState.ACTIVE) color else Color.WHITE }, stateProperty, colorProperty))
            textProperty().bind(textProperty)
            textAlignmentProperty().bind(textAlignmentProperty)
        }
        spacing = 5.0

        textAlignmentProperty.addListener { _, _, newValue ->
            when (newValue) {
                TextAlignment.LEFT -> {
                    children.setAll(label, circlePane)
                    alignment = Pos.CENTER_RIGHT
                }

                TextAlignment.RIGHT -> {
                    children.setAll(circlePane, label)
                    alignment = Pos.CENTER_LEFT
                }

                else -> throw IllegalArgumentException(newValue.toString())
            }
        }
        textAlignmentProperty.set(TextAlignment.LEFT)
    }
}
