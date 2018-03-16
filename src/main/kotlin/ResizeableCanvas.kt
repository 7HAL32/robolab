import javafx.scene.canvas.Canvas

/**
 * @author lars
 */
class ResizeableCanvas : Canvas() {

    override fun isResizable() = true

    private val drawHooks = ArrayList<() -> Unit>()

    init {
        widthProperty().addListener({ _ -> draw() })
        heightProperty().addListener({ _ -> draw() })
    }

    override fun prefWidth(height: Double): Double = width

    override fun prefHeight(width: Double): Double = height

    fun draw() {
        drawHooks.forEach { it() }
    }

    fun addDrawHook(drawHook: () -> Unit) {
        drawHooks.add(drawHook)
    }
}