package communication.mqtt

import model.Direction
import model.Path
import model.Point

/**
 * @author leon
 */
object MqttParser {

    object Prefix {
        const val PATH = "path"
        const val TARGET = "target"
        const val READY = "ready"
    }

    fun getMessageType(message: String): Pair<MessageType, Boolean> {
        val fromRobot = !message.startsWith("ACK")
        val messageType = with(message.drop(4)) {
            when {
                startsWith(Prefix.PATH) -> MessageType.PATH
                startsWith(Prefix.TARGET) && !fromRobot -> MessageType.TARGET
                startsWith(Prefix.READY) -> MessageType.READY
                !fromRobot -> MessageType.PLANET
                else -> MessageType.UNKNOWN
            }
        }
        return Pair(messageType, fromRobot)
    }

    fun parsePlanet(message: String) =
            message.split("[, ]".toRegex())
                    .let {
                        Pair(it[1], Point(it[2].toInt(), it[3].toInt()))
                    }

    fun parsePath(message: String, fromRobot: Boolean) =
            message.split("[, ]".toRegex())
                    .let {
                        Path(
                                Point(it[2].toInt(), it[3].toInt()),
                                Direction.parse(it[4]),
                                Point(it[5].toInt(), it[6].toInt()),
                                Direction.parse(it[7]),
                                if (fromRobot) null else it[9].toInt()
                        )
                    }

    fun parseTarget(message: String) =
            message.split("[, ]".toRegex())
                    .let { Point(it[2].toInt(), it[3].toInt()) }

    enum class MessageType {
        READY, PLANET, PATH, TARGET, UNKNOWN
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val messages = listOf(
                "testplanet Endor",
                "SYN ready",
                "SYN path 0,1,S 3,2,W free",
                "SYN path 0,1,S 3,2,W blocked",
                "SYN target reached! foo",
                "SYN exploration completed! foo",
                "ACK Endor-123 4,-14",
                "ACK path 0,1,S 3,2,W free 1",
                "ACK path 0,1,S 3,2,W blocked -1",
                "ACK target -13,15"
        )
        messages.forEach {
            val messageType = getMessageType(it)
            println("$it: $messageType")
            when {
                messageType.first == MessageType.PATH -> println(parsePath(it, messageType.second))
                messageType.first == MessageType.PLANET -> println(parsePlanet(it))
                messageType.first == MessageType.TARGET -> println(parseTarget(it))
            }
        }
    }
}