package plotter

import javafx.geometry.Point2D
import javafx.geometry.VPos
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author lars
 */
class DrawHelper(
        private val plotter: Plotter
) {
    private val canvas = plotter.canvas.graphicsContext2D

    fun clear() = canvas.clearRect(0.0, 0.0, width, height)

    fun dashed(from: Point2D, to: Point2D, color: Color) {
        val start = systemToReal(from)
        val end = systemToReal(to)

        canvas.stroke = color
        canvas.setLineDashes(4.0, 6.0)
        canvas.strokeLine(start.x, start.y, end.x, end.y)
        canvas.setLineDashes(0.0)
    }


    fun line(start: Point2D, end: Point2D, lineColor: Color, lineType: Plotter.LineType = Plotter.LineType.NORMAL) {
        canvas.stroke = lineColor

        val s = systemToReal(start)
        val e = systemToReal(end)

        val oldSize = canvas.lineWidth
        if (lineType == Plotter.LineType.THICK)
            canvas.lineWidth = THICK_LINE
        canvas.strokeLine(s.x, s.y, e.x, e.y)
        canvas.lineWidth = oldSize
    }

    fun line(points: List<Point2D>, lineColor: Color, lineType: Plotter.LineType = Plotter.LineType.NORMAL) {
        canvas.stroke = lineColor

        val oldSize = canvas.lineWidth
        if (lineType == Plotter.LineType.THICK)
            canvas.lineWidth = THICK_LINE


        canvas.beginPath()
        val h1 = systemToReal(points.first())
        canvas.moveTo(h1.x, h1.y)

        points.drop(1).map {
            systemToReal(it)
        }.forEach {
            canvas.lineTo(it.x, it.y)
        }

        canvas.stroke()

        canvas.lineWidth = oldSize
    }

    fun rect(bottomLeft: Point2D, size: Point2D, background: Color, lineColor: Color, lineType: Plotter.LineType = Plotter.LineType.NORMAL) {
        canvas.stroke = lineColor
        canvas.fill = background

        val p = systemToReal(bottomLeft)
        val s = size * (Plotter.WIDTH_GRID * plotter.scale)

        canvas.fillRect(p.x, p.y, s.x, s.y)
        val oldSize = canvas.lineWidth
        if (lineType == Plotter.LineType.THICK)
            canvas.lineWidth = THICK_LINE
        canvas.strokeRect(p.x, p.y, s.x, s.y)
        canvas.lineWidth = oldSize
    }

    fun circle(center: Point2D, radius: Double, background: Color) {
        canvas.fill = background

        val p = systemToReal(center - (radius to -radius))
        val r = radius * 2 * Plotter.WIDTH_GRID * plotter.scale

        canvas.fillArc(p.x, p.y, r, r, 0.toDouble(), 360.toDouble(), ArcType.CHORD)
    }

    fun circleOutline(center: Point2D, radius: Double, lineColor: Color, lineType: Plotter.LineType = Plotter.LineType.NORMAL) {
        canvas.stroke = lineColor

        val p = systemToReal(center - (radius to -radius))
        val r = radius * 2 * Plotter.WIDTH_GRID * plotter.scale

        val oldSize = canvas.lineWidth
        if (lineType == Plotter.LineType.THICK)
            canvas.lineWidth = THICK_LINE
        canvas.strokeArc(p.x, p.y, r, r, 0.toDouble(), 360.toDouble(), ArcType.CHORD)
        canvas.lineWidth = oldSize
    }

    fun arc(center: Point2D, radius: Double, lineColor: Color, start: Double, extend: Double) {
        canvas.stroke = lineColor

        val p = systemToReal(center - (radius to -radius))
        val r = radius * 2 * Plotter.WIDTH_GRID * plotter.scale

        canvas.strokeArc(p.x, p.y, r, r, start, extend, ArcType.OPEN)
    }

    fun number(number: Int, position: Point2D, color: Color, fontSize: Double) {
        canvas.fill = color
        canvas.textAlign = TextAlignment.CENTER
        canvas.textBaseline = VPos.CENTER
        canvas.font = Font.font(fontSize)

        val center = systemToReal(position)
        canvas.fillText(number.toString(), center.x, center.y)
    }

    fun arrow(position: Point2D, heading: Double, color: Color) {
        val top = systemToReal(position + (Point2D(sin(heading), cos(heading)) * Plotter.ARROW_SIZE))
        val left = systemToReal(position + (Point2D(sin(heading + 0.8 * PI), cos(heading + 0.8 * PI)) * Plotter.ARROW_SIZE))
        val bottom = systemToReal(position + (Point2D(sin(heading + PI), cos(heading + PI)) * (Plotter.ARROW_SIZE / 3)))
        val right = systemToReal(position + (Point2D(sin(heading - 0.8 * PI), cos(heading - 0.8 * PI)) * Plotter.ARROW_SIZE))

        val points = listOf(top, left, bottom, right)

        canvas.fill = color
        canvas.fillPolygon(
                points.map { it.x }.toDoubleArray(),
                points.map { it.y }.toDoubleArray(),
                points.size
        )
    }


    fun hLine(row: Double, color: Color) {
        val point = systemToReal(Point2D(0.0, row))
        canvas.stroke = color
        canvas.strokeLine(0.0, point.y, width, point.y)
    }

    fun vLine(col: Double, color: Color) {
        val point = systemToReal(Point2D(col, 0.0))
        canvas.stroke = color
        canvas.strokeLine(point.x, 0.0, point.x, height)
    }

    fun colNumber(col: Int, color: Color, fontSize: Double) {
        canvas.fill = color
        canvas.textAlign = TextAlignment.CENTER
        canvas.textBaseline = VPos.CENTER
        canvas.font = Font.font(fontSize)

        val position = systemToReal(Point2D(col.toDouble(), 0.0))

        if (position.x > fontSize * 3)
            canvas.fillText(col.toString(), position.x, height - fontSize * 1.5)
    }

    fun rowNumber(row: Int, color: Color, fontSize: Double) {
        canvas.fill = color
        canvas.textAlign = TextAlignment.CENTER
        canvas.textBaseline = VPos.CENTER
        canvas.font = Font.font(fontSize)

        val position = systemToReal(Point2D(0.0, row.toDouble()))

        if (position.y < height - fontSize * 3)
            canvas.fillText(row.toString(), fontSize * 1.5, position.y)
    }


    fun getVisibleRows(): Pair<Int, Int> = Math.floor(realToSystem(Point2D(0.0, height)).y).toInt() to
            Math.ceil(realToSystem(Point2D(0.0, 0.0)).y).toInt()

    fun getVisibleCols(): Pair<Int, Int> = Math.floor(realToSystem(Point2D(0.0, 0.0)).x).toInt() to
            Math.ceil(realToSystem(Point2D(width, 0.0)).x).toInt()

    val gridWidth: Double
        get() = Plotter.WIDTH_GRID * plotter.scale

    val height: Double
        get() = plotter.height

    val width: Double
        get() = plotter.width

    fun systemToReal(point: Point2D, translate: Point2D = plotter.translate): Point2D = Point2D(
            point.x,
            -point.y
    ) * (Plotter.WIDTH_GRID * plotter.scale) + translate


    fun realToSystem(point: Point2D, translate: Point2D = plotter.translate): Point2D = (Point2D(
            (point.x - translate.x),
            (translate.y - point.y)
    )) * (1 / (Plotter.WIDTH_GRID * plotter.scale))

    companion object {
        const val THICK_LINE = 4.0
    }
}