package plotter

import model.LiveOdometry

/**
 * @author lars
 */
class OdometryDrawer(private val drawer: DrawHelper) {
    fun draw(liveOdometry: List<LiveOdometry>) {
        if (liveOdometry.isEmpty())
            return

        drawer.line(
                liveOdometry.map {
                    it.to2D()
                },
                Plotter.Companion.COLOR.ODOMETRY
        )
        drawer.arrow(liveOdometry.last().to2D(), liveOdometry.last().heading, Plotter.Companion.COLOR.ODOMETRY)
    }
}