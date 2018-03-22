package communication

import model.Path
import model.Point
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask

/**
 * @author leon
 */
sealed class RobolabMessage(
        val time: Long,
        val groupId: String,
        val topic: String,
        val message: String
) {


    class PathMessage(
            time: Long,
            groupId: String,
            topic: String,
            message: String,
            val path: Path
    ) : RobolabMessage(time, groupId, topic, message)

    class PlanetMessage(
            time: Long,
            groupId: String,
            topic: String,
            message: String,
            val planetName: String,
            val startPoint: Point
    ) : RobolabMessage(time, groupId, topic, message)

    class TargetMessage(
            time: Long,
            groupId: String,
            topic: String,
            message: String,
            val target: Point
    ) : RobolabMessage(time, groupId, topic, message)

    class ReadyMessage(
            time: Long,
            groupId: String,
            topic: String,
            message: String
    ) : RobolabMessage(time, groupId, topic, message)

    override fun toString() = "${SimpleDateFormat("HH:mm:ss").format(Date(time))}: $message"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RobolabMessage

        if (time != other.time) return false
        if (groupId != other.groupId) return false
        if (topic != other.topic) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + groupId.hashCode()
        result = 31 * result + topic.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }


}