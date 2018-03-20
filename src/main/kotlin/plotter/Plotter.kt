package plotter

import PlanetListener
import javafx.geometry.Point2D
import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import model.Direction
import model.Path
import model.Point
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.with

/**
 * @author lars
 */

class Plotter(
        private val canvas: GraphicsContext
) : PlanetListener {

    private var scale = 1.0
    private var translate = Point2D.ZERO

    private var planetName = ""
    private var start: Point? = null
    private var startColor: Point.Color = Point.Color.UNDEFINED
    private var paths = emptyList<Pair<Path, Set<PathAttributes>>>()
    private var target: Point? = null

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

    private var pointerEvent: PointerEvent = PointerEvent.NOTHING

    val isPointHighlighted: Boolean
        get() = pointerEvent.point != null

    val isDirectionHighlighted: Boolean
        get() = pointerEvent.direction != null

    private var editStart: Pair<Point, Direction>? = null

    val isPathEditing: Boolean
        get() = editStart != null

    fun startPathEditing() {
        if (pointerEvent.point != null && pointerEvent.direction != null)
            editStart = Pair(pointerEvent.point!!, pointerEvent.direction!!)
    }

    fun finishPathEditing(): Path? {
        val ret = editStart?.let {
            if (pointerEvent.point != null && pointerEvent.direction != null && (it.first != pointerEvent.point || it.second != pointerEvent.direction)) {
                Path(it.first, it.second, pointerEvent.point!!, pointerEvent.direction!!)
            } else {
                null
            }
        }
        editStart = null
        return ret
    }

    override fun onUpdate(
            planetName: String,
            start: Point,
            startColor: Point.Color,
            paths: List<Pair<Path, Set<PathAttributes>>>,
            target: Point?
    ) {
        if (this.planetName != planetName) {
            resetScroll(start)
        }

        this.planetName = planetName
        this.start = start
        this.startColor = startColor
        this.paths = paths
        this.target = target

        draw()
    }

    var heightReduce: Double = 0.0
        set(value) = drawAfter {
            field = value
        }

    private val width: Double
        get() = canvas.canvas.width

    private val height: Double
        get() = canvas.canvas.height - heightReduce

    fun draw() {
        clear()
        printGrid()


        start?.let {
            if (editMode)
                printVisiblePoints(it, startColor, target)

            printAllPoints(
                    paths.map { it.first },
                    it,
                    startColor,
                    target
            )

            paths.forEach {
                printPath(it.first, it.second)
            }

            if (isPathEditing && pointerEvent.point != null && pointerEvent.direction != null) {
                editStart?.let {
                    val p = Path(it.first, it.second, pointerEvent.point!!, pointerEvent.direction!!)
                    printPath(p, setOf(PathAttributes.EDITING))
                }
            }
        }
    }

    private fun <T> drawAfter(block: () -> T) {
        val h = block()
        draw()

        editStart?.let {
            if (h is Point2D) {
                canvas.stroke = COLOR.LINE

                val s = transform(getLineStart(it.first, it.second))
                val e: Point2D = h

                canvas.setLineDashes(4.0, 6.0)
                canvas.strokeLine(s.x, s.y, e.x, e.y)
                canvas.setLineDashes(0.0)
            }
        }
    }

    fun scrollBy(d: Point2D) = drawAfter {
        translate += d
    }

    fun resetScroll(point: Point) = drawAfter {
        translate = Point2D(width / 2, height * 2 / 3) - transform(point.to2D(), Point2D.ZERO)
        pointerEvent = PointerEvent.NOTHING
    }

    private fun mousePointToModelPoint(point: Point2D):Point2D {
        val p = (translate - point) * (-1.0 to 1.0)
        return Point2D(p.x / (WIDTH_GRID * scale), p.y / (WIDTH_GRID * scale))
    }

    fun zoomIn(zoomTo:Point2D = Point2D(width / 2, height / 2)) = drawAfter {
        val dataPoint = mousePointToModelPoint(zoomTo)
        if (scale < 1.0)
            scale += 0.05
        else
            scale = min(10.0, scale + 0.1)
        val newPoint = transform(dataPoint)

        translate -= (newPoint - zoomTo)
    }

    fun zoomOut(zoomTo:Point2D = Point2D(width / 2, height / 2)) = drawAfter {
        val dataPoint = mousePointToModelPoint(zoomTo)
        if (scale > 1.0)
            scale -= 0.1
        else
            scale = max(0.1, scale - 0.05)
        val newPoint = transform(dataPoint)
        translate -= (newPoint - zoomTo)
    }

    fun zoomReset() = drawAfter {
        scale = 1.0
        pointerEvent = PointerEvent.NOTHING
    }

    fun testPointer(point: Point2D) = drawAfter {
        val (x, y) = mousePointToModelPoint(point).toPair()

        val col = Math.round(x).toInt()
        val dx = Math.abs(x - col)
        val row = Math.round(y).toInt()
        val dy = Math.abs(y - row)

        pointerEvent = when {
            dx < POINT_SIZE / 2 && dy < POINT_SIZE / 2 -> PointerEvent(
                    Point(col, row),
                    null,
                    null
            )
            (dx < POINT_SIZE && dy < POINT_SHIFT) || (dx < POINT_SHIFT && dy < POINT_SIZE) -> PointerEvent(
                    Point(col, row),
                    when {
                        x - col > POINT_SIZE / 2 -> Direction.EAST
                        col - x > POINT_SIZE / 2 -> Direction.WEST
                        y - row > POINT_SIZE / 2 -> Direction.NORTH
                        row - y > POINT_SIZE / 2 -> Direction.SOUTH
                        else -> null
                    },
                    null
            )
            else -> PointerEvent.NOTHING
        }
        point
    }

    private fun clear() = canvas.clearRect(0.0, 0.0, width, height)

    private fun printGrid() {
        val topLeft = Point2D(translate.x, translate.y)
        val bottomRight = topLeft + (width to height)

        canvas.stroke = COLOR.GRID
        canvas.fill = COLOR.GRID_NUMBER
        val oldWidth = canvas.lineWidth
        canvas.lineWidth = 1.0

        canvas.textAlign = TextAlignment.CENTER
        canvas.textBaseline = VPos.CENTER
        canvas.font = Font.font(16.0)

        val gridWidth = WIDTH_GRID * scale
        val everyLine = gridWidth > canvas.font.size

        val rowOffset = (translate.y / (WIDTH_GRID * scale)).toInt()
        for (row in 0..Math.ceil((bottomRight.y - topLeft.y) / (WIDTH_GRID * scale)).toInt()) {
            val y = (row * WIDTH_GRID * scale) + translate.y % (WIDTH_GRID * scale)
            if (showGrid)
                canvas.strokeLine(0.0, y, width, y)
            if (showGridNumber && (y < height - 32.0) && (everyLine || (rowOffset - row) % 2 == 0))
                canvas.fillText((rowOffset - row).toString(), 24.0, y)
        }

        val colOffset = (translate.x / (WIDTH_GRID * scale)).toInt()
        for (col in 0..Math.ceil((bottomRight.x - topLeft.x) / (WIDTH_GRID * scale)).toInt()) {
            val x = (col * WIDTH_GRID * scale) + translate.x % (WIDTH_GRID * scale)
            if (showGrid)
                canvas.strokeLine(x, 0.0, x, height)
            if (showGridNumber && (x > 48.0) && (everyLine || (col - colOffset) % 2 == 0))
                canvas.fillText((col - colOffset).toString(), x, height - 15.0)
        }

        canvas.lineWidth = oldWidth
    }

    private fun printVisiblePoints(start: Point, startColor: Point.Color, target: Point?) {
        val topLeft = Point2D(translate.x, translate.y)
        val bottomRight = topLeft + (width to height)

        val rowOffset = (translate.y / (WIDTH_GRID * scale)).toInt()
        val colOffset = (translate.x / (WIDTH_GRID * scale)).toInt()
        for (row in -1..Math.ceil((bottomRight.y - topLeft.y) / (WIDTH_GRID * scale)).toInt()) {
            for (col in -1..Math.ceil((bottomRight.x - topLeft.x) / (WIDTH_GRID * scale)).toInt()) {
                val p = Point(col - colOffset, rowOffset - row)
                printPoint(DirectedPoint(p, emptySet()), start, startColor, target == p, false)
            }
        }
    }

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
                }.let {
                    val h = it.toMutableMap()
                    if (!h.containsKey(start)) {
                        h[start] = emptySet()
                    }
                    if (!h.containsKey(target) && target != null) {
                        h[target] = emptySet()
                    }
                    h.toMap()
                }
                .forEach {
                    printPoint(
                            DirectedPoint(it),
                            start,
                            startColor,
                            it.key == target
                    )
                }
    }

    private fun printPoint(directedPoint: DirectedPoint, start: Point, startColor: Point.Color, isTarget: Boolean, isUsed: Boolean = true) {
        if (isTarget) {
            drawCircle(directedPoint.point.to2D(), POINT_RADIUS, COLOR.TARGET)
        }

        if (directedPoint.point == start) {
            drawLine(directedPoint.point.to2D(), directedPoint.point.to2D() - (0.0 to 0.5), COLOR.LINE)
        }
        directedPoint.directions.forEach {
            drawLine(directedPoint.point.to2D(), getLineStart(directedPoint.point, it), COLOR.LINE)
        }

        if (pointerEvent.point == directedPoint.point) {
            pointerEvent.direction?.let {
                drawLine(directedPoint.point.to2D(), getLineStart(directedPoint.point, it), COLOR.HIGHLIGHT, LineType.THICK)
            }
        }
        if (editStart?.first == directedPoint.point) {
            editStart?.let {
                drawLine(directedPoint.point.to2D(), getLineStart(directedPoint.point, it.second), COLOR.HIGHLIGHT, LineType.THICK)
            }
        }

        val background = when (isUsed) {
            true -> when (directedPoint.point.getColor(start, startColor)) {
                Point.Color.RED -> COLOR.RED
                Point.Color.BLUE -> COLOR.BLUE
                Point.Color.UNDEFINED -> COLOR.BACKGROUND
            }
            false -> when (directedPoint.point.getColor(start, startColor)) {
                Point.Color.RED -> COLOR.RED_LIGHT
                Point.Color.BLUE -> COLOR.BLUE_LIGHT
                Point.Color.UNDEFINED -> COLOR.BACKGROUND
            }
        }


        drawRect(
                directedPoint.point.to2D() - (POINT_SIZE / 2 to -POINT_SIZE / 2),
                Point2D(POINT_SIZE, POINT_SIZE),
                background,
                if (pointerEvent.point == directedPoint.point && pointerEvent.direction == null) COLOR.HIGHLIGHT else COLOR.LINE,
                if (pointerEvent.point == directedPoint.point && pointerEvent.direction == null) LineType.THICK else LineType.NORMAL
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
            startPoint == endPoint -> printPathSamePoint(this@with, attributes)
            isOnSameLine() && isOppositeDirection() && isTowardsDirection() -> printPathStraight(this@with, attributes)
            else -> printPathCurved(this@with, attributes)
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

    private fun getLineColor(path: Path, attributes: Set<PathAttributes>): Color = when {
        attributes.contains(PathAttributes.HIGHLIGHTED) -> COLOR.HIGHLIGHT
        attributes.contains(PathAttributes.EDITING) -> COLOR.LINE_LIGHT
        path.weight == null -> COLOR.ROBOT
        else -> COLOR.LINE
    }

    private fun printPathStraight(path: Path, attributes: Set<PathAttributes>) {
        val start = getLineStart(path.startPoint, path.startDirection)
        val end = getLineStart(path.endPoint, path.endDirection)
        drawLine(
                start,
                end,
                getLineColor(path, attributes)
        )

        if (path.weight ?: 1 < 0) {
            val lineHalf = 0.1
            val s = (start + end) * 0.5
            val (x1, x2) = when (path.startDirection) {
                Direction.NORTH, Direction.SOUTH -> (s - (lineHalf to 0.0)) to (s + (lineHalf to 0.0))
                Direction.EAST, Direction.WEST -> (s - (0.0 to lineHalf)) to (s + (0.0 to lineHalf))
            }

            val oldSize = canvas.lineWidth
            canvas.lineWidth = 3.0
            drawLine(x1, x2, COLOR.BLOCKED)
            canvas.lineWidth = oldSize
        }
    }

    private fun radiusToShift(radius: Double): Double {
        return radius * Math.PI / 4
    }

    private fun printPathSamePoint(path: Path, attributes: Set<PathAttributes>) = with(path) {
        val radius = 0.3
        val s = getLineStart(startPoint, startDirection, radius)
        val e = getLineStart(endPoint, endDirection, radius)

        when {
            isSameDirection() -> {
                val lineHalf = 0.1
                drawLine(getLineStart(startPoint, startDirection), s, getLineColor(path, attributes))
                val (x1, x2) = when (path.startDirection) {
                    Direction.NORTH, Direction.SOUTH -> (s - (lineHalf to 0.0)) to (s + (lineHalf to 0.0))
                    Direction.EAST, Direction.WEST -> (s - (0.0 to lineHalf)) to (s + (0.0 to lineHalf))
                }

                val oldSize = canvas.lineWidth
                canvas.lineWidth = 3.0
                drawLine(x1, x2, COLOR.BLOCKED)
                canvas.lineWidth = oldSize
            }
            isOppositeDirection() -> {
                drawLine(getLineStart(startPoint, startDirection), s, getLineColor(path, attributes))
                drawLine(getLineStart(endPoint, endDirection), e, getLineColor(path, attributes))
                val sides = getUsedPointSides(path.startPoint)
                when (startDirection) {
                    Direction.NORTH, Direction.SOUTH -> {
                        val shift = if (Direction.WEST in sides) radius else -radius
                        val c1 = s + (shift to 0.0)
                        val c2 = e + (shift to 0.0)

                        drawArc(c1, radius, getLineColor(path, attributes), if (startDirection == Direction.NORTH) 0.0 else 180.0, 180.0)
                        drawArc(c2, radius, getLineColor(path, attributes), if (startDirection == Direction.SOUTH) 0.0 else 180.0, 180.0)
                        val start = c1 + (shift to 0.0)
                        val end = c2 + (shift to 0.0)
                        drawLine(start, end, getLineColor(path, attributes))

                        if (path.weight ?: 1 < 0) {
                            val lineHalf = 0.1
                            val s1 = (start + end) * 0.5
                            val (x1, x2) = (s1 - (lineHalf to 0.0)) to (s1 + (lineHalf to 0.0))

                            val oldSize = canvas.lineWidth
                            canvas.lineWidth = 3.0
                            drawLine(x1, x2, COLOR.BLOCKED)
                            canvas.lineWidth = oldSize
                        }
                    }
                    Direction.EAST, Direction.WEST -> {
                        val shift = if (Direction.SOUTH in sides) radius else -radius
                        val c1 = s + (0.0 to shift)
                        val c2 = e + (0.0 to shift)

                        drawArc(c1, radius, getLineColor(path, attributes), if (startDirection == Direction.WEST) 90.0 else 270.0, 180.0)
                        drawArc(c2, radius, getLineColor(path, attributes), if (startDirection == Direction.EAST) 90.0 else 270.0, 180.0)
                        val start = c1 + (0.0 to shift)
                        val end = c2 + (0.0 to shift)
                        drawLine(c1 + (0.0 to shift), c2 + (0.0 to shift), getLineColor(path, attributes))

                        if (path.weight ?: 1 < 0) {
                            val lineHalf = 0.1
                            val s1 = (start + end) * 0.5
                            val (x1, x2) = (s1 - (0.0 to lineHalf)) to (s1 + (0.0 to lineHalf))

                            val oldSize = canvas.lineWidth
                            canvas.lineWidth = 3.0
                            drawLine(x1, x2, COLOR.BLOCKED)
                            canvas.lineWidth = oldSize
                        }
                    }
                }
            }
            else -> {
                drawLine(getLineStart(startPoint, startDirection), s, getLineColor(path, attributes))
                drawLine(getLineStart(endPoint, endDirection), e, getLineColor(path, attributes))
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

                drawArc(center, radius, getLineColor(path, attributes), start.toDouble(), 270.0)

                if (path.weight ?: 1 < 0) {
                    val lineHalf = radiusToShift(0.1)
                    val rs = radiusToShift(radius)
                    val c = when (start) {
                        0 -> center + (-rs to rs)
                        90 -> center + (-rs to -rs)
                        180 -> center + (rs to -rs)
                        else -> center + (rs to rs)
                    }
                    val (x1, x2) = when (start) {
                        0, 180 -> (c - (-lineHalf to lineHalf)) to (c + (-lineHalf to lineHalf))
                        else -> (c - (lineHalf to lineHalf)) to (c + (lineHalf to lineHalf))
                    }

                    val oldSize = canvas.lineWidth
                    canvas.lineWidth = 3.0
                    drawLine(x1, x2, COLOR.BLOCKED)
                    canvas.lineWidth = oldSize
                }
            }
        }
    }

    private fun printPathCurved(path: Path, attributes: Set<PathAttributes>) {
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

        canvas.stroke = getLineColor(path, attributes)
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


        if (path.weight ?: 1 < 0) {
            val lineHalf = 0.1
            val c = calcBezier(s, sd, ed, e, 0.5)
            val dir = (calcBezier(s, sd, ed, e, 0.49) - calcBezier(s, sd, ed, e, 0.51)).let {
                Point2D(it.y, -it.x).normalize()
            } * lineHalf
            val (x1, x2) = when (path.startDirection) {
                Direction.NORTH, Direction.SOUTH -> (c - (lineHalf to 0.0)) to (c + (lineHalf to 0.0))
                Direction.EAST, Direction.WEST -> (c - dir) to (c + dir)
            }

            val oldSize = canvas.lineWidth
            canvas.lineWidth = 3.0
            drawLine(x1, x2, COLOR.BLOCKED)
            canvas.lineWidth = oldSize
        }
    }

    private fun drawLine(start: Point2D, end: Point2D, lineColor: Color, lineType: LineType = LineType.NORMAL) {
        canvas.stroke = lineColor

        val s = transform(start)
        val e = transform(end)

        val oldSize = canvas.lineWidth
        if (lineType == LineType.THICK)
            canvas.lineWidth = 4.0
        canvas.strokeLine(s.x, s.y, e.x, e.y)
        canvas.lineWidth = oldSize
    }

    private fun drawRect(bottomLeft: Point2D, size: Point2D, background: Color, lineColor: Color, lineType: LineType = LineType.NORMAL) {
        canvas.stroke = lineColor
        canvas.fill = background

        val p = transform(bottomLeft)
        val s = size * (WIDTH_GRID * scale)

        canvas.fillRect(p.x, p.y, s.x, s.y)

        val oldSize = canvas.lineWidth
        if (lineType == LineType.THICK)
            canvas.lineWidth = 4.0
        canvas.strokeRect(p.x, p.y, s.x, s.y)
        canvas.lineWidth = oldSize
    }

    private fun drawCircle(center: Point2D, radius: Double, background: Color) {
        canvas.fill = background

        val p = transform(center - (radius to -radius))

        val r = radius * 2 * WIDTH_GRID * scale
        canvas.fillArc(p.x, p.y, r, r, 0.toDouble(), 360.toDouble(), ArcType.ROUND)
    }

    private fun drawArc(center: Point2D, radius: Double, lineColor: Color, start: Double, extend: Double) {
        canvas.stroke = lineColor

        val p = transform(center - (radius to -radius))

        val r = radius * 2 * WIDTH_GRID * scale
        canvas.strokeArc(p.x, p.y, r, r, start, extend, ArcType.OPEN)
    }

    private fun transform(point: Point2D, trans: Point2D = translate) = Point2D(point.x, -point.y) * (WIDTH_GRID * scale) + trans

    companion object {
        const val WIDTH_GRID = 100

        const val POINT_SIZE = 0.22
        const val POINT_SHIFT = 0.28
        const val POINT_RADIUS = 0.25

        private object COLOR {
            val RED = Color.web("#F44336")
            val RED_LIGHT = Color.web("#FFEBEE")
            val BLUE = Color.web("#3F51B5")
            val BLUE_LIGHT = Color.web("#E8EAF6")
            val LINE = Color.web("#263238")
            val LINE_LIGHT = Color.web("#607D8B")
            val GRID = Color.web("#E0E0E0")
            val GRID_NUMBER = Color.web("#C0C0C0")
            val BACKGROUND = Color.web("#FFFFFF")
            val ROBOT = Color.web("#FF9800")
            val TARGET = Color.web("#AED581")
            val BLOCKED = Color.web("#F44336")
            val HIGHLIGHT = Color.web("#009688")
        }
    }

    data class PointerEvent(
            val point: Point?,
            val direction: Direction?,
            val path: Path?
    ) {
        companion object {
            val NOTHING: PointerEvent
                get() = PointerEvent(null, null, null)
        }
    }

    enum class LineType {
        NORMAL, THICK
    }
}