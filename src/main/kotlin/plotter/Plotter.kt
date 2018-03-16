package plotter

import PlanetListener
import javafx.geometry.Point2D
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import model.Direction
import model.Path
import model.Point
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * @author lars
 */

typealias DirectedPoint = Pair<Point, Set<Direction>> // TODO: data class

class Plotter(
        private val canvas: GraphicsContext
) : PlanetListener {

    private var scale = 1.0
    private var translate = Point2D(200.toDouble(), 300.toDouble())

    private var planetName = ""
    private var start: Point? = null
    private var paths = emptyList<Pair<Path, Set<PathAttributes>>>()
    private var target: Point? = null

    override fun onUpdate(
            planetName: String,
            start: Point,
            paths: List<Pair<Path, Set<PathAttributes>>>,
            target: Point?
    ) {
        this.planetName = planetName
        this.start = start
        this.paths = paths
        this.target = target

        draw()
    }

    val width: Double
        get() = canvas.canvas.width

    val height: Double
        get() = canvas.canvas.height

    fun draw() {
        clear()

        start?.let {
            printAllPoints(
                    paths.map { it.first },
                    it,
                    Point.Color.RED,
                    target
            )

            paths.forEach {
                printPath(it.first, it.second)
            }
        }
    }

    private fun drawAfter(block: () -> Unit) {
        block()
        draw()
    }

    fun scroll(d: Point2D) = drawAfter {
        translate += d
    }

    fun resetScroll(point: Point) = drawAfter {
        translate = Point2D(200.0, 300.0)
    }

    fun zoomIn() = drawAfter {
        if (scale < 1.0)
            scale += 0.05
        else
            scale = min(10.0, scale + 0.1)
    }

    fun zoomOut() = drawAfter {
        if (scale > 1.0)
            scale -= 0.1
        else
            scale = max(0.1, scale - 0.05)
    }

    fun zoomReset() = drawAfter {
        scale = 1.0
    }

    private fun clear() = canvas.clearRect(0.0, 0.0, width, height)

    private fun printAllPoints(paths: List<Path>, start: Point, startColor: Point.Color, target: Point?) {
        paths
                .flatMap {
                    setOf(
                            it.startPoint to it.startDirection,
                            it.endPoint to it.endDirection
                    )
                }
                .groupBy { it.first }
                .mapValues {
                    it.value
                            .map { it.second }
                            .toSet()
                }
                .forEach {
                    printPoint(
                            it.toPair(),
                            start,
                            startColor,
                            it.key == target
                    )
                }
    }

    private fun printPoint(directedPoint: DirectedPoint, start: Point, startColor: Point.Color, isTarget: Boolean) {
        if (isTarget) {
            drawCircle(directedPoint.first.to2D(), POINT_RADIUS, COLOR.TARGET)
        }

        directedPoint.second.forEach {
            drawLine(directedPoint.first.to2D(), getLineStart(directedPoint.first, it), COLOR.LINE)
        }
        if (directedPoint.first == start) {
            drawLine(directedPoint.first.to2D(), directedPoint.first.to2D() - (0.0 to 0.5))
        }

        val background = when (directedPoint.first.getColor(start, startColor)) {
            Point.Color.RED -> COLOR.RED
            Point.Color.BLUE -> COLOR.BLUE
            Point.Color.UNDEFINED -> COLOR.BACKGROUND
        }

        drawRect(
                directedPoint.first.to2D() - (POINT_SIZE / 2 to -POINT_SIZE / 2),
                Point2D(POINT_SIZE, POINT_SIZE),
                background,
                COLOR.LINE
        )
    }

    private fun getLineStart(point: Point, direction: Direction, shift: Double = POINT_SHIFT) =
            when (direction) {
                Direction.NORTH -> point.to2D() + (0.toDouble() to shift)
                Direction.EAST -> point.to2D() + (shift to 0.toDouble())
                Direction.SOUTH -> point.to2D() - (0.toDouble() to shift)
                Direction.WEST -> point.to2D() - (shift to 0.toDouble())
            }

    private fun printPath(path: Path, attributes: Set<PathAttributes>) = with(path) {
        when {
            startPoint == endPoint -> printSamePointPath(this@with, attributes)
            isOnSameLine() && isOppositeDirection() && isTowardsDirection() -> printStraightPath(this@with, attributes)
            else -> printCurvedPath(this@with, attributes)
        }
    }

    private fun getUsedPointSides(point: Point) =
            paths.map { it.first }
                    .flatMap {
                        setOf(
                                it.startPoint to it.startDirection,
                                it.endPoint to it.endDirection
                        )
                    }
                    .groupBy { it.first }
                    .mapValues {
                        it.value
                                .map { it.second }
                                .toSet()
                    }[point] ?: emptySet()


    private fun printStraightPath(path: Path, attributes: Set<PathAttributes>) = drawLine(
            getLineStart(path.startPoint, path.startDirection),
            getLineStart(path.endPoint, path.endDirection)
    )

    private fun printSamePointPath(path: Path, attributes: Set<PathAttributes>) = with(path) {
        val radius = 0.3
        val s = getLineStart(startPoint, startDirection, radius)
        val e = getLineStart(endPoint, endDirection, radius)

        drawLine(getLineStart(startPoint, startDirection), s, COLOR.LINE)
        drawLine(getLineStart(endPoint, endDirection), e, COLOR.LINE)


        when {
            isSameDirection() -> {
                // TODO
            }
            isOppositeDirection() -> {
                val sides = getUsedPointSides(path.startPoint)
                when (startDirection) {
                    Direction.NORTH, Direction.SOUTH -> {
                        val shift = if (Direction.WEST in sides) radius else -radius
                        val c1 = s + (shift to 0.0)
                        val c2 = e + (shift to 0.0)

                        drawArc(c1, radius, COLOR.LINE, if (startDirection == Direction.NORTH) 0.0 else 180.0, 180.0)
                        drawArc(c2, radius, COLOR.LINE, if (startDirection == Direction.SOUTH) 0.0 else 180.0, 180.0)
                        drawLine(c1 + (shift to 0.0), c2 + (shift to 0.0))
                    }
                    Direction.EAST, Direction.WEST -> {
                        val shift = if (Direction.SOUTH in sides) radius else -radius
                        val c1 = s + (0.0 to shift)
                        val c2 = e + (0.0 to shift)

                        drawArc(c1, radius, COLOR.LINE, if (startDirection == Direction.WEST) 90.0 else 270.0, 180.0)
                        drawArc(c2, radius, COLOR.LINE, if (startDirection == Direction.EAST) 90.0 else 270.0, 180.0)
                        drawLine(c1 + (0.0 to shift), c2 + (0.0 to shift))
                    }
                }
            }
            else -> {
                val p1 = Point2D(s.x, e.y)
                val p2 = Point2D(e.x, s.y)

                val center = if (startPoint.to2D().distance(p1) > startPoint.to2D().distance(p2)) p1 else p2
                val start = when {
                    isTowardsTopLeft() -> 0
                    isTowardsBottomLeft() -> 90
                    isTowardsBottomRight() -> 180
                    isTowardsTopRight() -> 270
                    else -> 0
                }

                drawArc(center, radius, COLOR.LINE, start.toDouble(), 270.0)
            }
        }
    }

    private fun printCurvedPath(path: Path, attributes: Set<PathAttributes>) {
        fun calcBezier(p0: Point2D, p1: Point2D, p2: Point2D, p3: Point2D, t: Double): Point2D {
            val h0 = -p0 + p1 * 3 - p2 * 3 + p3
            val h1 = p0 * 3 - p1 * 6 + p2 * 3
            val h2 = p0 * -3 + p1 * 3

            return h0 * t.pow(3) + h1 * t.pow(2) + h2 * t + p0
        }

        val s = getLineStart(path.startPoint, path.startDirection)
        val sd = getLineStart(path.startPoint, path.startDirection, 0.85)
        val ed = getLineStart(path.endPoint, path.endDirection, 0.85)
        val e = getLineStart(path.endPoint, path.endDirection)

        val step = 10.0 / (s.distance(e) * WIDTH_GRID * scale)
        var t = step

        canvas.beginPath()
        val h1 = transform(s)
        canvas.moveTo(h1.x, h1.y)

        while (t < 1.0) {
            val h2 = transform(calcBezier(s, sd, ed, e, t))
            canvas.lineTo(h2.x, h2.y)
            t += step
        }

        val h3 = transform(e)
        canvas.lineTo(h3.x, h3.y)
        canvas.stroke()
    }

    private fun drawLine(start: Point2D, end: Point2D, color: Color? = null) {
        if (color != null) // TODO: remove null
            canvas.stroke = color

        val s = transform(start)
        val e = transform(end)

        canvas.strokeLine(s.x, s.y, e.x, e.y)
    }

    private fun drawRect(bottomLeft: Point2D, size: Point2D, background: Color? = null, lineColor: Color? = null) {
        if (lineColor != null) // TODO: remove null
            canvas.stroke = lineColor
        if (background != null) // TODO: remove null
            canvas.fill = background

        val p = transform(bottomLeft)
        val s = size * (WIDTH_GRID * scale)

        canvas.fillRect(p.x, p.y, s.x, s.y)
        canvas.strokeRect(p.x, p.y, s.x, s.y)
    }

    private fun drawCircle(center: Point2D, radius: Double, background: Color? = null) {
        if (background != null)
            canvas.fill = background

        val p = transform(center - (radius to -radius))

        val r = radius * 2 * WIDTH_GRID * scale
        canvas.fillArc(p.x, p.y, r, r, 0.toDouble(), 360.toDouble(), ArcType.ROUND)
    }

    private fun drawArc(center: Point2D, radius: Double, lineColor: Color? = null, start: Double, extend: Double) {
        if (lineColor != null)
            canvas.stroke = lineColor

        val p = transform(center - (radius to -radius))

        val r = radius * 2 * WIDTH_GRID * scale
        canvas.strokeArc(p.x, p.y, r, r, start, extend, ArcType.OPEN)
    }

    private fun transform(point: Point2D) = Point2D(point.x, -point.y) * (WIDTH_GRID * scale) + translate

    companion object {
        const val WIDTH_GRID = 100

        const val POINT_SIZE = 0.22
        const val POINT_SHIFT = 0.28
        const val POINT_RADIUS = 0.25

        const val SIZE_LINE = 4
        const val SIZE_POINT_COLOR = 2
        const val SIZE_POINT_UNKOWN = 3

        private object COLOR {
            val RED = Color.web("#F44336")
            val BLUE = Color.web("#3F51B5")
            val LINE = Color.web("#263238")
            val GRID = Color.web("#ECEFF1")
            val BACKGROUND = Color.web("#FFFFFF")
            val ROBOT = Color.web("#FF9800")
            val TARGET = Color.web("#AED581")
        }
    }

}