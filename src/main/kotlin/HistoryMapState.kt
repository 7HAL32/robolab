import communication.MessageManager
import communication.RobolabMessage
import communication.toLog

/**
 * @author leon
 */
class HistoryMapState(
        groupId: String,
        stateSetter: (MqttMapState) -> Unit,
        messageManager: MessageManager,
        updateCallback: (List<RobolabMessage>) -> Unit,
        private val index: Int
) : MqttMapState(groupId, stateSetter, messageManager, updateCallback) {

    override val stateText = "History mode"
    override val currentIndex: Int
        get() = index

    override fun destroy() {

    }

    override fun next() {
        if (index < groupMessages.toLog().size - 2) {
            stateSetter(HistoryMapState(groupId, stateSetter, messageManager, updateCallback, index + 1))
        } else {
            stateSetter(LiveMapState(groupId, stateSetter, messageManager, updateCallback))
        }
    }

    override fun previous() {
        if (index > 0) {
            stateSetter(HistoryMapState(groupId, stateSetter, messageManager, updateCallback, index - 1))
        }

    }

    override val planetMessages: List<RobolabMessage>
        get() = groupMessages.toLog()[index]
}