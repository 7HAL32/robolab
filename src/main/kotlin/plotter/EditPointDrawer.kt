package plotter

import Planet
import javafx.geometry.Point2D
import model.Direction
import model.Point

/**
 * @author lars
 */
class EditPointDrawer(drawer: DrawHelper) : PointDrawer(drawer) {

    var editStart: Pair<Point, Direction>? = null

    override fun draw(planet: Planet, pointerEvent: PointerEvent) {
        val cols = drawer.getVisibleCols()
        val rows = drawer.getVisibleRows()

        (cols.first..cols.second).forEach { x ->
            (rows.first..rows.second).forEach { y ->
                drawBackgroundPoint(planet, Point(x, y), pointerEvent)
            }
        }
        super.draw(planet, pointerEvent)
    }


    private fun drawBackgroundPoint(planet: Planet, point: Point, pointerEvent: PointerEvent) {
        if (pointerEvent.point == point) {
            pointerEvent.direction?.let {
                drawer.line(point.to2D(), getLineStart(point, it), Plotter.Companion.COLOR.HIGHLIGHT, Plotter.LineType.THICK)
            }
        }

        editStart?.let {
            if (it.first == point)
                drawer.line(point.to2D(), getLineStart(point, it.second), Plotter.Companion.COLOR.HIGHLIGHT, Plotter.LineType.THICK)
        }

        val background = when (point.getColor(planet.start, planet.startColor)) {
            Point.Color.RED -> Plotter.Companion.COLOR.RED_LIGHT
            Point.Color.BLUE -> Plotter.Companion.COLOR.BLUE_LIGHT
            Point.Color.UNDEFINED -> Plotter.Companion.COLOR.BACKGROUND
        }

        drawer.rect(
                point.to2D() - (Plotter.POINT_SIZE / 2 to -Plotter.POINT_SIZE / 2),
                Point2D(Plotter.POINT_SIZE, Plotter.POINT_SIZE),
                background,
                if (pointerEvent.point == point && pointerEvent.direction == null) Plotter.Companion.COLOR.HIGHLIGHT else Plotter.Companion.COLOR.LINE,
                if (pointerEvent.point == point && pointerEvent.direction == null) Plotter.LineType.THICK else Plotter.LineType.NORMAL
        )
    }

    fun isPathEditing(): Boolean = editStart != null
}