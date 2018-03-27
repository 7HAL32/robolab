package plotter.drawer

import Planet
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import model.Direction
import model.Point
import plotter.*
import kotlin.math.PI

/**
 * @author lars
 */
open class PointDrawer(drawer: DrawHelper) : AbsDrawer(drawer) {

    data class Asdf(
            val point: Point,
            val direction: Direction,
            val fromServer: Boolean,
            val isHighlight: Boolean
    )

    override fun draw(planet: Planet, pointerEvent: PointerEvent, t: Double) {
        val cols = drawer.getVisibleCols()
        val rows = drawer.getVisibleRows()

        planet.paths.let {
            it.flatMap {
                setOf(
                        Asdf(it.first.startPoint, it.first.startDirection, it.first.weight != null, it.second.contains(PathAttributes.HIGHLIGHTED)),
                        Asdf(it.first.endPoint, it.first.endDirection, it.first.weight != null, it.second.contains(PathAttributes.HIGHLIGHTED))
                )
            }.groupBy { it.point }
                    .mapValues {
                        it.value.groupBy { it.direction }.mapValues {
                            if (it.value.map { it.isHighlight }.contains(true)) {
                                Pair(Plotter.Companion.COLOR.HIGHLIGHT, Plotter.LineType.THICK)
                            } else {
                                Pair(
                                        if (it.value.map { it.fromServer }.contains(true)) {
                                            Plotter.Companion.COLOR.LINE
                                        } else {
                                            Plotter.Companion.COLOR.ROBOT
                                        },
                                        Plotter.LineType.NORMAL
                                )
                            }
                        }
                    }
        }.filter { (point, _) ->
            (cols.first <= point.x) && (point.x <= cols.second) && (rows.first <= point.y) && (point.y <= rows.second)
        }.forEach { (point, fromServerMap) ->
            drawPoint(planet, point, fromServerMap, pointerEvent)
        }
    }

    private fun drawPoint(planet: Planet, point: Point, directionFromServer: Map<Direction, Pair<Color, Plotter.LineType>>, pointerEvent: PointerEvent) {
        if (point == planet.target) {
            drawer.circle(point.to2D(), Plotter.POINT_RADIUS, Plotter.Companion.COLOR.TARGET)
        }

        if (point == planet.start) {
            drawer.line(point.to2D(), point.to2D() - (0.0 to 0.5), Plotter.Companion.COLOR.LINE)
        }


        directionFromServer.forEach { (direction, style) ->
            drawer.line(point.to2D(), getLineStart(point, direction), style.first, style.second)
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