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
}