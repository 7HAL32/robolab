package communication

import Planet
import model.Point
import plotter.PathAttributes

class RobolabMessagePlanet {
    var messages = emptyList<RobolabMessage>()
        private set(value) {
            field = value
        }

    val timeOfFirstMessage
        get() = messages.firstOrNull()?.time

    fun addMessage(message: RobolabMessage) {
        messages += message
    }

    fun toPlanet() = listToPlanet(messages)

    companion object {
        fun listToPlanet(list: List<RobolabMessage>): Planet {
            val (name, startPoint) = (list
                    .firstOrNull { it is RobolabMessage.PlanetMessage } as? RobolabMessage.PlanetMessage)
                    ?.let {
                        it.planetName to it.startPoint
                    } ?: null to null
            val paths = list.mapNotNull {
                it as? RobolabMessage
                .PathMessage
            }.map { it.path to emptySet<PathAttributes>() }
            val target = (list
                    .firstOrNull { it is RobolabMessage.TargetMessage } as? RobolabMessage.TargetMessage)?.target
            val startColor = Point.Color.UNDEFINED
            return Planet(name ?: "", paths, target, startPoint ?: Point(0, 0) /* TODO */, startColor)
        }
    }
}
