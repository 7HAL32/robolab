import model.Point
import plotter.Plotter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

/**
 * @author lars
 */
class PlanetProvider(
        val planet: String,
        val plotter: Plotter
) {

    companion object {
        val planets: List<Planet>
            get() = Files.list(Paths.get("planet")).collect(Collectors.toList()).map { Planet.loadFrom(it) }

        fun getColor(name: String): Point.Color {
            return Point.Color.UNDEFINED // TODO
        }

        fun checkPlanet(name:String) = Files.exists(Paths.get("planet").resolve("$name.planet"))

        fun getPlanet(name: String) = Planet.loadFrom(Paths.get("planet").resolve("$name.planet"))

    }
}