package communication

import communication.mqtt.MqttMessageListener
import communication.mqtt.MqttParser
import org.eclipse.paho.client.mqttv3.MqttMessage

/**
 * @author leon
 */

class RobolabMessageProvider : MqttMessageListener {

    val mqttConnection = RobolabMqttConnection().apply {
        addOnMessageListener(this@RobolabMessageProvider)
    }

    private var listeners = setOf<RobolabMessageListener>()

    fun start() = if (mqttConnection.connect()) {
        mqttConnection.subscribe("#")
        true
    } else {
        false
    }

    fun addMessageListener(listener: RobolabMessageListener) {
        listeners += listener
    }

    fun removeMessageListener(listener: RobolabMessageListener) {
        listeners -= listener
    }

    override fun onMessage(timeArrived: Long, topic: String, message: MqttMessage) {
        val msg = String(message.payload)
        println("$topic: $message")

        val (messageType, fromRobot) = MqttParser.getMessageType(msg)
        when {
            topic.startsWith("explorer") -> onExplorerMessage(timeArrived, topic, msg, messageType, fromRobot)
            topic.startsWith("planet") -> onPlanetMessage(timeArrived, topic, msg, messageType, fromRobot)
            else -> return
        }
    }

    private fun onPlanetMessage(
            timeArrived: Long,
            topic: String,
            message: String,
            messageType: MqttParser.MessageType,
            fromRobot: Boolean
    ) {
        val groupId = topic.split("-")[1]
        val robolabMessage = when {
            messageType == MqttParser.MessageType.PATH -> {
                val path = MqttParser.parsePath(message, fromRobot)
                RobolabMessage.PathMessage(timeArrived, groupId, topic, message, path)
            }
            !fromRobot && messageType == MqttParser.MessageType.TARGET -> {
                val target = MqttParser.parseTarget(message)
                RobolabMessage.TargetMessage(timeArrived, groupId, topic, message, target)
            }
            else -> return
        }
        onRobolabMessage(robolabMessage)
    }

    private fun onExplorerMessage(
            timeArrived: Long,
            topic: String,
            message: String,
            messageType: MqttParser.MessageType,
            fromRobot: Boolean
    ) {

        val groupId = topic.split("/")[1]
        val robolabMessage = when {
            fromRobot && messageType == MqttParser.MessageType.READY -> RobolabMessage.ReadyMessage(timeArrived, groupId, topic, message)
            !fromRobot && messageType == MqttParser.MessageType.PLANET -> {
                val (planetName, startPoint) = MqttParser.parsePlanet(message)
                RobolabMessage.PlanetMessage(timeArrived, groupId, topic, message, planetName, startPoint)
            }
            else -> return
        }
        onRobolabMessage(robolabMessage)
    }

    private fun onRobolabMessage(robolabMessage: RobolabMessage) {
        listeners.forEach { it.onRobolabMessage(robolabMessage) }
    }
}




