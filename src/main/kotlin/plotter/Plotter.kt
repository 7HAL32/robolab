package plotter

import Planet
import javafx.geometry.Point2D
import javafx.scene.canvas.Canvas
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
        val canvas: Canvas
) {

    internal var scale = 1.0
    internal var translate = Point2D.ZERO

    private var planet = Planet.empty()

    private val drawer = DrawHelper(this)

    private var gridDrawer = GridDrawer(drawer)
    private var pointDrawer = PointDrawer(drawer)
    private var pathDrawer = PathDrawer(drawer)

    fun update(planet: Planet) = drawAfter {
        this.planet = planet
    }

    var showGrid: Boolean = true
        set(value) = drawAfter {
            field = value
        }

    var showGridNumber: Boolean = true
        set(value) = drawAfter {
            field = value
        }

    var editMode: Boolean = false
        set(value) = drawAfter {
            field = value
        }

    private var pointerEvent: PointerEvent = PointerEvent.empty(Point2D.ZERO)

    val isDirectionHighlighted: Boolean
        get() = pointerEvent.direction != null

    val isPathEditing: Boolean
        get() = pointDrawer is EditPointDrawer

    fun startPathEditing() {
        if (pointerEvent.point != null && pointerEvent.direction != null) {
            val editStart = Pair(pointerEvent.point!!, pointerEvent.direction!!)
            pointDrawer = EditPointDrawer(drawer, editStart)
            pathDrawer = EditPathDrawer(drawer, editStart)
        }
    }

    fun finishPathEditing(): Path? {
        return (pathDrawer as? EditPathDrawer)?.let {
            val result = it.getPath(pointerEvent)

            pointDrawer = PointDrawer(drawer)
            pathDrawer = PathDrawer(drawer)

            result
        }
    }

    var heightReduce: Double = 0.0
        set(value) = drawAfter {
            field = value
        }

    internal val width: Double
        get() = canvas.width

    internal val height: Double
        get() = canvas.height - heightReduce

    fun draw() {
        drawer.clear()

        gridDrawer.draw(planet, pointerEvent)
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

    fun resetScroll(point: Point) = drawAfter {
        translate = Point2D(width / 2, height * 2 / 3) - drawer.systemToReal(point.to2D(), Point2D.ZERO)
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


