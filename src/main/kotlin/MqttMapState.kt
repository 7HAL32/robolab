import communication.MessageManager
import communication.RobolabMessage
import communication.forGroup

/**
 * @author leon
 */
abstract class MqttMapState(
        val groupId: String,
        val stateSetter: (MqttMapState) -> Unit,
        val messageManager: MessageManager,
        val updateCallback: (List<RobolabMessage>) -> Unit
) {
    abstract fun destroy()
    abstract val stateText: String
    abstract val currentIndex: Int
    abstract val planetMessages: List<RobolabMessage>
    abstract fun next()
    abstract fun previous()

    val groupMessages: List<RobolabMessage>
        get() = messageManager.messages forGroup groupId
}