package model

/**
 * @author leon
 */
data class Path(
        val startPoint: Point,
        val startDirection: Direction,
        val endPoint: Point,
        val endDirection: Direction,
        val weight: Int? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Path)
            return false
        return (startPoint == other.startPoint &&
                startDirection == other.startDirection &&
                endPoint == other.endPoint &&
                endDirection == other.endDirection) ||
                (startPoint == other.endPoint &&
                        startDirection == other.endDirection &&
                        endPoint == other.startPoint &&
                        endDirection == other.endDirection)
    }

    fun isOnSameLine(): Boolean {
        return startPoint.x == endPoint.x || startPoint.y == endPoint.y
    }

    fun isOppositeDirection(): Boolean {
        return when (startDirection) {
            Direction.NORTH -> endDirection == Direction.SOUTH
            Direction.EAST -> endDirection == Direction.WEST
            Direction.SOUTH -> endDirection == Direction.NORTH
            Direction.WEST -> endDirection == Direction.EAST
        }
    }

    fun isSameDirection(): Boolean {
        return startDirection == endDirection
    }

    fun isTowardsDirection(): Boolean {
        return startPoint.distance(endPoint) >= startPoint.getNextPoint(startDirection).distance(endPoint.getNextPoint(endDirection))
    }

    fun isTowardsTopRight(): Boolean {
        return (startDirection == Direction.NORTH && endDirection == Direction.EAST) ||
                (endDirection == Direction.NORTH && startDirection == Direction.EAST)
    }

    fun isTowardsTopLeft(): Boolean {
        return (startDirection == Direction.NORTH && endDirection == Direction.WEST) ||
                (endDirection == Direction.NORTH && startDirection == Direction.WEST)
    }

    fun isTowardsBottomRight(): Boolean {
        return (startDirection == Direction.SOUTH && endDirection == Direction.EAST) ||
                (endDirection == Direction.SOUTH && startDirection == Direction.EAST)
    }

    fun isTowardsBottomLeft(): Boolean {
        return (startDirection == Direction.SOUTH && endDirection == Direction.WEST) ||
                (endDirection == Direction.SOUTH && startDirection == Direction.WEST)
    }
}