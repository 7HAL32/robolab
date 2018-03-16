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
            paths: List<Pair<Path, Set<PathAttributes>>>,
            target: Point?
    )
}