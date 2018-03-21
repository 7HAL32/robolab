package model

import javafx.geometry.Point2D
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @author leon
 */
data class Point(
        val x: Int,
        val y: Int
) {

    constructor(point: Point2D) : this(Math.round(point.x).toInt(), Math.round(point.y).toInt())

    fun getColor(reference: Point?, color: Color) = when {
        color == Color.UNDEFINED || (x + (reference?.x ?: 0) + y + (reference?.y ?: 0)) % 2 == 0 -> color
        color == Color.RED -> Color.BLUE
        else -> Color.RED
    }

    enum class Color {
        RED,
        BLUE,
        UNDEFINED
    }

    fun to2D(): Point2D = Point2D(x.toDouble(), y.toDouble())

    fun getNextPoint(direction: Direction) = when (direction) {
        Direction.NORTH -> Point(x, y + 1)
        Direction.EAST -> Point(x + 1, y)
        Direction.SOUTH -> Point(x, y - 1)
        Direction.WEST -> Point(x - 1, y)
    }

    fun distance(point: Point) = sqrt(
            (x - point.x).toDouble().pow(2.0) + (y - point.y).toDouble().pow(2.0)
    )

    companion object {
        val ZERO = Point(0, 0)
    }
}