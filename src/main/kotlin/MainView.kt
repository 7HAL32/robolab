import communication.MessageManager
import javafx.application.Platform
import javafx.scene.control.TableView
import tornadofx.*

/**
 * @author leon
 */
class MainView : View() {

    private lateinit var groupTable: TableView<GroupInfo>
    private val messageManager = MessageManager()

    override val root = borderpane {
        this.top = menubar {
            menu("Foo") {
                item("Export everything")
                item("Import from file")
            }
            menu("Planet") {
                PlanetProvider.planets.forEach {
                    this.item(it.fileName.toString())
                }
            }
        }
        this.center = vbox {

            groupTable = tableview {
                column("ID", GroupInfo::groupNameProperty)
                column("last msg", GroupInfo::lastMessageTimeProperty)
                column("server paths", GroupInfo::numberOfServerPathsProperty)
                column("robot paths", GroupInfo::numberOfRobotPathsProperty)
                column("old planet count", GroupInfo::oldPlanetCountProperty)

                this.setOnMouseClicked {
                    if (it.clickCount == 2) {
                        selectedItem?.let {
                            find<MqttMapFragment>(
                                    mapOf(
                                            MqttMapFragment::groupId to it.groupName,
                                            MqttMapFragment::messageManager to messageManager
                                    )
                            ).openWindow()
                        }
                    }
                }
            }
        }
    }

    override fun onUndock() {
        println("exiting")
        System.exit(0)
    }

    init {
        messageManager.addListener {
            Platform.runLater {
                groupTable.items = it.groupBy { it.groupId }.map {
                    GroupInfo(it.key, it.value)
                }
                        .sortedByDescending { it.lastMessageTime }
                        .observable()
            }
        }
    }

}

