import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.control.ChoiceDialog
import javafx.scene.control.TextInputDialog
import javafx.scene.input.KeyCode
import javafx.util.Duration
import model.Point
import plotter.Plotter
import tornadofx.*


/**
 * @author lars
 */

class MyApp : App(MyView::class)

class MyView : View() {

    val planet = mutableListOf(Planet.empty())

    val canvas = ResizeableCanvas()
    val plotter = Plotter(canvas)

    override val root = borderpane {
        center = canvas
        top = toolbar {
            heightProperty().onChange {
                plotter.heightReduce = it
            }
            button("Enable edit mode") {
                action {
                    if (plotter.editMode) {
                        text = "Enable edit mode"
                        plotter.editMode = false
                    } else {
                        text = "Disable edit mode"
                        plotter.editMode = true
                    }
                }
            }
            button("Load planet") {
                action {
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
                            plotter.update(planet.last())
                        }
                    }
                }
            }
            button("Create planet") {
                action {
                    val dialog = TextInputDialog("0,0")
                    dialog.title = "Create new planet"
                    dialog.headerText = "Create new planet"
                    dialog.contentText = "Start coordinates:"

                    val result = dialog.showAndWait()
                    result.ifPresent { coord ->
                        val split = coord.split(",")
                        planet.add(Planet.fromScratch(Point(split[0].toInt(), split[1].toInt())))
                        plotter.update(planet.last())
                    }
                }
            }
            button("Export") {
                action {
                    planet.last().export()
                }
            }
            button("Set start color") {
                action {
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
                        plotter.update(planet.last())
                    }
                }
            }
            button("Undo") {
                action {
                    if (planet.size > 1) {
                        planet.removeAt(planet.size - 1)
                        plotter.update(planet.last())
                    }
                }
            }
            button("Toggle grid") {
                action {
                    plotter.showGrid = !plotter.showGrid
                }
            }
            button("Toggle grid number") {
                action {
                    plotter.showGridNumber = !plotter.showGridNumber
                }
            }
        }

        setOnKeyPressed {
            when (it.code) {
                KeyCode.UP -> plotter.scrollBy(Point2D(0.0, KEYBOARD_SCROLL))
                KeyCode.DOWN -> plotter.scrollBy(Point2D(0.0, -KEYBOARD_SCROLL))
                KeyCode.LEFT -> plotter.scrollBy(Point2D(KEYBOARD_SCROLL, 0.0))
                KeyCode.RIGHT -> plotter.scrollBy(Point2D(-KEYBOARD_SCROLL, 0.0))
                KeyCode.PLUS -> plotter.zoomIn()
                KeyCode.MINUS -> plotter.zoomOut()
                KeyCode.DIGIT0, KeyCode.EQUALS -> plotter.zoomReset()
                KeyCode.R -> plotter.resetScroll(planet.last().start)
                else -> {
                }
            }
        }
    }

    init {
        canvas.widthProperty().bind(root.widthProperty())
        canvas.heightProperty().bind(root.heightProperty())

        plotter.update(planet.last())

        val timeline = Timeline(KeyFrame(Duration.seconds(0.2), EventHandler<ActionEvent> {
            plotter.resetScroll(planet.last().start)
        }))
        timeline.cycleCount = 1
        timeline.play()
    }

    companion object {
        const val KEYBOARD_SCROLL = 20.0
    }
}


fun main(args: Array<String>) {
    Application.launch(MyApp::class.java, *args)
}
