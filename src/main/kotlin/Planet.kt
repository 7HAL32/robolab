import model.Direction
import model.Point
import plotter.PathAttributes
import plotter.Plotter
import java.nio.file.Files
import java.nio.file.Path

/**
 * @author lars
 */
class Planet(path: Path) {

    val name: String = path.fileName.toString()
    val paths: List<Pair<model.Path, Set<PathAttributes>>>
    var target: Point? = null
    var start: Point? = null
    var startColor: Point.Color = Point.Color.UNDEFINED

    init {
        paths = ArrayList()
        for (line in Files.lines(path).map { it.trim() }) {
            val split = line.split("[, ]".toRegex())
            when (split[0]) {
                "start" -> {
                    start = Point(split[1].toInt(), split[2].toInt())
                    if (split.size > 3) {
                        startColor = when (split[3].toUpperCase()) {
                            "RED" -> Point.Color.RED
                            "BLUE" -> Point.Color.BLUE
                            else -> Point.Color.UNDEFINED
                        }
                    }
                }
                "target" -> {
                    target = Point(split[1].toInt(), split[2].toInt())
                }
                else -> {
                    val p = model.Path(
                            Point(split[0].toInt(), split[1].toInt()),
                            Direction.parse(split[2]),
                            Point(split[3].toInt(), split[4].toInt()),
                            Direction.parse(split[5]),
                            split[6].toInt()
                    )

                    paths.add(Pair(p, HashSet()))
                }
            }
        }

        if (start == null)
            throw IllegalArgumentException("Cannot found a start point")
    }

    fun plot(plotter: Plotter) {
        plotter.onUpdate(name, start!!, paths, target)
    }

}