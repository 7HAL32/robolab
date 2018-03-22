package plotter

import Planet
import javafx.scene.paint.Color
import model.Direction
import model.Point

/**
 * @author lars
 */
abstract class AbsDrawer(
        val drawer: DrawHelper
) {

    protected fun getLineStart(point: Point, direction: Direction, shift: Double = Plotter.POINT_SHIFT) =
            when (direction) {
                Direction.NORTH -> point.to2D() + (0.toDouble() to shift)
                Direction.EAST -> point.to2D() + (shift to 0.toDouble())
                Direction.SOUTH -> point.to2D() - (0.toDouble() to shift)
                Direction.WEST -> point.to2D() - (shift to 0.toDouble())
            }

    abstract fun draw(planet: Planet, pointerEvent:PointerEvent, t: Double)
}