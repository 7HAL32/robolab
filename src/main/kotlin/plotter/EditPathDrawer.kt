package plotter

import Planet
import model.Direction
import model.Path
import model.Point

/**
 * @author lars
 */
class EditPathDrawer(drawer: DrawHelper) : PathDrawer(drawer) {

    var editStart: Pair<Point, Direction>? = null

    override fun draw(planet: Planet, pointerEvent: PointerEvent, t: Double) {
        super.draw(planet, pointerEvent, t)

        editStart?.let {
            if (pointerEvent.point != null && pointerEvent.direction != null) {
                printPath(
                        planet,
                        Path(it.first, it.second, pointerEvent.point, pointerEvent.direction),
                        setOf(PathAttributes.EDITING),
                        t
                )
            }

            drawer.dashed(getLineStart(it.first, it.second), pointerEvent.mouse, Plotter.Companion.COLOR.LINE)
        }
    }

    fun getPath(pointerEvent: PointerEvent): Path? = if (pointerEvent.point != null && pointerEvent.direction != null) {
        editStart?.let {
            Path(it.first, it.second, pointerEvent.point, pointerEvent.direction, 1)
        }
    } else {
        null
    }

}