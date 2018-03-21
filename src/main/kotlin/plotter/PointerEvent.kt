package plotter

import javafx.geometry.Point2D
import model.Direction
import model.Path
import model.Point

data class PointerEvent(
        val point: Point?,
        val direction: Direction?,
        val path: Path?,
        val mouse: Point2D
) {
    companion object {
        fun empty(mouse: Point2D): PointerEvent = PointerEvent(null, null, null, mouse)

        fun empty(old: PointerEvent): PointerEvent = PointerEvent(null, null, null, old.mouse)
    }
}