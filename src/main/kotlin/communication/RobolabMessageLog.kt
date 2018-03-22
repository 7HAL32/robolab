package communication

/**
 * @author leon
 */
class RobolabMessageLog {
    var planets = emptyList<RobolabMessagePlanet>()
        private set(value) {
            field = value
        }

    fun currentPlanet(): RobolabMessagePlanet? = planets.lastOrNull()

    fun addMessage(robolabMessage: RobolabMessage) {
        if (robolabMessage is RobolabMessage.ReadyMessage) {
            planets += RobolabMessagePlanet()
        }
        currentPlanet()?.addMessage(robolabMessage)
    }
}