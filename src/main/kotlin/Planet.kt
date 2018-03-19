import model.Direction
import model.Point
import plotter.PathAttributes
import plotter.Plotter
import java.nio.file.Path

/**
 * @author lars
 */
class Planet(
        val name: String,
        val paths: List<model.Path>,
        val target: Point?,
        val start: Point,
        val startColor: Point.Color
) {

    fun plot(plotter: Plotter) {
        plotter.onUpdate(
                name,
                start,
                startColor,
                paths.map { it to emptySet<PathAttributes>() },
                target
        )
    }

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

    fun addPath(path: model.Path): Planet = Planet(
            name,
            paths + path,
            target,
            start,
            startColor
    )

    fun export() {
        val lines = ArrayList<String>()

        val h = if (startColor == Point.Color.UNDEFINED) "" else " ${startColor.name.toLowerCase()}"
        lines.add("start ${start.x},${start.y}$h")

        paths.forEach {
            val w = it.weight ?: 1
            lines.add("${it.startPoint.x},${it.startPoint.y},${it.startDirection.export()} ${it.endPoint.x},${it.endPoint.y},${it.endDirection.export()} $w")
        }

        target?.let {
            lines.add("target ${it.x},${it.y}")
        }

        lines.forEach {
            println(it)
        }
    }

    companion object {
        fun EMPTY(): Planet = Planet(
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
                )
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