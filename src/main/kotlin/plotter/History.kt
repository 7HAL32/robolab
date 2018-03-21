package plotter

class History<T>(obj: T) {

    private val elements = mutableListOf(obj)
    private var index: Int = 0

    fun current() = elements[index]

    fun reset(obj: T) {
        index = 0
        elements.clear()
        elements.add(obj)
    }


}
