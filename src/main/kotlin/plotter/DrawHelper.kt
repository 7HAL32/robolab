package plotter

import javafx.geometry.Point2D
import javafx.geometry.VPos
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment

/**
 * @author lars
 */
class DrawHelper(
        private val plotter: Plotter
) {
    private val canvas = plotter.canvas.graphicsContext2D

    fun clear() = canvas.clearRect(plotter.widthReduce / 2, 0.0, plotter.width + plotter.widthReduce / 2, plotter.height)

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
            canvas.lineWidth = 4.0
        canvas.strokeLine(s.x, s.y, e.x, e.y)
        canvas.lineWidth = oldSize
    }

    fun line(points: List<Point2D>, lineColor: Color, lineType: Plotter.LineType = Plotter.LineType.NORMAL) {
        canvas.stroke = lineColor

        val oldSize = canvas.lineWidth
        if (lineType == Plotter.LineType.THICK)
            canvas.lineWidth = 4.0


        canvas.beginPath()
        val h1 = systemToReal(points.first())
        canvas.moveTo(h1.x, h1.y)

        points.stream().skip(1).map {
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
            canvas.lineWidth = 4.0
        canvas.strokeRect(p.x, p.y, s.x, s.y)
        canvas.lineWidth = oldSize
    }

    fun circle(center: Point2D, radius: Double, background: Color) {
        canvas.fill = background

        val p = systemToReal(center - (radius to -radius))
        val r = radius * 2 * Plotter.WIDTH_GRID * plotter.scale

        canvas.fillArc(p.x, p.y, r, r, 0.toDouble(), 360.toDouble(), ArcType.ROUND)
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


    fun hLine(row: Double, color: Color) {
        val point = systemToReal(Point2D(0.0, row))
        canvas.stroke = color
        canvas.strokeLine(plotter.widthReduce / 2, point.y, plotter.width+ plotter.widthReduce/2, point.y)
    }

    fun vLine(col: Double, color: Color) {
        val point = systemToReal(Point2D(col, 0.0))
        canvas.stroke = color
        canvas.strokeLine(point.x, 0.0, point.x, plotter.height)
    }

    fun fixedNumber(number: Int, position: Point2D, color: Color, fontSize: Double) {
        canvas.fill = color
        canvas.textAlign = TextAlignment.CENTER
        canvas.textBaseline = VPos.CENTER
        canvas.font = Font.font(fontSize)

        canvas.fillText(number.toString(), position.x + plotter.widthReduce / 2, position.y)
    }


    fun getVisibleRows(): Pair<Int, Int> = Math.floor(realToSystem(Point2D(0.0, plotter.height)).y).toInt() to
            Math.ceil(realToSystem(Point2D(0.0, 0.0)).y).toInt()

    fun getVisibleCols(): Pair<Int, Int> = Math.floor(realToSystem(Point2D(plotter.widthReduce / 2, 0.0)).x).toInt() to
            Math.ceil(realToSystem(Point2D(plotter.width + plotter.widthReduce / 2, 0.0)).x).toInt()

    val gridWidth: Double
        get() = Plotter.WIDTH_GRID * plotter.scale

    val height: Double
        get() = plotter.height

    val width: Double
        get() = plotter.width

    fun systemToReal(point: Point2D, translate: Point2D = plotter.translate): Point2D = Point2D(
            point.x,
            -point.y
    ) * (Plotter.WIDTH_GRID * plotter.scale) + translate + Point2D(plotter.widthReduce / 2, 0.0)


    fun realToSystem(point: Point2D, translate: Point2D = plotter.translate): Point2D = (Point2D(
            (point.x - translate.x),
            (translate.y - point.y)
    ) - Point2D(plotter.widthReduce / 2, 0.0)) * (1 / (Plotter.WIDTH_GRID * plotter.scale))
}