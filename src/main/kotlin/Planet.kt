import javafx.geometry.Point2D
import model.Direction
import model.Point
import plotter.PathAttributes
import plotter.plus
import plotter.times
import java.nio.file.Files
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

    fun export(): List<String> {
        val lines = mutableListOf<String>()

        if (name.isNotBlank()) {
            lines.add("# name $name")
        }

        if (startColor != Point.Color.UNDEFINED) {
            lines.add("# startColor ${startColor.toString().toLowerCase()}")
        }

        start?.let {
            lines.add("start ${it.x},${it.y}")
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

            if (points.isEmpty())
                return Point.ZERO

            val sum = points.fold(Point2D.ZERO) { acc, point ->
                acc + point.to2D()
            }

            return Point(sum * (1 / points.size))
        }
    }

    fun hasAnimation(): Boolean = paths.any {
        PathAttributes.ANIMATED in it.second
    }

    fun saveTo(filePath: Path) {
        val destination = if (Files.isDirectory(filePath)) {
            filePath.resolve(name.toLowerCase()+FILE_ENDING)
        } else {
            filePath
        }

        Files.write(destination, export())
    }

    companion object {
        const val FILE_ENDING = ".planet"

        fun empty(): Planet = Planet(
                "",
                emptyList(),
                null,
                null,
                Point.Color.UNDEFINED
        )

        fun loadFrom(filePath: Path): Planet {
            val lines = Files.readAllLines(filePath).map { it.split("[, ]".toRegex()).map { it.trim() } }

            val name = lines.find {
                it.contains("name")
            }?.let {
                it.dropWhile {
                    !it.contains("name")
                }.drop(1).joinToString(" ")
            }?: filePath.fileName.toString().replace(FILE_ENDING, "")

            val start = lines.find {
                it.first() == "start"
            }?.let {
                Point(it[1].toInt(), it[2].toInt())
            }
            val startColor = lines.find {
                it.contains("startColor")
            }?.let {
                when (it.last().toUpperCase()) {
                    "RED" -> Point.Color.RED
                    "BLUE" -> Point.Color.BLUE
                    else -> Point.Color.UNDEFINED
                }
            }?: Point.Color.UNDEFINED

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