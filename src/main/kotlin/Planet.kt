import model.Direction
import model.Point
import plotter.PathAttributes
import plotter.Plotter
import java.nio.file.Path

/**
 * @author lars
 */
class Planet(filePath: Path) {

    val name: String = filePath.fileName.toString()
    val paths: List<model.Path>
    val target: Point?
    val start: Point
    val startColor: Point.Color

    init {
        val lines = filePath.toFile().readLines().map { it.split("[, ]".toRegex()).map { it.trim() } }

        val startPointAndColor = lines.find { it.first() == "start" }?.let {
            Pair(Point(it[1].toInt(), it[2].toInt()), if (it.size > 3) {
                when (it[3].toUpperCase()) {
                    "RED" -> Point.Color.RED
                    "BLUE" -> Point.Color.BLUE
                    else -> Point.Color.UNDEFINED
                }
            } else Point.Color.UNDEFINED)
        } ?: Pair(Point(0, 0), Point.Color.UNDEFINED).also {
            throw IllegalArgumentException("Cannot find a start point")
        }
        start = startPointAndColor.first
        startColor = startPointAndColor.second


        target = lines.find { it.first() == "target" }?.let { Point(it[1].toInt(), it[2].toInt()) }

        paths = lines.filter { it.first().toIntOrNull() != null }.map {
            model.Path(
                    Point(it[0].toInt(), it[1].toInt()),
                    Direction.parse(it[2]),
                    Point(it[3].toInt(), it[4].toInt()),
                    Direction.parse(it[5]),
                    if (it[6] == "blocked") -1 else it[6].toInt()
            )
        }
    }

    fun plot(plotter: Plotter) = plotter.onUpdate(
            name,
            start,
            paths.map { Pair(it, emptySet<PathAttributes>()) },
            target
    )

}