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

    fun isOnSameLine() = startPoint.x == endPoint.x || startPoint.y == endPoint.y

    fun isOppositeDirection() = when (startDirection) {
        Direction.NORTH -> endDirection == Direction.SOUTH
        Direction.EAST -> endDirection == Direction.WEST
        Direction.SOUTH -> endDirection == Direction.NORTH
        Direction.WEST -> endDirection == Direction.EAST
    }

    fun isSameDirection() = startDirection == endDirection

    fun isTowardsDirection() = startPoint.distance(endPoint) >=
            startPoint.getNextPoint(startDirection).distance(endPoint.getNextPoint(endDirection))

    fun isTowardsTopRight() = (startDirection == Direction.NORTH && endDirection == Direction.EAST) ||
            (endDirection == Direction.NORTH && startDirection == Direction.EAST)

    fun isTowardsTopLeft() = (startDirection == Direction.NORTH && endDirection == Direction.WEST) ||
            (endDirection == Direction.NORTH && startDirection == Direction.WEST)

    fun isTowardsBottomRight() = (startDirection == Direction.SOUTH && endDirection == Direction.EAST) ||
            (endDirection == Direction.SOUTH && startDirection == Direction.EAST)

    fun isTowardsBottomLeft() = (startDirection == Direction.SOUTH && endDirection == Direction.WEST) ||
            (endDirection == Direction.SOUTH && startDirection == Direction.WEST)

    override fun hashCode(): Int {
        return super.hashCode() // TODO
    }
}