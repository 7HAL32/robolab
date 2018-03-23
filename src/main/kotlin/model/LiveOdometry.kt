package model

import javafx.geometry.Point2D

/**
 * @author lars
 */
data class LiveOdometry (
        val x:Double,
        val y: Double,
        val heading: Double
) {
    fun to2D(): Point2D = Point2D(x, y)
}