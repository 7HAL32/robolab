package plotter

import model.LiveOdometry

/**
 * @author lars
 */
class OdometryDrawer(private val drawer: DrawHelper) {
    fun draw(liveOdometry: List<LiveOdometry>) {
        drawer.line(
                liveOdometry.map {
                    it.to2D()
                },
                Plotter.Companion.COLOR.ODOMETRY
        )
    }
}