package mqtt

import org.eclipse.paho.client.mqttv3.*
import java.util.*

/**
 * @author leon
 */
class MqttConnection(
        vararg listener: (String) -> Unit
) {
    val serverUri = "tcp://robolab.inf.tu-dresden.de:8883"
    val clientId = uniqueId()

    val username = Login.login.username
    val password = Login.login.password

    val client by lazy { MqttClient(serverUri, clientId) }
    private val listeners = mutableSetOf(*listener)

    fun connect() {
        val connectOptions = MqttConnectOptions().apply {
            userName = username
            password = this@MqttConnection.password.toCharArray()
            mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1
        }
        client.setCallback(object : MqttCallback {
            override fun connectionLost(p0: Throwable) {
                // TODO: handle
            }

            override fun deliveryComplete(p0: IMqttDeliveryToken) {
                // nothing to do here
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                onMessage(String(message.payload))

            }

        })

        client.connect(connectOptions)
    }

    fun subscribe(channel: String) = client.subscribe(channel)

    fun unsubscribe(channel: String) = client.unsubscribe(channel)

    fun onMessage(message: String) {
        listeners.forEach { it(message) }
    }

    companion object {
        fun uniqueId() = UUID.randomUUID().mostSignificantBits.toString()
    }
}