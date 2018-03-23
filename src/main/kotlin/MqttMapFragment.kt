import communication.*
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListView
import tornadofx.*

/**
 * @author leon
 */
class MqttMapFragment : BaseMapFragment() {

    val groupId: String by param()
    val messageManager: MessageManager by param()
    var mapState: MqttMapState? = null

    fun setState(state: MqttMapState) {
        mapState?.destroy()
        mapState = state
        state.planetMessages?.let { update(it) }
        modeLabel.text = state.stateText
    }

    private lateinit var listView: ListView<RobolabMessage>
    private lateinit var groupLabel: Label
    private lateinit var planetLabel: Label
    private lateinit var timeLabel: Label
    private lateinit var modeLabel: Label
    private lateinit var planetNumberLabel: Label

    override fun rightView(rootNode: Node) = with(rootNode) {
        vbox {

            hbox {
                button("live mode") {
                    action {
                        setState(LiveMapState(
                                groupId,
                                ::setState,
                                messageManager,
                                ::onUpdate
                        ))
                    }
                }
                button("prev planet") {
                    action {
                        mapState?.previous()
                    }
                }
                button("next planet") {
                    action {
                        mapState?.next()
                    }
                }

            }
            widthProperty().onChange {
                plotter.widthReduce = it
            }
            planetNumberLabel = label()
            modeLabel = label()
            groupLabel = label()
            planetLabel = label()
            timeLabel = label()
            listView = listview {
                onUserSelect(1) { message ->
                    plotter.update(
                            ((mapState?.planetMessages?.takeWhile { it != message } ?: emptyList()) + message)
                                    .toPlanet())
                }
            }
        }
    }


    init {
        setState(LiveMapState(
                groupId,
                ::setState,
                messageManager,
                ::onUpdate
        ))
        mapState?.let { state ->
            update(state.planetMessages, true)
            println(state.planetMessages)
        }

    }

    private fun onUpdate(planetMessages: List<RobolabMessage>) {
        update(planetMessages, planetMessages.lastOrNull() is RobolabMessage.PlanetMessage)
    }

    override fun onUndock() {
        mapState?.destroy()
        super.onUndock()
    }

    private fun update(planetMessages: List<RobolabMessage>, reset: Boolean = false) {
        Platform.runLater {
            listView.items = planetMessages.observable()
            val planet = planetMessages.toPlanet()
            plotter.update(planet, reset)
            val planetCount = (messageManager.messages forGroup groupId).toLog().size
            val planetNumber = (mapState?.currentIndex ?: -1) + 1
            planetNumberLabel.text = "$planetNumber/$planetCount"
            groupLabel.text = "Group id: $groupId"
            planetLabel.text = "Planet name: ${planet.name}"
            timeLabel.text = "Time (sec): " + planetMessages.timeOfFirstMessage.let {
                (System.currentTimeMillis() - it) / 1000
            }.toString()

        }
    }
}