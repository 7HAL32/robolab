import javafx.scene.Node
import plotter.Plotter
import tornadofx.Fragment
import tornadofx.borderpane
import tornadofx.vbox

/**
 * @author leon
 */
abstract class BaseMapFragment : Fragment() {

    abstract fun rightView(rootNode: Node): Node
    private val resizeableCanvas = ResizeableCanvas()
    protected val plotter = Plotter(resizeableCanvas)

    final override val root =
            borderpane {
                minWidth = 1000.0
                center = vbox {
                    add(resizeableCanvas)
                }
                right = rightView(this)

            }

    init {
        resizeableCanvas.widthProperty().bind(root.widthProperty())
        resizeableCanvas.heightProperty().bind(root.heightProperty())
    }
}