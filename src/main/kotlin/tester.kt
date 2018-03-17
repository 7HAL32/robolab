import javafx.application.Application
import javafx.geometry.Point2D
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.layout.VBox
import model.Direction
import model.Path
import model.Point
import plotter.PathAttributes
import plotter.Plotter
import tornadofx.*

/**
 * @author lars
 */

class MyApp : App(MyView::class)

class MyView : View() {
    override val root = VBox()

    init {
        val planet = PlanetProvider.getPlanet("large_staircase")

        val canvas = ResizeableCanvas()
        root += canvas

        val btn = Button("Draw")
        root += btn
        val plotter = Plotter(canvas.graphicsContext2D)

        canvas.addDrawHook({
            plotter.draw()
        })
        canvas.widthProperty().bind(root.widthProperty())
        canvas.heightProperty().bind(root.heightProperty())

        var scroll: Point2D = Point2D.ZERO
        canvas.setOnMousePressed {
            scroll = Point2D(it.x, it.y)
        }
        canvas.setOnMouseDragged {
            plotter.scroll(scroll.subtract(it.x, it.y).multiply((-1).toDouble()))
            scroll = Point2D(it.x, it.y)
        }
        canvas.setOnScroll {
            if (it.deltaY > 0)
                plotter.zoomIn()
            else if (it.deltaY < 0)
                plotter.zoomOut()
        }
        val h = 20.0
        root.setOnKeyPressed {
            when (it.code) {
                KeyCode.UP -> plotter.scroll(Point2D(0.0, -h))
                KeyCode.DOWN -> plotter.scroll(Point2D(0.0, h))
                KeyCode.LEFT -> plotter.scroll(Point2D(-h, 0.0))
                KeyCode.RIGHT -> plotter.scroll(Point2D(h, 0.0))
                KeyCode.PLUS -> plotter.zoomIn()
                KeyCode.MINUS -> plotter.zoomOut()
                KeyCode.DIGIT0, KeyCode.EQUALS -> plotter.zoomReset()
                KeyCode.R -> plotter.resetScroll(planet.start)
                else -> {
                }
            }
        }

        planet.plot(plotter)
    }
}


fun main(args: Array<String>) {
    Application.launch(MyApp::class.java, *args)
}
