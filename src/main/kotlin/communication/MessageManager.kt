package communication

/**
 * @author leon
 */
class MessageManager : RobolabMessageListener {

    private val mqttConnection = RobolabMessageProvider()
    val groupDataMap = mutableMapOf<String, RobolabMessageLog>()
    private var listeners = setOf<(Map<String, RobolabMessageLog>) -> Unit>()
    private val groupListeners = mutableMapOf<String, MutableList<(RobolabMessageLog) -> Unit>>()

    init {
        mqttConnection.addMessageListener(this)
        mqttConnection.start()
    }

    fun addListener(listener: (Map<String, RobolabMessageLog>) -> Unit) {
        listeners += listener
    }

    fun addListenerForGroup(groupId: String, listener: (RobolabMessageLog) -> Unit) =
            groupListeners.getOrPut(groupId) { mutableListOf() }.add(listener)

    fun removeListener(listener: (Map<String, RobolabMessageLog>) -> Unit) {
        listeners -= listener
    }

    fun removeListenerForGroup(groupId: String, listener: (RobolabMessageLog) -> Unit) =
            groupListeners[groupId]?.remove(listener) ?: false


    override fun onRobolabMessage(message: RobolabMessage) {
        val log = groupDataMap.getOrPut(message.groupId, ::RobolabMessageLog)
        log.addMessage(message)
        updateGroup(message.groupId)
        update()
    }

    private fun update() = listeners.forEach { it(groupDataMap.toMap()) }

    private fun updateGroup(groupId: String) {
        val log = groupDataMap[groupId]
        if (log != null) {
            groupListeners[groupId]?.forEach {
                it(log)
            }
        }
    }
}
