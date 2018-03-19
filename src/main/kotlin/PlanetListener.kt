import model.Path
import model.Point
import plotter.PathAttributes

/**
 * @author lars
 */
interface PlanetListener {
    fun onUpdate(
            planetName: String,
            start: Point,
            startColor: Point.Color,
            paths: List<Pair<Path, Set<PathAttributes>>>,
            target: Point?
    )
}