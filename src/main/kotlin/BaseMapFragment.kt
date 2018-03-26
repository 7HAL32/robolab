import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Pane
import plotter.Plotter
import tornadofx.*

/**
 * @author leon
 */
abstract class BaseMapFragment : Fragment() {

    abstract fun rightView(rootNode: Node): Pane
    private val resizeableCanvas = ResizeableCanvas()
    protected val plotter = Plotter(resizeableCanvas)

    final override val root =
            borderpane {
                center = vbox {
                    add(resizeableCanvas)
                }
                val r = rightView(this)
                right = r

                val b = vbox {
                    alignment = Pos.CENTER_LEFT
                    prefHeight = 32.0
                    val label = text {
                        paddingAll = 8
                    }
                    plotter.positionLabel = label
                    add(label)
                }
                bottom = b

                widthProperty().onChange {
                    resizeableCanvas.widthProperty().set(it - r.width)
                }
                heightProperty().onChange {
                    resizeableCanvas.heightProperty().set(it - b.height)
                }
            }
}