package plotter

import Planet
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import model.Direction
import model.Path
import model.Point
import kotlin.math.pow

/**
 * @author lars
 */
open class PathDrawer(drawer: DrawHelper) : AbsDrawer(drawer) {
    override fun draw(planet: Planet, pointerEvent: PointerEvent) {
        planet.paths.forEach {
            printPath(planet, it.first, it.second)
        }
    }


    protected fun printPath(planet: Planet, path: Path, attributes: Set<PathAttributes>) = with(path) {
        when {
            startPoint == endPoint -> printPathSamePoint(planet, this@with, attributes)
            isOnSameLine() && isOppositeDirection() && isTowardsDirection() -> printPathStraight(this@with, attributes)
            else -> printPathCurved(this@with, attributes)
        }
    }

    private fun getUsedPointSides(planet: Planet, point: Point) =
            planet.paths.map { it.first }
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
        attributes.contains(PathAttributes.HIGHLIGHTED) -> Plotter.Companion.COLOR.HIGHLIGHT
        attributes.contains(PathAttributes.EDITING) -> Plotter.Companion.COLOR.LINE_LIGHT
        path.weight == null -> Plotter.Companion.COLOR.ROBOT
        else -> Plotter.Companion.COLOR.LINE
    }

    private fun printPathStraight(path: Path, attributes: Set<PathAttributes>) {
        val start = getLineStart(path.startPoint, path.startDirection)
        val end = getLineStart(path.endPoint, path.endDirection)
        drawer.line(
                start,
                end,
                getLineColor(path, attributes)
        )

        val s = (start + end) * 0.5
        val (x1, x2) = when (path.startDirection) {
            Direction.NORTH, Direction.SOUTH -> (s - (Plotter.LINE_HALF to 0.0)) to (s + (Plotter.LINE_HALF to 0.0))
            Direction.EAST, Direction.WEST -> (s - (0.0 to Plotter.LINE_HALF)) to (s + (0.0 to Plotter.LINE_HALF))
        }

        if (path.weight ?: 1 < 0) {
            drawer.line(x1, x2, Plotter.Companion.COLOR.BLOCKED, Plotter.LineType.THICK)
        } else {
            path.weight?.let {
                drawer.number(it, x1, Plotter.Companion.COLOR.WEIGHT, 12.0)
            }
        }
    }

    private fun radiusToShift(radius: Double): Double {
        return radius * Math.PI / 4
    }

    private fun printPathSamePoint(planet: Planet, path: Path, attributes: Set<PathAttributes>) = with(path) {
        val radius = 0.3
        val s = getLineStart(startPoint, startDirection, radius)
        val e = getLineStart(endPoint, endDirection, radius)

        when {
            isSameDirection() -> {
                drawer.line(getLineStart(startPoint, startDirection), s, getLineColor(path, attributes))
                val (x1, x2) = when (path.startDirection) {
                    Direction.NORTH, Direction.SOUTH -> (s - (Plotter.LINE_HALF to 0.0)) to (s + (Plotter.LINE_HALF to 0.0))
                    Direction.EAST, Direction.WEST -> (s - (0.0 to Plotter.LINE_HALF)) to (s + (0.0 to Plotter.LINE_HALF))
                }

                drawer.line(x1, x2, Plotter.Companion.COLOR.BLOCKED, Plotter.LineType.THICK)
            }
            isOppositeDirection() -> {
                drawer.line(getLineStart(startPoint, startDirection), s, getLineColor(path, attributes))
                drawer.line(getLineStart(endPoint, endDirection), e, getLineColor(path, attributes))
                val sides = getUsedPointSides(planet, path.startPoint)
                when (startDirection) {
                    Direction.NORTH, Direction.SOUTH -> {
                        val shift = if (Direction.WEST in sides) radius else -radius
                        val c1 = s + (shift to 0.0)
                        val c2 = e + (shift to 0.0)

                        drawer.arc(c1, radius, getLineColor(path, attributes), if (startDirection == Direction.NORTH) 0.0 else 180.0, 180.0)
                        drawer.arc(c2, radius, getLineColor(path, attributes), if (startDirection == Direction.SOUTH) 0.0 else 180.0, 180.0)
                        val start = c1 + (shift to 0.0)
                        val end = c2 + (shift to 0.0)
                        drawer.line(start, end, getLineColor(path, attributes))

                        val s1 = (start + end) * 0.5
                        val (x1, x2) = ((s1 - (Plotter.LINE_HALF to 0.0)) to (s1 + (Plotter.LINE_HALF to 0.0))).let {
                            if (it.first < it.second) it else it.second to it.first
                        }

                        if (path.weight ?: 1 < 0) {
                            drawer.line(x1, x2, Plotter.Companion.COLOR.BLOCKED, Plotter.LineType.THICK)
                        } else {
                            path.weight?.let {
                                drawer.number(it, x1, Plotter.Companion.COLOR.WEIGHT, 12.0)
                            }
                        }
                    }
                    Direction.EAST, Direction.WEST -> {
                        val shift = if (Direction.SOUTH in sides) radius else -radius
                        val c1 = s + (0.0 to shift)
                        val c2 = e + (0.0 to shift)

                        drawer.arc(c1, radius, getLineColor(path, attributes), if (startDirection == Direction.WEST) 90.0 else 270.0, 180.0)
                        drawer.arc(c2, radius, getLineColor(path, attributes), if (startDirection == Direction.EAST) 90.0 else 270.0, 180.0)
                        val start = c1 + (0.0 to shift)
                        val end = c2 + (0.0 to shift)
                        drawer.line(c1 + (0.0 to shift), c2 + (0.0 to shift), getLineColor(path, attributes))

                        val s1 = (start + end) * 0.5
                        val (x1, x2) = ((s1 - (0.0 to Plotter.LINE_HALF)) to (s1 + (0.0 to Plotter.LINE_HALF))).let {
                            if (it.first < it.second) it else it.second to it.first
                        }

                        if (path.weight ?: 1 < 0) {
                            drawer.line(x1, x2, Plotter.Companion.COLOR.BLOCKED, Plotter.LineType.THICK)
                        } else {
                            path.weight?.let {
                                drawer.number(it, x1, Plotter.Companion.COLOR.WEIGHT, 12.0)
                            }
                        }
                    }
                }
            }
            else -> {
                drawer.line(getLineStart(startPoint, startDirection), s, getLineColor(path, attributes))
                drawer.line(getLineStart(endPoint, endDirection), e, getLineColor(path, attributes))
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

                drawer.arc(center, radius, getLineColor(path, attributes), start.toDouble(), 270.0)

                val lineHalf = radiusToShift(Plotter.LINE_HALF)
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
                }.let {
                    if (it.first < it.second) it else it.second to it.first
                }

                if (path.weight ?: 1 < 0) {
                    drawer.line(x1, x2, Plotter.Companion.COLOR.BLOCKED, Plotter.LineType.THICK)
                } else {
                    path.weight?.let {
                        drawer.number(it, x1, Plotter.Companion.COLOR.WEIGHT, 12.0)
                    }
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

        val step = 10.0 / (s.distance(e) * drawer.gridWidth)
        var t = step

        val points = mutableListOf(s)

        while (t < 1.0) {
            points.add(calcBezier(s, sd, ed, e, t))
            t += step
        }
        points.add(e)
        drawer.line(points, getLineColor(path, attributes))

        val c = calcBezier(s, sd, ed, e, 0.5)
        val (bezierBefore, bezierAfter) =
                (calcBezier(s, sd, ed, e, 0.49) to calcBezier(s, sd, ed, e, 0.51))

        val dir = (bezierBefore - bezierAfter).let {
            Point2D(it.y, -it.x).normalize()
        } * Plotter.LINE_HALF
        val (x1, x2) = ((c - dir) to (c + dir)).let {
            if (it.first < it.second) it else it.second to it.first
        }

        if (path.weight ?: 1 < 0) {
            drawer.line(x1, x2, Plotter.Companion.COLOR.BLOCKED, Plotter.LineType.THICK)
        } else {
            path.weight?.let {
                drawer.number(it, x1, Plotter.Companion.COLOR.WEIGHT, 12.0)
            }
        }
    }

}