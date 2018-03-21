package plotter

import Planet
import ResizeableCanvas
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import model.Direction
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
    private val planetHistory = History(Planet.empty())
    val planet
        get() = planetHistory.current()

    init {

        canvas.addDrawHook {
            draw()
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
                testPointer(Point2D(it.x, it.y))
            } else {
                scrollBy(scroll.subtract(it.x, it.y).multiply((-1).toDouble()))
                scroll = Point2D(it.x, it.y)
            }
        }
        canvas.setOnMouseReleased {
            if (isPathEditing) {
                testPointer(Point2D(it.x, it.y))
                val p = finishPathEditing()

                p?.let {
                    planetHistory.push(planet.addPath(p))
                    draw()
                }

            }
        }
        canvas.setOnMouseMoved {
            testPointer(Point2D(it.x, it.y))
        }
        canvas.setOnScroll {
            if (it.deltaY > 0)
                zoomIn(Point2D(it.x, it.y))
            else if (it.deltaY < 0)
                zoomOut(Point2D(it.x, it.y))
        }
    }

    fun update(planet: Planet, reset:Boolean = false) = drawAfter {
        if (reset) {
            planetHistory.reset(planet)
            resetScroll()
        } else {
            planetHistory.push(planet)
        }
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

    fun draw() {
        drawer.clear()

        gridDrawer.draw()
        pointDrawer.draw(planet, pointerEvent)
        pathDrawer.draw(planet, pointerEvent)
    }

    private fun drawAfter(block: () -> Unit) {
        block()
        draw()
    }

    fun scrollBy(d: Point2D) = drawAfter {
        translate += d
    }

    fun resetScroll(point: Point? = null) = drawAfter {
        val p = point ?: planet.getCenter()

        translate = Point2D(width / 2, height * 2 / 3) - drawer.systemToReal(p.to2D(), Point2D.ZERO)
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
    }

    fun zoomOut(zoomTo: Point2D = Point2D(width / 2, height / 2)) = drawAfter {
        val dataPoint = drawer.realToSystem(zoomTo)
        if (scale > 1.0)
            scale -= 0.1
        else
            scale = max(0.1, scale - 0.05)
        val newPoint = drawer.systemToReal(dataPoint)
        translate -= (newPoint - zoomTo)
    }

    fun zoomReset(zoomTo: Point2D = Point2D(width / 2, height / 2)) = drawAfter {
        val dataPoint = drawer.realToSystem(zoomTo)
        scale = 1.0
        pointerEvent = PointerEvent.empty(pointerEvent)
        val newPoint = drawer.systemToReal(dataPoint)
        translate -= (newPoint - zoomTo)
    }

    fun testPointer(point: Point2D) = drawAfter {
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

        const val LINE_HALF = 0.1

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
        }
    }

    enum class LineType {
        NORMAL, THICK
    }
}


