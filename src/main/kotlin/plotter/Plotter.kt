package plotter

import Planet
import ResizeableCanvas
import javafx.animation.Animation.INDEFINITE
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.util.Duration
import model.Direction
import model.LiveOdometry
import model.Path
import model.Point
import kotlin.math.max
import kotlin.math.min

/**
 * @author lars
 */

class Plotter(
        val canvas: ResizeableCanvas
) {

    internal var scale = 1.0
    internal var translate = Point2D.ZERO

    private val drawer = DrawHelper(this)

    private var gridDrawer = GridDrawer(drawer)
    private var pointDrawer = PointDrawer(drawer)
    private var pathDrawer = PathDrawer(drawer)
    private var odometryDrawer = OdometryDrawer(drawer)
    private var liveOdometry = emptyList<LiveOdometry>()
    private val planetHistory = History(Planet.empty())
    val planet
        get() = planetHistory.current()

    private var update: Boolean = false

    private var animationProgress: Double = 1.0
    private var isUserTranslated: Boolean = false

    init {

        canvas.addDrawHook {
            update(reset = !isUserTranslated)
        }

        var scroll: Point2D = Point2D.ZERO
        canvas.setOnMousePressed {
            if (isDirectionHighlighted && editMode) {
                startPathEditing()
            } else {
                scroll = Point2D(it.x, it.y)
            }
        }
        canvas.setOnMouseDragged {
            if (isPathEditing) {
                checkPosition(Point2D(it.x, it.y))
            } else {
                scrollBy(scroll.subtract(it.x, it.y).multiply((-1).toDouble()))
                scroll = Point2D(it.x, it.y)
            }
        }
        canvas.setOnMouseReleased {
            if (isPathEditing) {
                checkPosition(Point2D(it.x, it.y))
                val p = finishPathEditing()

                p?.let {
                    planetHistory.push(planet.addPath(p))
                }

            }
        }
        canvas.setOnMouseMoved {
            checkPosition(Point2D(it.x, it.y))
        }
        canvas.setOnScroll {
            if (it.deltaY > 0)
                zoomIn(Point2D(it.x, it.y))
            else if (it.deltaY < 0)
                zoomOut(Point2D(it.x, it.y))
        }


        val timeline = Timeline(
                KeyFrame(
                        Duration.seconds(1.0 / FPS),
                        EventHandler<ActionEvent> {
                            draw()
                        }
                )
        )
        timeline.cycleCount = INDEFINITE
        timeline.play()
    }

    fun update(planet: Planet? = null, reset: Boolean = false) = drawAfter {
        planet?.let {
            animationProgress = if (it.hasAnimation()) 0.0 else 1.0

            if (reset) {
                planetHistory.reset(it)
            } else {
                planetHistory.push(it)
            }
        }
        if (reset) {
            zoomReset()
            resetScroll()
        }
    }

    fun liveOdometry(data: List<LiveOdometry>) = drawAfter {
        liveOdometry = data
    }

    fun undo() = drawAfter {
        planetHistory.undo()
    }

    fun redo() = drawAfter {
        planetHistory.redo()
    }

    var showGrid: Boolean
        get() = gridDrawer.showGrid
        set(value) = drawAfter {
            gridDrawer.showGrid = value
        }

    var showGridNumber: Boolean
        get() = gridDrawer.showGridNumber
        set(value) = drawAfter {
            gridDrawer.showGridNumber = value
        }

    var editMode: Boolean
        get() = pointDrawer is EditPointDrawer
        set(value) = drawAfter {
            if (value) {
                pointDrawer = EditPointDrawer(drawer)
                pathDrawer = EditPathDrawer(drawer)
            } else {
                pointDrawer = PointDrawer(drawer)
                pathDrawer = PathDrawer(drawer)
            }
        }

    private var pointerEvent: PointerEvent = PointerEvent.empty(Point2D.ZERO)

    val isDirectionHighlighted: Boolean
        get() = pointerEvent.direction != null

    val isPathEditing: Boolean
        get() = (pointDrawer as? EditPointDrawer)?.isPathEditing() ?: false

    fun startPathEditing() {
        pointerEvent.point?.let { point ->
            pointerEvent.direction?.let { direction ->
                val editStart = Pair(point, direction)
                (pointDrawer as? EditPointDrawer)?.editStart = editStart
                (pathDrawer as? EditPathDrawer)?.editStart = editStart
            }
        }
    }

    fun finishPathEditing(): Path? {
        return (pathDrawer as? EditPathDrawer)?.let {
            val result = it.getPath(pointerEvent)

            (pointDrawer as? EditPointDrawer)?.editStart = null
            (pathDrawer as? EditPathDrawer)?.editStart = null

            result
        }
    }

    internal var heightReduce: Double = 0.0
        set(value) = drawAfter {
            field = value
        }

    internal var widthReduce: Double = 0.0
        set(value) = drawAfter {
            field = value
        }

    internal val width: Double
        get() = canvas.width - widthReduce

    internal val height: Double
        get() = canvas.height - heightReduce

    private fun draw() {
        if (update || animationProgress < 1.0) {
            update = false
            drawer.clear()

            gridDrawer.draw()

            if (liveOdometry.isNotEmpty()) {
                odometryDrawer.draw(liveOdometry)
            }

            pointDrawer.draw(planet, pointerEvent, animationProgress)
            pathDrawer.draw(planet, pointerEvent, animationProgress)

            if (animationProgress < 1.0) {
                animationProgress += 1 / (FPS * ANIMATION_TIME)
                if (animationProgress > 1.0) {
                    animationProgress = 1.0
                }
            }

            drawer.cleanSides()
        }
    }

    private fun drawAfter(block: () -> Unit) {
        block()
        update = true
    }

    fun scrollBy(d: Point2D) = drawAfter {
        translate += d
        isUserTranslated = true
    }

    fun resetScroll(point: Point? = null) = drawAfter {
        val p = point ?: planet.getCenter()
        isUserTranslated = false

        println("Reset scroll to $point at the size $width, $height")
        translate = Point2D(width / 2 + widthReduce/2, height * 2 / 3) - drawer.systemToReal(p.to2D(), Point2D.ZERO)
        pointerEvent = PointerEvent.empty(pointerEvent.mouse)
    }

    fun zoomIn(zoomTo: Point2D = Point2D(width / 2, height / 2)) = drawAfter {
        val dataPoint = drawer.realToSystem(zoomTo)
        if (scale < 1.0)
            scale += 0.05
        else
            scale = min(10.0, scale + 0.1)
        val newPoint = drawer.systemToReal(dataPoint)

        translate -= (newPoint - zoomTo)
        isUserTranslated = true
    }

    fun zoomOut(zoomTo: Point2D = Point2D(width / 2, height / 2)) = drawAfter {
        val dataPoint = drawer.realToSystem(zoomTo)
        if (scale > 1.0)
            scale -= 0.1
        else
            scale = max(0.1, scale - 0.05)
        val newPoint = drawer.systemToReal(dataPoint)
        translate -= (newPoint - zoomTo)
        isUserTranslated = true
    }

    fun zoomReset(zoomTo: Point2D = Point2D(width / 2, height / 2)) = drawAfter {
        val dataPoint = drawer.realToSystem(zoomTo)
        scale = 1.0
        pointerEvent = PointerEvent.empty(pointerEvent)
        val newPoint = drawer.systemToReal(dataPoint)
        translate -= (newPoint - zoomTo)
        isUserTranslated = true
    }

    private fun checkPosition(point: Point2D) = drawAfter {
        val mouse = drawer.realToSystem(point)

        val col = Math.round(mouse.x).toInt()
        val dx = Math.abs(mouse.x - col)
        val row = Math.round(mouse.y).toInt()
        val dy = Math.abs(mouse.y - row)

        pointerEvent = when {
            dx < POINT_SIZE / 2 && dy < POINT_SIZE / 2 -> PointerEvent(
                    Point(col, row),
                    null,
                    null,
                    mouse
            )
            (dx < POINT_SIZE && dy < POINT_SHIFT) || (dx < POINT_SHIFT && dy < POINT_SIZE) -> PointerEvent(
                    Point(col, row),
                    when {
                        mouse.x - col > POINT_SIZE / 2 -> Direction.EAST
                        col - mouse.x > POINT_SIZE / 2 -> Direction.WEST
                        mouse.y - row > POINT_SIZE / 2 -> Direction.NORTH
                        row - mouse.y > POINT_SIZE / 2 -> Direction.SOUTH
                        else -> null
                    },
                    null,
                    mouse
            )
            else -> PointerEvent.empty(mouse)
        }
    }

    companion object {
        const val WIDTH_GRID = 100

        const val POINT_SIZE = 0.22
        const val POINT_SHIFT = 0.28
        const val POINT_RADIUS = 0.25
        const val RADIUS = 0.3
        const val LINE_HALF = 0.1
        const val ARROW_SIZE = 0.15

        const val FPS = 50.0
        const val ANIMATION_TIME = 20.0

        object COLOR {
            val RED: Color = Color.web("#F44336")
            val RED_LIGHT: Color = Color.web("#FFEBEE")
            val BLUE: Color = Color.web("#3F51B5")
            val BLUE_LIGHT: Color = Color.web("#E8EAF6")
            val LINE: Color = Color.web("#263238")
            val LINE_LIGHT: Color = Color.web("#607D8B")
            val GRID: Color = Color.web("#E0E0E0")
            val GRID_NUMBER: Color = Color.web("#C0C0C0")
            val BACKGROUND: Color = Color.web("#FFFFFF")
            val ROBOT: Color = Color.web("#FF9800")
            val TARGET: Color = Color.web("#AED581")
            val BLOCKED: Color = Color.web("#F44336")
            val HIGHLIGHT: Color = Color.web("#009688")
            val WEIGHT: Color = Color.web("#666666")
            val ODOMETRY: Color = Color.web("#CDDC39")
        }
    }

    enum class LineType {
        NORMAL, THICK
    }
}


