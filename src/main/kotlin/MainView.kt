import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import communication.Login
import communication.MessageManager
import javafx.application.Platform
import javafx.scene.control.TableView
import tornadofx.*
import java.io.File

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
                    val name =it.name
                    this.item(name).action {
                        find<FileMapFragment>(
                                mapOf(
                                        FileMapFragment::initPlanet to it
                                )
                        ).openWindow()
                    }
                }
            }
        }
        this.center = vbox {

            button("connect") {
                action {
                    if (!messageManager.mqttConnection.mqttConnection.mqttClient.isConnected) {
                        startConnection()
                    }
                }
            }
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

    private fun startConnection() {
        if (Login.loginFromFile().run { password.isEmpty() || username.isEmpty() }) {
            object : View() {
                override val root = vbox {
                    label("username:")
                    val user = textfield()
                    label("password:")
                    val pass = textfield()
                    val wrongLabel = label("Wrong login") {
                        this.isVisible = false
                    }

                    val check = checkbox("Save")
                    button("start") {
                        action {
                            val username = user.characters
                            val password = pass.characters
                            if (username.isNotEmpty() && password.isNotEmpty()) {
                                Login.login = Login.Login(username.toString(), password.toString())
                                if (check.isSelected) {
                                    jacksonObjectMapper()
                                            .writerWithDefaultPrettyPrinter()
                                            .writeValue(
                                                    File("src/main/resources/mqtt_login.json").also {
                                                        if (!it.exists()) {
                                                            it.createNewFile()
                                                        }
                                                    },
                                                    Login.login
                                            )
                                }
                                try {
                                    messageManager.start()
                                    println("connected")
                                    close()
                                } catch (e: IllegalArgumentException) {
                                    wrongLabel.isVisible = true
                                } catch (e: SecurityException) {
                                    wrongLabel.isVisible = true
                                }
                            }
                        }
                    }
                }
            }.openWindow()
        } else {
            Login.login = Login.loginFromFile()
            messageManager.start()
            println("connected")
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

