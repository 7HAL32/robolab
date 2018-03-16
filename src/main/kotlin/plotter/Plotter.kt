package plotter

import PlanetListener
import javafx.geometry.Point2D
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import model.Direction
import model.Path
import model.Point

/**
 * @author lars
 */

typealias DirectedPoint = Pair<Point, Set<Direction>>

class Plotter(
        private val canvas: GraphicsContext
) : PlanetListener {

    private var scale: Double = 1.0
    private var translate: Point2D = Point2D(200.toDouble(), 300.toDouble())

    private var planetName: String = ""
    private var start: Point? = null
    private var paths: List<Pair<Path, Set<PathAttributes>>> = ArrayList()
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
        } ?: return

        paths.forEach {
            printPath(it.first, it.second)
        }
    }

    fun scroll(d: Point2D) {
        translate = translate.add(d)
        draw()
    }

    fun resetScroll(point: Point) {
        translate = Point2D(200.toDouble(), 300.toDouble())
        draw()
    }

    fun zoomIn() {
        if (scale < 1.0)
            scale += 0.05
        else
            scale = Math.min(10.0, scale + 0.1)
        draw()
    }

    fun zoomOut() {
        if (scale > 1.0)
            scale -= 0.1
        else
            scale = Math.max(0.1, scale - 0.05)
        draw()
    }

    fun zoomReset() {
        scale = 1.0
        draw()
    }

    private fun clear() {
        canvas.clearRect(0.toDouble(), 0.toDouble(), width, height)
    }

    private fun printAllPoints(paths: List<Path>, start: Point, startColor: Point.Color, target: Point?) {
        paths
                .flatMap {
                    setOf(
                            Pair(it.startPoint, it.startDirection),
                            Pair(it.endPoint, it.endDirection)
                    )
                }
                .groupBy { it.first }
                .mapValues {
                    it.value
                            .map { it.second }
                            .toSet()
                }
                .forEach({
                    printPoint(it.toPair(), start, startColor, it.key == target)
                })
    }

    private fun printPoint(directedPoint: DirectedPoint, start: Point, startColor: Point.Color, isTarget: Boolean) {
        if (isTarget)
            drawCircle(directedPoint.first.to2D(), POINT_RADIUS, COLOR.TARGET)

        directedPoint.second.forEach {
            drawLine(directedPoint.first.to2D(), getLineStart(directedPoint.first, it), COLOR.LINE)
        }
        if (directedPoint.first == start)
            drawLine(directedPoint.first.to2D(), directedPoint.first.to2D().subtract(0.0, 0.5))

        val background = when (directedPoint.first.getColor(start, startColor)) {
            Point.Color.RED -> COLOR.RED
            Point.Color.BLUE -> COLOR.BLUE
            Point.Color.UNDEFINED -> COLOR.BACKGROUND
        }

        drawRect(directedPoint.first.to2D().subtract(Point2D(POINT_SIZE / 2, -POINT_SIZE / 2)), Point2D(POINT_SIZE, POINT_SIZE), background = background, lineColor = COLOR.LINE)
    }

    private fun getLineStart(point: Point, direction: Direction, shift: Double = POINT_SHIFT): Point2D {
        return when (direction) {
            Direction.NORTH -> point.to2D().add(0.toDouble(), shift)
            Direction.EAST -> point.to2D().add(shift, 0.toDouble())
            Direction.SOUTH -> point.to2D().subtract(0.toDouble(), shift)
            Direction.WEST -> point.to2D().subtract(shift, 0.toDouble())
        }
    }

    private fun printPath(path: Path, attributes: Set<PathAttributes>) {
        if (path.startPoint == path.endPoint) {
            printSamePointPath(path, attributes)
        } else if (path.isOnSameLine() && path.isOppositeDirection() && path.isTowardsDirection()) {
            printStraightPath(path, attributes)
        } else {
            printCurvedPath(path, attributes)
        }
    }

    private fun getUsedPointSides(point: Point): Set<Direction> =
            paths.map { it.first }
                    .flatMap {
                        setOf(
                                Pair(it.startPoint, it.startDirection),
                                Pair(it.endPoint, it.endDirection)
                        )
                    }
                    .groupBy { it.first }
                    .mapValues {
                        it.value
                                .map { it.second }
                                .toSet()
                    }.getOrDefault(point, HashSet())


    private fun printStraightPath(path: Path, attributes: Set<PathAttributes>) {
        drawLine(getLineStart(path.startPoint, path.startDirection), getLineStart(path.endPoint, path.endDirection))
    }

    private fun printSamePointPath(path: Path, attributes: Set<PathAttributes>) {
        val radius = 0.3
        val s = getLineStart(path.startPoint, path.startDirection, radius)
        val e = getLineStart(path.endPoint, path.endDirection, radius)

        drawLine(getLineStart(path.startPoint, path.startDirection), s, COLOR.LINE)
        drawLine(getLineStart(path.endPoint, path.endDirection), e, COLOR.LINE)

        when {
            path.isSameDirection() -> {

            }
            path.isOppositeDirection() -> {
                val sides = getUsedPointSides(path.startPoint)
                when (path.startDirection) {
                    Direction.NORTH, Direction.SOUTH -> {
                        val shift = if (Direction.WEST in sides) radius else -radius
                        val c1 = s.add(shift, 0.0)
                        val c2 = e.add(shift, 0.0)

                        drawArc(c1, radius, COLOR.LINE, if (path.startDirection == Direction.NORTH) 0.0 else 180.0, 180.0)
                        drawArc(c2, radius, COLOR.LINE, if (path.startDirection == Direction.SOUTH) 0.0 else 180.0, 180.0)
                        drawLine(c1.add(shift, 0.0), c2.add(shift, 0.0))
                    }
                    Direction.EAST, Direction.WEST -> {
                        val shift = if (Direction.SOUTH in sides) radius else -radius
                        val c1 = s.add(0.0, shift)
                        val c2 = e.add(0.0, shift)

                        drawArc(c1, radius, COLOR.LINE, if (path.startDirection == Direction.WEST) 90.0 else 270.0, 180.0)
                        drawArc(c2, radius, COLOR.LINE, if (path.startDirection == Direction.EAST) 90.0 else 270.0, 180.0)
                        drawLine(c1.add(0.0, shift), c2.add(0.0, shift))
                    }
                }
            }
            else -> {
                val p1 = Point2D(s.x, e.y)
                val p2 = Point2D(e.x, s.y)

                val center = if (path.startPoint.to2D().distance(p1) > path.startPoint.to2D().distance(p2)) p1 else p2
                val start = when {
                    path.isTowardsTopLeft() -> 0
                    path.isTowardsBottomLeft() -> 90
                    path.isTowardsBottomRight() -> 180
                    path.isTowardsTopRight() -> 270
                    else -> 0
                }

                drawArc(center, radius, COLOR.LINE, start.toDouble(), 270.0)
            }
        }
    }

    private fun printCurvedPath(path: Path, attributes: Set<PathAttributes>) {
        fun calcBezier(p0: Point2D, p1: Point2D, p2: Point2D, p3: Point2D, t: Double): Point2D {
            val h0 = p0.multiply(-1.0).add(p1.multiply(3.0)).subtract(p2.multiply(3.0)).add(p3)
            val h1 = p0.multiply(3.0).subtract(p1.multiply(6.0)).add(p2.multiply(3.0))
            val h2 = p0.multiply(-3.0).add(p1.multiply(3.0))

            return h0.multiply(t * t * t).add(h1.multiply(t * t)).add(h2.multiply(t)).add(p0)
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
        if (color != null)
            canvas.stroke = color

        val s = transform(start)
        val e = transform(end)

        canvas.strokeLine(s.x, s.y, e.x, e.y)
    }

    private fun drawRect(bottomLeft: Point2D, size: Point2D, background: Color? = null, lineColor: Color? = null) {
        if (lineColor != null)
            canvas.stroke = lineColor
        if (background != null)
            canvas.fill = background

        val p = transform(bottomLeft)
        val s = size.multiply(WIDTH_GRID * scale)

        canvas.fillRect(p.x, p.y, s.x, s.y)
        canvas.strokeRect(p.x, p.y, s.x, s.y)
    }

    private fun drawCircle(center: Point2D, radius: Double, background: Color? = null) {
        if (background != null)
            canvas.fill = background

        val p = transform(center.subtract(radius, -radius))

        val r = radius * 2 * WIDTH_GRID * scale
        canvas.fillArc(p.x, p.y, r, r, 0.toDouble(), 360.toDouble(), ArcType.ROUND)
    }

    private fun drawArc(center: Point2D, radius: Double, lineColor: Color? = null, start: Double, extend: Double) {
        if (lineColor != null)
            canvas.stroke = lineColor

        val p = transform(center.subtract(radius, -radius))

        val r = radius * 2 * WIDTH_GRID * scale
        canvas.strokeArc(p.x, p.y, r, r, start, extend, ArcType.OPEN)
    }

    private fun transform(point: Point2D): Point2D {
        return Point2D(point.x, -1 * point.y).multiply(WIDTH_GRID * scale).add(translate)
    }

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