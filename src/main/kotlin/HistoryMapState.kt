import communication.MessageManager
import communication.RobolabMessagePlanet

/**
 * @author leon
 */
class HistoryMapState(
        groupId: String,
        stateSetter: (MqttMapState) -> Unit,
        messageManager: MessageManager,
        updateCallback: (RobolabMessagePlanet) -> Unit,
        private val index: Int
) : MqttMapState(groupId, stateSetter, messageManager, updateCallback) {

    override val stateText = "History mode"
    override val currentIndex: Int
        get() = index

    override fun destroy() {

    }

    override fun next() {
        messageManager.groupDataMap[groupId]?.planets?.size?.let {
            if (index < it - 2) {
                stateSetter(HistoryMapState(groupId, stateSetter, messageManager, updateCallback, index + 1))
            } else {
                stateSetter(LiveMapState(groupId, stateSetter, messageManager, updateCallback))
            }
        }
    }

    override fun previous() {
        if (index > 0) {
            stateSetter(HistoryMapState(groupId, stateSetter, messageManager, updateCallback, index - 1))
        }

    }

    override val messagePlanet: RobolabMessagePlanet?
        get() = messageManager.groupDataMap[groupId]?.planets?.get(index)
}