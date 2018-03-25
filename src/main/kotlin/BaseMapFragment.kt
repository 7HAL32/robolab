import javafx.scene.Node
import javafx.stage.Screen
import plotter.Plotter
import tornadofx.*

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

    override fun onDock() {
        val screen = Screen.getPrimary()
        val bounds = screen.visualBounds

        currentStage?.x = bounds.minX + bounds.width / 6
        currentStage?.y = bounds.minY + bounds.height / 6
        currentStage?.width = bounds.width * 2 / 3
        currentStage?.height = bounds.height * 2 / 3

        currentStage?.isMaximized = true
        currentStage?.isIconified = true
    }

    init {
        resizeableCanvas.widthProperty().bind(root.widthProperty())
        resizeableCanvas.heightProperty().bind(root.heightProperty())
    }
}