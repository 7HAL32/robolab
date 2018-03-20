import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.control.Button
import javafx.scene.control.ChoiceDialog
import javafx.scene.control.TextInputDialog
import javafx.scene.control.ToolBar
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.util.Duration
import model.Point
import plotter.Plotter
import tornadofx.*


/**
 * @author lars
 */

class MyApp : App(MyView::class)

class MyView : View() {
    override val root = VBox()

    init {
        val planet = mutableListOf(Planet.EMPTY())

        val canvas = ResizeableCanvas()
        val plotter = Plotter(canvas.graphicsContext2D)

        val toolbar = ToolBar()
        toolbar.heightProperty().onChange {
            plotter.heightReduce = it
        }
        val editMode = Button("Enable edit mode")
        editMode.setOnAction {
            if (plotter.editMode) {
                editMode.text = "Enable edit mode"
                plotter.editMode = false
            } else {
                editMode.text = "Disable edit mode"
                plotter.editMode = true
            }
        }
        toolbar += editMode

        val testPlanet = Button("Load planet")
        testPlanet.setOnAction {
            val dialog = ChoiceDialog("", PlanetProvider.planets.map {
                it.fileName.toString().replace(".planet", "")
            })
            dialog.title = "Load planet"
            dialog.headerText = "Load planet"
            dialog.contentText = "Planet"

            val result = dialog.showAndWait()
            result.ifPresent { name ->
                if (PlanetProvider.checkPlanet(name)) {
                    planet.add(PlanetProvider.getPlanet(name))
                    planet.last().plot(plotter)
                }
            }
        }
        toolbar += testPlanet

        val newPlanet = Button("Create planet")
        newPlanet.setOnAction {
            val dialog = TextInputDialog("0,0")
            dialog.title = "Create new planet"
            dialog.headerText = "Create new planet"
            dialog.contentText = "Start coordinates:"

            val result = dialog.showAndWait()
            result.ifPresent { coord ->
                val split = coord.split(",")
                planet.add(Planet.fromScratch(Point(split[0].toInt(), split[1].toInt())))
                planet.last().plot(plotter)
            }
        }
        toolbar += newPlanet

        val exportPlanet = Button("Export")
        exportPlanet.setOnAction {
            planet.last().export()
        }
        toolbar += exportPlanet

        val startColor = Button("Set start color")
        startColor.setOnAction {
            val dialog = ChoiceDialog(planet.last().startColor.toString(), listOf(
                    Point.Color.UNDEFINED.toString(),
                    Point.Color.RED.toString(),
                    Point.Color.BLUE.toString()
            ))
            dialog.title = "Set start color"
            dialog.headerText = "Set start color"
            dialog.contentText = "Start color:"

            val result = dialog.showAndWait()
            result.ifPresent { c ->
                val color = when {
                    c.toUpperCase().startsWith("R") -> Point.Color.RED
                    c.toUpperCase().startsWith("B") -> Point.Color.BLUE
                    else -> Point.Color.UNDEFINED
                }
                planet.add(planet.last().setStartColor(color))
                planet.last().plot(plotter)
            }
        }
        toolbar += startColor

        val undo = Button("Undo")
        undo.setOnAction {
            if (planet.size > 1) {
                planet.removeAt(planet.size - 1)
                planet.last().plot(plotter)
            }
        }
        toolbar += undo

        val toggleGrid = Button("Toggle grid")
        toggleGrid.setOnAction {
            plotter.showGrid = !plotter.showGrid
        }
        toolbar += toggleGrid

        val toggleGridNumber = Button("Toggle grid number")
        toggleGridNumber.setOnAction {
            plotter.showGridNumber = !plotter.showGridNumber
        }
        toolbar += toggleGridNumber

        val borderPane = BorderPane()
        borderPane.center = canvas
        borderPane.top = toolbar
        root += borderPane

        canvas.addDrawHook({
            plotter.draw()
        })
        canvas.widthProperty().bind(root.widthProperty())
        canvas.heightProperty().bind(root.heightProperty())

        var scroll: Point2D = Point2D.ZERO
        canvas.setOnMousePressed {
            if (plotter.isDirectionHighlighted && plotter.editMode) {
                plotter.startPathEditing()
            } else {
                scroll = Point2D(it.x, it.y)
            }
        }
        canvas.setOnMouseDragged {
            if (plotter.isPathEditing) {
                plotter.testPointer(Point2D(it.x, it.y))
            } else {
                plotter.scrollBy(scroll.subtract(it.x, it.y).multiply((-1).toDouble()))
                scroll = Point2D(it.x, it.y)
            }
        }
        canvas.setOnMouseReleased {
            if (plotter.isPathEditing) {
                plotter.testPointer(Point2D(it.x, it.y))
                val p = plotter.finishPathEditing()
                p?.let {
                    planet.add(planet.last().addPath(p))
                    planet.last().plot(plotter)
                }
            }
        }
        canvas.setOnMouseMoved {
            plotter.testPointer(Point2D(it.x, it.y))
        }
        canvas.setOnScroll {
            if (it.deltaY > 0)
                plotter.zoomIn(Point2D(it.x, it.y))
            else if (it.deltaY < 0)
                plotter.zoomOut(Point2D(it.x, it.y))
        }
        val h = 20.0
        root.setOnKeyPressed {
            when (it.code) {
                KeyCode.UP -> plotter.scrollBy(Point2D(0.0, h))
                KeyCode.DOWN -> plotter.scrollBy(Point2D(0.0, -h))
                KeyCode.LEFT -> plotter.scrollBy(Point2D(h, 0.0))
                KeyCode.RIGHT -> plotter.scrollBy(Point2D(-h, 0.0))
                KeyCode.PLUS -> plotter.zoomIn()
                KeyCode.MINUS -> plotter.zoomOut()
                KeyCode.DIGIT0, KeyCode.EQUALS -> plotter.zoomReset()
                KeyCode.R -> plotter.resetScroll(planet.last().start)
                else -> {
                }
            }
        }

        planet.last().plot(plotter)

        val fiveSecondsWonder = Timeline(KeyFrame(Duration.seconds(0.2), EventHandler<ActionEvent> {
            plotter.resetScroll(planet.last().start)
        }))
        fiveSecondsWonder.cycleCount = 1
        fiveSecondsWonder.play()
    }
}


fun main(args: Array<String>) {
    Application.launch(MyApp::class.java, *args)
}
