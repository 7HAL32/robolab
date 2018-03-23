import communication.RobolabMessage
import communication.toLog
import tornadofx.observable
import java.text.SimpleDateFormat
import java.util.*

class GroupInfo(
        val groupName: String,
        groupMessages: List<RobolabMessage>
) {
    val mqttMessages = groupMessages.toLog().last()

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

    val oldPlanetCount = groupMessages.toLog().size - 1
    val oldPlanetCountProperty = observable(this, GroupInfo::oldPlanetCount)
}