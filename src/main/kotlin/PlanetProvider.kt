import model.Point
import plotter.Plotter
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author lars
 */
class PlanetProvider(
        val planet: String,
        val plotter: Plotter
) {

    companion object {
        val planets: List<Path>
            get() = Paths.get("planet").toList()

        fun getColor(name: String): Point.Color {
            return Point.Color.UNDEFINED // TODO
        }

        fun getPlanet(name: String): Planet {
            return Planet(Paths.get("planet").resolve("$name.planet"))
        }

    }
}