package communication

import Planet
import model.Point
import plotter.PathAttributes

val List<RobolabMessage>.timeOfFirstMessage
    get() = first().time

fun List<RobolabMessage>.toLog() = fold(emptyList<List<RobolabMessage>>()) { acc, msg ->
    if (msg is RobolabMessage.ReadyMessage || acc.isEmpty()) {
        acc.plus<List<RobolabMessage>>(listOf(msg))
    } else {
        acc.dropLast(1).plus<List<RobolabMessage>>(acc.lastOrNull()?.plus(msg) ?: listOf(msg))
    }
}

fun List<RobolabMessage>.toPlanet(): Planet {
    val (name, startPoint) = (firstOrNull { it is RobolabMessage.PlanetMessage } as? RobolabMessage.PlanetMessage)
            ?.let {
                it.planetName to it.startPoint
            } ?: firstOrNull { it.topic.startsWith("planet/") }?.topic?.split("/")?.get(1) to null
    val paths = mapNotNull {
        it as? RobolabMessage
        .PathMessage
    }.map { it.path to emptySet<PathAttributes>() }
    val target = (firstOrNull { it is RobolabMessage.TargetMessage } as? RobolabMessage.TargetMessage)?.target
    val startColor = Point.Color.UNDEFINED
    return Planet(name ?: "", paths, target, startPoint /* TODO */, startColor)
}

infix fun List<RobolabMessage>.forGroup(groupId: String) = filter { it.groupId == groupId }