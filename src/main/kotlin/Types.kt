import model.Path
import model.Point

/**
 * @author leon
 */
typealias PlanetListener = (String, Point, List<Pair<Path, Set<PathAttributes>>>, Point?) -> Unit