package communication

/**
 * @author leon
 */
class MessageManager : RobolabMessageListener {

    private val mqttConnection = RobolabMessageProvider()
    var messages: List<RobolabMessage> = emptyList()
        private set(value) {
            field = value
        }
    private var listeners: Set<(List<RobolabMessage>) -> Unit> = emptySet()
    private var groupListeners: Map<String, Set<(List<RobolabMessage>) -> Unit>> = emptyMap()

    init {
        mqttConnection.addMessageListener(this)
        mqttConnection.start()
    }

    fun addListener(listener: (List<RobolabMessage>) -> Unit) {
        listeners += listener
    }

    fun addListenerForGroup(groupId: String, listener: (List<RobolabMessage>) -> Unit) {
        groupListeners = with(groupListeners) {
            filterNot { it.key == groupId }.plus(groupId to (getOrDefault(groupId, emptySet())) + listener)
        }
    }

    fun removeListener(listener: (List<RobolabMessage>) -> Unit) {
        listeners -= listener
    }

    fun removeListenerForGroup(groupId: String, listener: (List<RobolabMessage>) -> Unit) {
        groupListeners = with(groupListeners) {
            filterNot { it.key == groupId }.plus(groupId to (getOrDefault(groupId, emptySet())) - listener)
        }
    }


    override fun onRobolabMessage(message: RobolabMessage) {
        messages += message
        updateGroup(message.groupId)
        update()
    }

    private fun update() = listeners.forEach { it(messages) }

    private fun updateGroup(groupId: String) {
        groupListeners[groupId]?.forEach { listener ->
            listener(messages.filter { it.groupId == groupId })
        }
    }
}
