package plotter

import Planet
import javafx.geometry.Point2D
import model.Direction
import model.Path
import model.Point

/**
 * @author lars
 */
class EditPathDrawer(drawer: DrawHelper, private val editStart: Pair<Point, Direction>) : PathDrawer(drawer) {
    override fun draw(planet: Planet, pointerEvent: PointerEvent) {
        super.draw(planet, pointerEvent)

        if (pointerEvent.point != null && pointerEvent.direction != null) {
            printPath(
                    planet,
                    Path(editStart.first, editStart.second, pointerEvent.point, pointerEvent.direction),
                    setOf(PathAttributes.EDITING)
            )
        }

        drawer.dashed(getLineStart(editStart.first, editStart.second), pointerEvent.mouse, Plotter.Companion.COLOR.LINE)
    }

    fun getPath(pointerEvent: PointerEvent): Path? = if (pointerEvent.point != null && pointerEvent.direction != null) {
        Path(editStart.first, editStart.second, pointerEvent.point, pointerEvent.direction, 1)
    } else {
        null
    }

}