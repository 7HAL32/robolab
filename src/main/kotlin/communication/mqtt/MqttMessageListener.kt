package communication.mqtt

import org.eclipse.paho.client.mqttv3.MqttMessage

/**
 * @author leon
 */
interface MqttMessageListener {
    fun onMessage(timeArrived: Long, topic: String, message: MqttMessage)
}