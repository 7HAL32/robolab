package model

import javafx.geometry.Point2D

/**
 * @author leon
 */
data class Point(
        val x: Int,
        val y: Int
) {
    fun getColor(reference: Point, color: Color): Color {
        if (color == Color.UNDEFINED || (x + reference.x + y + reference.y) % 2 == 0)
            return color
        return if (color == Color.RED) Color.BLUE else Color.RED
    }

    enum class Color {
        RED,
        BLUE,
        UNDEFINED
    }

    fun to2D(): Point2D = Point2D(x.toDouble(), y.toDouble())

    fun getNextPoint(direction: Direction): Point {
        return when (direction) {
            Direction.NORTH -> Point(x, y + 1)
            Direction.EAST -> Point(x + 1, y)
            Direction.SOUTH -> Point(x, y - 1)
            Direction.WEST -> Point(x - 1, y)
        }
    }

    fun distance(point: Point): Double {
        return Math.sqrt(Math.pow((x - point.x).toDouble(), 2.0) + Math.pow((y - point.y).toDouble(), 2.0))
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Point)
            return false
        return x == other.x && y == other.y
    }

    override fun toString(): String {
        return "Point( $x, $y)"
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}