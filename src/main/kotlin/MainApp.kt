import tornadofx.App

/**
 * @author leon
 */
class MainApp : App(MainView::class) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(MainApp::class.java)
        }
    }
}