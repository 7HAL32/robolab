package plotter.drawer

import plotter.DrawHelper
import plotter.Plotter
import plotter.PointerEvent
import kotlin.math.PI
import Planet

/**
 * @author lars
 */
class RobotPositionDrawer(drawer: DrawHelper) : AbsDrawer(drawer) {
    override fun draw(planet: Planet, pointerEvent: PointerEvent, t: Double) {
        if (t >= 1.0 && planet.paths.isNotEmpty()) {
            planet.paths.findLast {
                it.first.weight != null
            }?.let {
                drawer.arrow(getLineStart(it.first.endPoint, it.first.endDirection), it.first.endDirection.toHeading() + PI, Plotter.Companion.COLOR.ROBOT)
            }
        }
    }
}