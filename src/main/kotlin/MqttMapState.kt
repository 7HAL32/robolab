import communication.MessageManager
import communication.RobolabMessagePlanet

/**
 * @author leon
 */
abstract class MqttMapState(
        val groupId: String,
        val stateSetter: (MqttMapState) -> Unit,
        val messageManager: MessageManager,
        val updateCallback: (RobolabMessagePlanet) -> Unit
) {
    abstract fun destroy()
    abstract val stateText: String
    abstract val currentIndex: Int
    abstract val messagePlanet: RobolabMessagePlanet?
    abstract fun next()
    abstract fun previous()
}