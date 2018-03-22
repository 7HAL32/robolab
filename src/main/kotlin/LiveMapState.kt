/**
 * @author leon
 */
import communication.MessageManager
import communication.RobolabMessageLog
import communication.RobolabMessagePlanet

/**
 * @author leon
 */
class LiveMapState(
        groupId: String,
        stateSetter: (MqttMapState) -> Unit,
        messageManager: MessageManager,
        updateCallback: (RobolabMessagePlanet) -> Unit
) : MqttMapState(groupId, stateSetter, messageManager, updateCallback) {

    init {
        messageManager.addListenerForGroup(groupId, ::onUpdate)
    }

    override val stateText = "Live mode"
    override val currentIndex
        get() = (messageManager.groupDataMap[groupId]?.planets?.size ?: 0) - 1

    private fun onUpdate(log: RobolabMessageLog) {
        log.currentPlanet()?.let { updateCallback(it) }
    }

    override fun destroy() {
        messageManager.removeListenerForGroup(groupId, ::onUpdate)
    }

    override fun next() {
    }

    override fun previous() {
        messageManager.groupDataMap[groupId]?.planets?.let {
            if (it.size > 1) {
                stateSetter(HistoryMapState(
                        groupId,
                        stateSetter,
                        messageManager,
                        updateCallback,
                        it.size - 2
                ))
            }
        }
    }

    override val messagePlanet: RobolabMessagePlanet?
        get() = messageManager.groupDataMap[groupId]?.currentPlanet()
}