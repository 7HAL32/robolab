package plotter

import model.Direction
import model.Point

/**
 * @author lars
 */
data class DirectedPoint(
        val point:Point,
        val directions: Set<Direction>
) {
    constructor(pair: Pair<Point, Set<Direction>>):this(pair.first, pair.second)
    constructor(pair: Map.Entry<Point, Set<Direction>>):this(pair.key, pair.value)
}