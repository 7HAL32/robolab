import javafx.scene.Node
import javafx.stage.FileChooser
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import tornadofx.*
import java.nio.file.Files
import java.util.*
import java.util.regex.Pattern


/**
 * @author leon
 */
class FileMapFragment : BaseMapFragment() {

    val initPlanet: Planet by param()
    var planet: Planet

    private lateinit var codeArea: CodeArea

    override fun rightView(rootNode: Node) = with(rootNode) {
        vbox {
            hbox {
                button("Save to file") {
                    action {
                        save()
                    }
                }
            }

            codeArea = CodeArea()
            codeArea.richChanges()
                    .filter({ ch -> ch.inserted != ch.removed })
                    .subscribe({ _ ->
                        update(
                                codeArea.text.split("\n").filter {
                                    it.isNotBlank()
                                }
                        )
                        codeArea.setStyleSpans(0, computeHighlighting(codeArea.text))
                    })
            codeArea.minWidth = 250.0
            codeArea.minHeight = 300.0
            codeArea.style {
                fontSize = Dimension(16.0, Dimension.LinearUnits.pt)
            }
            add(codeArea)
        }
    }

    private fun computeHighlighting(text: String): StyleSpans<Collection<String>>? {
        val matcher = PATTERN.matcher(text)
        var lastKwEnd = 0
        val spansBuilder = StyleSpansBuilder<Collection<String>>()
        while (matcher.find()) {
            val styleClass = (when {
                matcher.group("KEYWORD") != null -> "keyword"
                matcher.group("DIRECTION") != null -> "direction"
                matcher.group("NUMBER") != null -> "number"
                matcher.group("COMMENT") != null -> "comment"
                matcher.group("STRING") != null -> "string"
                else -> ""
            })
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd)
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start())
            lastKwEnd = matcher.end()
        }
        spansBuilder.add(Collections.emptyList(), text.length - lastKwEnd)
        return spansBuilder.create()
    }


    init {
        importStylesheet("/style/codeArea.css")
        planet = initPlanet
        update()
    }

    private fun update() {
        codeArea.replaceText(0, 0, planet.export().joinToString("\n"))
        plotter.update(planet, true)
    }

    private fun update(lines: List<String>) {
        planet = Planet.loadStringList(lines)
        plotter.update(planet)
    }

    private fun save() {
        val dialog = FileChooser()
        dialog.title = "Save planet to file"
        dialog.initialFileName = planet.name.toLowerCase().replace(" ", "_") + ".planet"
        dialog.extensionFilters.add(FileChooser.ExtensionFilter("Planet file", ".planet"))
        try {
            val file = dialog.showSaveDialog(currentWindow!!)
            Files.write(file.toPath(), planet.export())
        } catch (e: Exception) {

        }
    }

    companion object {
        private val KEYWORDS = arrayOf("name", "startColor", "start", "target")

        private val KEYWORD_PATTERN = "\\b(" + KEYWORDS.joinToString("|") + ")\\b"
        private const val DIRECTION_PATTERN = "\\b([N|E|W|S])\\b"
        private const val NUMBER_PATTERN = "(-?[0-9]+)"
        private const val COMMENT_PATTERN = "#!|#[^\n|!]*"
        private const val STRING_PATTERN = "[a-zA-Z]"

        private val PATTERN = Pattern.compile(
                "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                        + "|(?<DIRECTION>" + DIRECTION_PATTERN + ")"
                        + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                        + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                        + "|(?<STRING>" + STRING_PATTERN + ")"
        )
    }
}