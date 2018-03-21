package plotter

import Planet
import javafx.geometry.Point2D
import model.Point

/**
 * @author lars
 */
open class PointDrawer(drawer: DrawHelper) : AbsDrawer(drawer) {

    override fun draw(planet: Planet, pointerEvent: PointerEvent) {
        val cols = drawer.getVisibleCols()
        val rows = drawer.getVisibleRows()

        getPoints(planet).filter {
            (cols.first <= it.point.x) && (it.point.x <= cols.second) &&
                    (rows.first <= it.point.y) && (it.point.y <= rows.second)
        }.forEach {
            drawPoint(planet, it, pointerEvent)
        }
    }

    private fun getPoints(planet: Planet): Set<DirectedPoint> =
            planet.paths
                    .flatMap {
                        setOf(
                                it.first.startPoint to it.first.startDirection,
                                it.first.endPoint to it.first.endDirection
                        )
                    }
                    .groupBy { it.first }
                    .mapValues {
                        it.value
                                .map { it.second }
                                .toSet()
                    }.let {
                        val h = it.toMutableMap()
                        planet.start?.let {
                            if (!h.containsKey(it)) {
                                h[it] = emptySet()
                            }
                        }
                        planet.target?.let {
                            if (!h.containsKey(it)) {
                                h[it] = emptySet()
                            }
                        }
                        h.toMap()
                    }.map {
                        DirectedPoint(it)
                    }.toSet()


    private fun drawPoint(planet: Planet, directedPoint: DirectedPoint, pointerEvent: PointerEvent) {
        if (directedPoint.point == planet.target) {
            drawer.circle(directedPoint.point.to2D(), Plotter.POINT_RADIUS, Plotter.Companion.COLOR.TARGET)
        }

        if (directedPoint.point == planet.start) {
            drawer.line(directedPoint.point.to2D(), directedPoint.point.to2D() - (0.0 to 0.5), Plotter.Companion.COLOR.LINE)
        }
        directedPoint.directions.forEach {
            drawer.line(directedPoint.point.to2D(), getLineStart(directedPoint.point, it), Plotter.Companion.COLOR.LINE)
        }

        if (pointerEvent.point == directedPoint.point) {
            pointerEvent.direction?.let {
                drawer.line(directedPoint.point.to2D(), getLineStart(directedPoint.point, it), Plotter.Companion.COLOR.HIGHLIGHT, Plotter.LineType.THICK)
            }
        }

        val background = when (directedPoint.point.getColor(planet.start, planet.startColor)) {
            Point.Color.RED -> Plotter.Companion.COLOR.RED
            Point.Color.BLUE -> Plotter.Companion.COLOR.BLUE
            Point.Color.UNDEFINED -> Plotter.Companion.COLOR.BACKGROUND
        }

        drawer.rect(
                directedPoint.point.to2D() - (Plotter.POINT_SIZE / 2 to -Plotter.POINT_SIZE / 2),
                Point2D(Plotter.POINT_SIZE, Plotter.POINT_SIZE),
                background,
                if (pointerEvent.point == directedPoint.point && pointerEvent.direction == null) Plotter.Companion.COLOR.HIGHLIGHT else Plotter.Companion.COLOR.LINE,
                if (pointerEvent.point == directedPoint.point && pointerEvent.direction == null) Plotter.LineType.THICK else Plotter.LineType.NORMAL
        )
    }
}