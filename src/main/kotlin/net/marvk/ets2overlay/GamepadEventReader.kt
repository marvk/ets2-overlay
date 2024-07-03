package net.marvk.ets2overlay

import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import net.java.games.input.Component
import net.java.games.input.ControllerEnvironment
import net.java.games.input.Event
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GamepadEventReader(private val deviceName: String, private val pollPeriod: Duration = 20.toDuration(DurationUnit.MILLISECONDS)) {
    private val buttonMap = HashMap<Int, ReadOnlyBooleanWrapper>()

    private fun getPropertyInternal(id: Int) = buttonMap.getOrPut(id) { ReadOnlyBooleanWrapper(false) }

    fun isButtonPressedProperty(id: Int): ReadOnlyBooleanProperty = getPropertyInternal(id).readOnlyProperty
    fun isButtonPressed(id: Int): Boolean = isButtonPressedProperty(id).value

    fun start() {
        while (true) {
            val event = Event()

            val controllers = ControllerEnvironment.getDefaultEnvironment().controllers.filter { it.name == deviceName }

            for (controller in controllers) {
                controller.poll()

                val queue = controller.eventQueue

                while (queue.getNextEvent(event)) {
                    val component = event.component
                    val analog = component.isAnalog

                    if (!analog) {
                        val value = event.value
                        val identifier = component.identifier

                        identifier.name.toIntOrNull()?.also {
                            getPropertyInternal(it + 1).value = if (value > 0.5) true else false
                        }
                    }
                }
            }

            Thread.sleep(pollPeriod.inWholeMilliseconds)
        }
    }
}
