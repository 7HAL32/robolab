import communication.RobolabMessage
import communication.RobolabMessageLog
import tornadofx.observable
import java.text.SimpleDateFormat
import java.util.*

class GroupInfo(
        val groupName: String,
        log: RobolabMessageLog
) {
    val mqttMessages = log.currentPlanet()?.messages ?: emptyList()

    val groupNameProperty = observable(this, GroupInfo::groupName)

    val lastMessageTime = SimpleDateFormat("HH:mm:ss").format(Date(mqttMessages.lastOrNull()?.time ?: 0))
    val lastMessageTimeProperty = observable(this, GroupInfo::lastMessageTime)

    val numberOfServerPaths = mqttMessages.filter {
        it is RobolabMessage.PathMessage && it.message.startsWith("ACK")
    }.size
    val numberOfServerPathsProperty = observable(this, GroupInfo::numberOfServerPaths)

    val numberOfRobotPaths = mqttMessages.filter {
        it is RobolabMessage.PathMessage && it.message.startsWith("SYN")
    }.size
    val numberOfRobotPathsProperty = observable(this, GroupInfo::numberOfRobotPaths)

    val oldPlanetCount = log.planets.size - 1
    val oldPlanetCountProperty = observable(this, GroupInfo::oldPlanetCount)
}