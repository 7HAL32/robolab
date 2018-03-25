import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ChoiceDialog
import javafx.scene.control.TextInputDialog
import javafx.util.Duration
import model.Point
import plotter.Plotter
import tornadofx.*


/**
 * @author lars
 */

class MyApp : App(MyView::class)

class MyView : View() {

    private val canvas = ResizeableCanvas()
    val plotter = Plotter(canvas)

    override val root = borderpane {
        center = canvas
        val bar = toolbar {
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
                        it.name
                    })
                    dialog.title = "Load planet"
                    dialog.headerText = "Load planet"
                    dialog.contentText = "Planet"

                    val result = dialog.showAndWait()
                    result.ifPresent { name ->
                        PlanetProvider.planets.find {
                            it.name == name
                        }?.let {
                            plotter.update(it)
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
                        plotter.update(Planet.fromScratch(Point(split[0].toInt(), split[1].toInt())))
                    }
                }
            }
            button("Export") {
                action {
                    plotter.planet.export().forEach {
                        println(it)
                    }
                }
            }
            button("Set start color") {
                action {
                    val dialog = ChoiceDialog(plotter.planet.startColor.toString(), listOf(
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
                        plotter.update(plotter.planet.setStartColor(color))
                    }
                }
            }
            button("Undo") {
                action {
                    plotter.undo()
                }
            }
            button("Redo") {
                action {
                    plotter.redo()
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
        top = bar

        canvas.widthProperty().bind(widthProperty())
        heightProperty().onChange {
            canvas.heightProperty().set(it - bar.height)
        }
    }

    init {
        val timeline = Timeline(KeyFrame(Duration.seconds(0.2), EventHandler<ActionEvent> {
            plotter.resetScroll()
        }))
        timeline.cycleCount = 1
        timeline.play()
    }
}


fun main(args: Array<String>) {
    Application.launch(MyApp::class.java, *args)
}
