package communication

import communication.mqtt.MqttConnection
import communication.mqtt.MqttMessageListener
import org.eclipse.paho.client.mqttv3.*
import tornadofx.error
import java.util.*

/**
 * @author leon
 */
class RobolabMqttConnection : MqttConnection {

    private val login
        get() = Login.login

    var mqttClient = MqttClient(serverUri, uniqueId())
        private set(value) {
            field = value
        }

    override var subscribedTopics: Set<String> = emptySet()
    private var messageListeners = emptySet<MqttMessageListener>()

    override fun connect() = tryConnecting()

    private fun tryConnecting(): Boolean = try {
        mqttClient = MqttClient(serverUri, uniqueId())
        val mqttConnectOptions = MqttConnectOptions().apply {
            userName = login.username
            password = login.password.toCharArray()
            mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1
        }
        Runtime.getRuntime().addShutdownHook(Thread({
            if (mqttClient.isConnected) {
                disconnect()
            }
        }))
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String, message: MqttMessage) {
                onMessage(topic, message)
            }

            override fun connectionLost(throwable: Throwable) {
                println("connection lost, trying to reconnect …")
                if (tryConnecting()) {
                    println("reconnected")
                } else {
                    error("could not reconnect")
                    throw throwable
                }
            }

            override fun deliveryComplete(p0: IMqttDeliveryToken) {
                // TODO
            }

        })
        mqttClient.connect(mqttConnectOptions)
        subscribedTopics.forEach {
            subscribe(it)
        }
        true
    } catch (e: MqttException) {
        error(e.message ?: "MqttClient could not be connected to server")
        false
    }

    override fun disconnect() = if (mqttClient.isConnected) {
        mqttClient.disconnect()
        true
    } else false

    override fun subscribe(topic: String) = if (mqttClient.isConnected) {
        mqttClient.subscribe(topic, 1)
        subscribedTopics += topic
        true
    } else false

    override fun unsubscribe(topic: String) = if (mqttClient.isConnected) {
        mqttClient.unsubscribe(topic)
        subscribedTopics -= topic
        true
    } else false


    override fun publish(topic: String, message: String) = if (mqttClient.isConnected) {
        mqttClient.publish(topic, message.toByteArray(), 1, false)
        true
    } else false

    override fun addOnMessageListener(listener: MqttMessageListener) = if (listener !in messageListeners) {
        messageListeners += listener
        true
    } else false

    override fun removeOnMessageListener(listener: MqttMessageListener) = if (listener in messageListeners) {
        messageListeners -= listener
        true
    } else false

    private fun onMessage(topic: String, message: MqttMessage) {
        val time = System.currentTimeMillis()
        messageListeners.forEach { it.onMessage(time, topic, message) }
    }

    companion object {
        const val serverUri = "tcp://robolab.inf.tu-dresden.de:8883"
        private fun uniqueId() = UUID.randomUUID().mostSignificantBits.toString()
    }
}