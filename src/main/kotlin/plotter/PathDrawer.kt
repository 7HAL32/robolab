package plotter

import Planet
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import model.Direction
import model.Path
import model.Point
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @author lars
 */
open class PathDrawer(drawer: DrawHelper) : AbsDrawer(drawer) {
    override fun draw(planet: Planet, pointerEvent: PointerEvent, t: Double) {
        planet.paths.forEach {
            printPath(planet, it.first, it.second, t)
        }
    }

    protected fun printPath(planet: Planet, path: Path, attributes: Set<PathAttributes>, animate: Double) = with(path) {
        val t = if (PathAttributes.ANIMATED in attributes) animate else 1.0
        when {
            startPoint == endPoint -> printPathSamePoint(planet, this@with, attributes, t)
            isOnSameLine() && isOppositeDirection() && isTowardsDirection() -> printPathStraight(this@with, attributes, t)
            else -> printPathCurved(this@with, attributes, t)
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

    private fun getWeightColor(t: Double): Color {
        val pos = Math.max(Math.min((t - 0.4) * 5, 1.0), 0.0)
        return Plotter.Companion.COLOR.BACKGROUND.interpolate(Plotter.Companion.COLOR.WEIGHT, pos)
    }

    private fun printPathStraight(path: Path, attributes: Set<PathAttributes>, t: Double) {
        val start = getLineStart(path.startPoint, path.startDirection)
        val end = getLineStart(path.endPoint, path.endDirection)

        val e = ((end - start) * t) + start
        drawer.line(
                start,
                e,
                getLineColor(path, attributes)
        )

        if (t < 1.0) {
            drawer.arrow(e, pointsToDeg(start, end), getLineColor(path, attributes))
        }

        val s = (start + end) * 0.5
        val (x1, x2) = when (path.startDirection) {
            Direction.NORTH, Direction.SOUTH -> (s - (Plotter.LINE_HALF to 0.0)) to (s + (Plotter.LINE_HALF to 0.0))
            Direction.EAST, Direction.WEST -> (s - (0.0 to Plotter.LINE_HALF)) to (s + (0.0 to Plotter.LINE_HALF))
        }

        if (path.weight ?: 1 < 0) {
            if (t >= 0.5)
                drawer.line(x1, x2, Plotter.Companion.COLOR.BLOCKED, Plotter.LineType.THICK)
        } else {
            path.weight?.let {
                drawer.number(it, x1, getWeightColor(t), 12.0)
            }
        }
    }

    private fun radiusToShift(radius: Double): Double {
        return radius * Math.PI / 4
    }

    private fun printPathSamePoint(planet: Planet, path: Path, attributes: Set<PathAttributes>, t: Double) = with(path) {
        when {
            isSameDirection() -> printPathSamePointSameDirection(path, attributes)
            isOppositeDirection() -> printPathSamePointOppositeDirection(planet, path, attributes, t)
            else -> printPathSamePointSquareDirection(path, attributes, t)
        }
    }

    private fun printPathSamePointSameDirection(path: Path, attributes: Set<PathAttributes>) = with(path) {
        val s = getLineStart(startPoint, startDirection, Plotter.RADIUS)

        drawer.line(getLineStart(startPoint, startDirection), s, getLineColor(path, attributes))
        val (x1, x2) = when (path.startDirection) {
            Direction.NORTH, Direction.SOUTH -> (s - (Plotter.LINE_HALF to 0.0)) to (s + (Plotter.LINE_HALF to 0.0))
            Direction.EAST, Direction.WEST -> (s - (0.0 to Plotter.LINE_HALF)) to (s + (0.0 to Plotter.LINE_HALF))
        }

        val color = when {
            path.weight ?: 1 < 0 -> Plotter.Companion.COLOR.BLOCKED
            path.weight == null -> Plotter.Companion.COLOR.ROBOT
            else -> Plotter.Companion.COLOR.LINE
        }
        drawer.line(x1, x2, color, Plotter.LineType.THICK)
    }

    private fun printPathSamePointOppositeDirection(planet: Planet, path: Path, attributes: Set<PathAttributes>, t: Double) = with(path) {
        val s = getLineStart(startPoint, startDirection, Plotter.RADIUS)
        val e = getLineStart(endPoint, endDirection, Plotter.RADIUS)

        val distance = 2 * Math.PI * Plotter.RADIUS + s.distance(e)
        val t1 = Math.min(Math.max((distance / (Math.PI * Plotter.RADIUS)) * t, 0.0), 1.0)
        val t2 = Math.min(Math.max((distance / s.distance(e)) * (t - (Math.PI * Plotter.RADIUS / distance)), 0.0), 1.0)
        val t3 = Math.min(Math.max((distance / (Math.PI * Plotter.RADIUS)) * (t - 1 + ((Math.PI * Plotter.RADIUS) / distance)), 0.0), 1.0)

        drawer.line(getLineStart(startPoint, startDirection), s, getLineColor(path, attributes))
        drawer.line(getLineStart(endPoint, endDirection), e, getLineColor(path, attributes))
        val sides = getUsedPointSides(planet, path.startPoint)
        when (startDirection) {
            Direction.NORTH, Direction.SOUTH -> {
                val shift = if (Direction.WEST in sides) Plotter.RADIUS else -Plotter.RADIUS
                val c1 = s + (shift to 0.0)
                val c2 = e + (shift to 0.0)

                //May be changed by swap the directions
                val invert = (startDirection == Direction.NORTH && shift < 0) || (startDirection == Direction.SOUTH && shift > 0)

                drawer.arc(c1, Plotter.RADIUS, getLineColor(path, attributes), if (startDirection == (if (invert) Direction.SOUTH else Direction.NORTH)) 0.0 else 180.0, (if (invert) -1 else 1) * 180.0 * t1)
                drawer.arc(c2, Plotter.RADIUS, getLineColor(path, attributes), if (startDirection == (if (invert) Direction.NORTH else Direction.SOUTH)) 0.0 else 180.0, (if (invert) -1 else 1) * 180.0 * t3)
                val start = c1 + (shift to 0.0)
                val end = c2 + (shift to 0.0)
                drawer.line(start, start + (end - start) * t2, getLineColor(path, attributes))

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
                val shift = if (Direction.SOUTH in sides) Plotter.RADIUS else -Plotter.RADIUS
                val c1 = s + (0.0 to shift)
                val c2 = e + (0.0 to shift)

                val invert = (startDirection == Direction.EAST && shift < 0) || (startDirection == Direction.WEST && shift > 0)

                drawer.arc(c1, Plotter.RADIUS, getLineColor(path, attributes), if (startDirection == (if (invert) Direction.EAST else Direction.WEST)) 90.0 else 270.0, (if (invert) -1 else 1) * 180.0 * t1)
                drawer.arc(c2, Plotter.RADIUS, getLineColor(path, attributes), if (startDirection == (if (invert) Direction.WEST else Direction.EAST)) 90.0 else 270.0, (if (invert) -1 else 1) * 180.0 * t3)
                val start = c1 + (0.0 to shift)
                val end = c2 + (0.0 to shift)
                drawer.line(start, start + (end - start) * t2, getLineColor(path, attributes))

                val s1 = (start + end) * 0.5
                val (x1, x2) = ((s1 - (0.0 to Plotter.LINE_HALF)) to (s1 + (0.0 to Plotter.LINE_HALF))).let {
                    if (it.first < it.second) it else it.second to it.first
                }

                if (path.weight ?: 1 < 0) {
                    drawer.line(x1, x2, Plotter.Companion.COLOR.BLOCKED, Plotter.LineType.THICK)
                } else {
                    path.weight?.let {
                        drawer.number(it, x1, getWeightColor(t), 12.0)
                    }
                }
            }
        }
    }

    private fun printPathSamePointSquareDirection(path: Path, attributes: Set<PathAttributes>, t: Double): Unit = with(path) {
        val s = getLineStart(startPoint, startDirection, Plotter.RADIUS)
        val e = getLineStart(endPoint, endDirection, Plotter.RADIUS)

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

        drawer.arc(center, Plotter.RADIUS, getLineColor(path, attributes), start.toDouble(), 270.0 * t)

        val lineHalf = radiusToShift(Plotter.LINE_HALF)
        val rs = radiusToShift(Plotter.RADIUS)
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

        if (weight ?: 1 < 0) {
            if (t >= 0.5) {
                drawer.line(x1, x2, Plotter.Companion.COLOR.BLOCKED, Plotter.LineType.THICK)
            }
        } else {
            weight?.let {
                drawer.number(it, x1, getWeightColor(t), 12.0)
            }
        }
    }

    private fun printPathCurved(path: Path, attributes: Set<PathAttributes>, t: Double) {
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
        var t1 = step

        val points = mutableListOf(s)

        while (t1 < t) {
            points.add(calcBezier(s, sd, ed, e, t1))
            t1 += step
        }
        if (t >= 1.0)
            points.add(e)
        else
            points.add(calcBezier(s, sd, ed, e, t))
        drawer.line(points, getLineColor(path, attributes))


        if (t < 1.0) {
            drawer.arrow(points.last(), pointsToDeg(points.getOrNull(points.size - 3)
                    ?: points.getOrNull(points.size - 2)
                    ?: points.last(), points.last()), getLineColor(path, attributes))
        }

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
            if (t >= 0.5)
                drawer.line(x1, x2, Plotter.Companion.COLOR.BLOCKED, Plotter.LineType.THICK)
        } else {
            path.weight?.let {
                drawer.number(it, x1, getWeightColor(t), 12.0)
            }
        }
    }

    private fun pointsToDeg(p1: Point2D, p2: Point2D): Double = with(p2 - p1) {
        acos(y / sqrt(x * x + y * y))
    } * if (p2.x > p1.x) 1.0 else -1.0
}