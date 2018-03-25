import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.stage.Screen
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

                widthProperty().onChange {
                    resizeableCanvas.widthProperty().set(it - r.width)
                }
                resizeableCanvas.heightProperty().bind(heightProperty())
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
}