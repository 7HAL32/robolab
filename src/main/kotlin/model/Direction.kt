package model

import kotlin.math.PI

/**
 * @author leon
 */
enum class Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    fun export():String = when(this) {
        Direction.NORTH -> "N"
        Direction.EAST -> "E"
        Direction.SOUTH -> "S"
        Direction.WEST -> "W"
    }

    fun toHeading():Double = when(this) {
        Direction.NORTH -> 0.0
        Direction.EAST -> PI / 2
        Direction.SOUTH -> PI
        Direction.WEST -> 3 * PI / 2
    }

    companion object {
        fun parse(direction: String) = when (direction.toLowerCase()) {
            "n" -> NORTH
            "north" -> NORTH
            "e" -> EAST
            "east" -> EAST
            "s" -> SOUTH
            "south" -> SOUTH
            "w" -> WEST
            "west" -> WEST
            else -> NORTH // TODO: default value
        }
    }
}