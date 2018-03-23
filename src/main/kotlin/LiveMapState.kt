/**
 * @author leon
 */
import communication.MessageManager
import communication.RobolabMessage
import communication.toLog

/**
 * @author leon
 */
class LiveMapState(
        groupId: String,
        stateSetter: (MqttMapState) -> Unit,
        messageManager: MessageManager,
        updateCallback: (List<RobolabMessage>) -> Unit
) : MqttMapState(groupId, stateSetter, messageManager, updateCallback) {

    init {
        messageManager.addListenerForGroup(groupId, ::onUpdate)
    }

    override val stateText = "Live mode"
    override val currentIndex
        get() = messageManager.messages.filter { it.groupId == groupId }.toLog().lastIndex

    private fun onUpdate(groupMessages: List<RobolabMessage>) = updateCallback(groupMessages.toLog().last())

    override fun destroy() {
        messageManager.removeListenerForGroup(groupId, ::onUpdate)
    }

    override fun next() {
    }

    override fun previous() {

        groupMessages.toLog().let {
            if (it.size > 1) {
                stateSetter(HistoryMapState(
                        groupId,
                        stateSetter,
                        messageManager,
                        updateCallback,
                        it.lastIndex - 1
                ))
            }
        }
    }

    override val planetMessages: List<RobolabMessage>
        get() = groupMessages.toLog().last()
}