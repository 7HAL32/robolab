import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.ListView
import tornadofx.*


/**
 * @author leon
 */
class FileMapFragment : BaseMapFragment() {

    val initPlanet: Planet by param()
    var planet: Planet

    private lateinit var listView: ListView<String>

    override fun rightView(rootNode: Node) = with(rootNode) {
        vbox {
            hbox {
                button("Save to file") {
                    action {
                        save()
                    }
                }
            }
            widthProperty().onChange {
                plotter.widthReduce = it
            }
            listView = listview {
                isEditable = true
                selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                    println("Selected item: $newValue")
                }
                onEdit { editEventType, s ->
                    println("Edit type='$editEventType' line='$s'")
                }
            }
        }
    }


    init {
        planet = initPlanet
        update()
    }

    private fun update() {
        val list: ObservableList<String> = observableList<String>(*planet.export().toTypedArray())

        listView.items = list
        plotter.update(planet)
    }

    private fun save() {

    }
}