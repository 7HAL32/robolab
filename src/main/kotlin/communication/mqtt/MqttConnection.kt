package communication.mqtt

/**
 * @author leon
 */
interface MqttConnection {

    val subscribedTopics: Set<String>

    fun connect(): Boolean
    fun disconnect(): Boolean
    fun subscribe(topic: String): Boolean
    fun unsubscribe(topic: String): Boolean
    fun publish(topic: String, message: String): Boolean
    fun addOnMessageListener(listener: MqttMessageListener): Boolean
    fun removeOnMessageListener(listener: MqttMessageListener): Boolean
}