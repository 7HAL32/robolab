import javafx.scene.Node
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import tornadofx.*
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
                        codeArea.setStyleSpans(0, computeHighlighting(codeArea.text))
                    })
            codeArea.minWidth = 200.0
            codeArea.minHeight = 300.0
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
                matcher.group("PAREN") != null -> "paren"
                matcher.group("BRACE") != null -> "brace"
                matcher.group("BRACKET") != null -> "bracket"
                matcher.group("SEMICOLON") != null -> "semicolon"
                matcher.group("STRING") != null -> "string"
                matcher.group("COMMENT") != null -> "comment"
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

    private fun save() {

    }

    companion object {
        private val KEYWORDS = arrayOf("name", "startColor", "start", "target")

        private val KEYWORD_PATTERN = "\\b(" + KEYWORDS.joinToString("|") + ")\\b"
        private const val PAREN_PATTERN = "\\(|\\)"
        private const val BRACE_PATTERN = "\\{|\\}"
        private const val BRACKET_PATTERN = "\\[|\\]"
        private const val SEMICOLON_PATTERN = "\\;"
        private const val STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\""
        private const val COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/"

        private val PATTERN = Pattern.compile(
                "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                        + "|(?<PAREN>" + PAREN_PATTERN + ")"
                        + "|(?<BRACE>" + BRACE_PATTERN + ")"
                        + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                        + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                        + "|(?<STRING>" + STRING_PATTERN + ")"
                        + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
        )
    }
}