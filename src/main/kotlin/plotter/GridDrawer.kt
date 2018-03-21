package plotter

import Planet
import javafx.geometry.Point2D

/**
 * @author lars
 */
class GridDrawer(drawer: DrawHelper) : AbsDrawer(drawer) {

    var showGrid: Boolean = true
    var showGridNumber: Boolean = true

    override fun draw(planet: Planet, pointerEvent: PointerEvent) {
        val fontSize = 16.0

        val everyLine = drawer.gridWidth > fontSize

        val rows = drawer.getVisibleRows()
        val cols = drawer.getVisibleCols()

        for (row in rows.first..rows.second) {
            val y = drawer.systemToReal(Point2D(0.0, row.toDouble())).y
            if (showGrid) {
                drawer.hLine(row.toDouble(), Plotter.Companion.COLOR.GRID)
            }
            if (showGridNumber && (y < drawer.height - 32.0) && (everyLine || (row) % 2 == 0)) {
                drawer.fixedNumber(row, Point2D(24.0, y), Plotter.Companion.COLOR.GRID_NUMBER, fontSize)
            }
        }

        for (col in cols.first..cols.second) {
            val x = drawer.systemToReal(Point2D(col.toDouble(), 0.0)).x
            if (showGrid) {
                drawer.vLine(col.toDouble(), Plotter.Companion.COLOR.GRID)
            }
            if (showGridNumber && (x > 48.0) && (everyLine || (col) % 2 == 0)) {
                drawer.fixedNumber(col, Point2D(x, drawer.height - 16.0), Plotter.Companion.COLOR.GRID_NUMBER, fontSize)
            }
        }
    }
}