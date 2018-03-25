import javafx.geometry.Pos
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