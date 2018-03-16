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
        fun getPlanets(): List<Path> {
            val p = Paths.get("planet")
            return Files.list(p).collect(Collectors.toList())
        }

        fun getColor(name: String): Point.Color {
            return Point.Color.UNDEFINED
        }

        fun getPlanet(name: String): Path {
            return Paths.get("planet").resolve("$name.planet")
        }

    }
}