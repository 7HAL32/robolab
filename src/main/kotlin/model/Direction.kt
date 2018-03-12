package model

/**
 * @author leon
 */
enum class Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST;

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