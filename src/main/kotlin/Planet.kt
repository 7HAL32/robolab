import model.Direction
import model.Point
import plotter.PathAttributes
import plotter.plus
import plotter.times
import java.nio.file.Path

/**
 * @author lars
 */
class Planet(
        val name: String,
        val paths: List<Pair<model.Path, Set<PathAttributes>>>,
        val target: Point?,
        val start: Point?,
        val startColor: Point.Color
) {

    fun setName(name: String): Planet = Planet(
            name,
            paths,
            target,
            start,
            startColor
    )

    fun setStartColor(startColor: Point.Color): Planet = Planet(
            name,
            paths,
            target,
            start,
            startColor
    )

    fun setTarget(target: Point?): Planet = Planet(
            name,
            paths,
            target,
            start,
            startColor
    )

    fun addPath(path: model.Path, attributes: Set<PathAttributes> = emptySet()): Planet = Planet(
            name,
            paths + Pair(path, attributes),
            target,
            start,
            startColor
    )

    fun export():List<String> {
        val lines = ArrayList<String>()

        val h = if (startColor == Point.Color.UNDEFINED) "" else " ${startColor.name.toLowerCase()}"
        start?.let {
            lines.add("start ${it.x},${it.y}$h")
        }

        paths.forEach {
            val w = it.first.weight ?: 1
            lines.add("${it.first.startPoint.x},${it.first.startPoint.y},${it.first.startDirection.export()} ${it.first.endPoint.x},${it.first.endPoint.y},${it.first.endDirection.export()} $w")
        }

        target?.let {
            lines.add("target ${it.x},${it.y}")
        }

        return lines.toList()
    }

    fun getCenter(): Point {
        if (start != null) {
            return start
        } else {
            val points = paths
                    .flatMap {
                        setOf(
                                it.first.startPoint,
                                it.first.endPoint
                        )
                    }
                    .distinct()
                    .map {
                        it.to2D()
                    }

            val sum = points.reduce { acc, point ->
                acc + point
            }

            return Point(sum * (1 / points.size))
        }
    }


    companion object {

        fun empty(): Planet = Planet(
                "",
                emptyList(),
                null,
                Point(0, 0),
                Point.Color.UNDEFINED
        )

        fun fromFile(filePath: Path): Planet {
            val name = filePath.fileName.toString()
            val lines = filePath.toFile().readLines().map { it.split("[, ]".toRegex()).map { it.trim() } }

            val startPointAndColor = lines.find { it.first() == "start" }?.let {
                Point(it[1].toInt(), it[2].toInt()) to if (it.size > 3) {
                    when (it[3].toUpperCase()) {
                        "RED" -> Point.Color.RED
                        "BLUE" -> Point.Color.BLUE
                        else -> Point.Color.UNDEFINED
                    }
                } else Point.Color.UNDEFINED
            } ?: (Point(0, 0) to Point.Color.UNDEFINED).also {
                throw IllegalArgumentException("Cannot find a start point")
            }
            val start = startPointAndColor.first
            val startColor = startPointAndColor.second

            val target = lines.find { it.first() == "target" }?.let { Point(it[1].toInt(), it[2].toInt()) }

            val paths = lines.filter { it.first().toIntOrNull() != null }.map {
                model.Path(
                        Point(it[0].toInt(), it[1].toInt()),
                        Direction.parse(it[2]),
                        Point(it[3].toInt(), it[4].toInt()),
                        Direction.parse(it[5]),
                        if (it[6] == "blocked") -1 else it[6].toInt()
                ) to emptySet<PathAttributes>()
            }

            return Planet(name, paths, target, start, startColor)
        }

        fun fromScratch(start: Point): Planet = Planet(
                "",
                emptyList(),
                null,
                start,
                Point.Color.UNDEFINED
        )
    }
}