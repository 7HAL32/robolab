package plotter.drawer

import plotter.DrawHelper
import plotter.Plotter

/**
 * @author lars
 */
class GridDrawer(private val drawer: DrawHelper) {

    var showGrid: Boolean = true
    var showGridNumber: Boolean = true

    fun draw() {
        val everyLine = drawer.gridWidth > FONT_SIZE

        val rows = drawer.getVisibleRows()
        val cols = drawer.getVisibleCols()

        for (row in rows.first..rows.second) {
            if (showGrid) {
                drawer.hLine(row.toDouble(), Plotter.Companion.COLOR.GRID)
            }
            if (showGridNumber && (everyLine || (row) % 2 == 0)) {
                drawer.rowNumber(row, Plotter.Companion.COLOR.GRID_NUMBER, FONT_SIZE)
            }
        }

        for (col in cols.first..cols.second) {
            if (showGrid) {
                drawer.vLine(col.toDouble(), Plotter.Companion.COLOR.GRID)
            }
            if (showGridNumber && (everyLine || (col) % 2 == 0)) {
                drawer.colNumber(col, Plotter.Companion.COLOR.GRID_NUMBER, FONT_SIZE)
            }
        }
    }

    companion object {
        const val FONT_SIZE = 16.0
    }
}