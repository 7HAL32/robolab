package plotter

import Planet
import javafx.geometry.Point2D
import model.Direction
import model.Point

/**
 * @author lars
 */
open class PointDrawer(drawer: DrawHelper) : AbsDrawer(drawer) {

    override fun draw(planet: Planet, pointerEvent: PointerEvent, t: Double) {
        val cols = drawer.getVisibleCols()
        val rows = drawer.getVisibleRows()

        planet.paths.map { it.first }.let {
            it.flatMap {
                setOf(
                        Triple(it.startPoint, it.startDirection, it.weight != null),
                        Triple(it.endPoint, it.endDirection, it.weight != null)
                )
            }.groupBy { it.first }
                    .mapValues {
                        it.value.groupBy { it.second }.mapValues {
                            it.value.map { it.third }.contains(true)
                        }
                    }
        }.filter { (point, _) ->
            (cols.first <= point.x) && (point.x <= cols.second) && (rows.first <= point.y) && (point.y <= rows.second)
        }.forEach { (point, fromServerMap) ->
            drawPoint(planet, point, fromServerMap, pointerEvent)
        }
    }

    private fun drawPoint(planet: Planet, point: Point, directionFromServer: Map<Direction, Boolean>, pointerEvent: PointerEvent) {
        if (point == planet.target) {
            drawer.circle(point.to2D(), Plotter.POINT_RADIUS, Plotter.Companion.COLOR.TARGET)
        }

        if (point == planet.start) {
            drawer.line(point.to2D(), point.to2D() - (0.0 to 0.5), Plotter.Companion.COLOR.LINE)
        }


        directionFromServer.forEach { (direction, fromServer) ->
            drawer.line(point.to2D(), getLineStart(point, direction), if (fromServer) Plotter.Companion.COLOR.LINE else Plotter.Companion.COLOR.ROBOT)
        }

        if (pointerEvent.point == point) {
            pointerEvent.direction?.let {
                drawer.line(point.to2D(), getLineStart(point, it), Plotter.Companion.COLOR.HIGHLIGHT, Plotter.LineType.THICK)
            }
        }


        val background = when (point.getColor(planet.start, planet.startColor)) {
            Point.Color.RED -> Plotter.Companion.COLOR.RED
            Point.Color.BLUE -> Plotter.Companion.COLOR.BLUE
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
}