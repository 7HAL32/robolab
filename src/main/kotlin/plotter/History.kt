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

    fun redo() {
        if (index < elements.size -1) {
            index += 1
        }
    }

    fun undo() {
        if (index > 0) {
            index -= 1
        }
    }

    fun push(obj: T) {
        while (elements.size - 1 > index) {
            elements.removeAt(elements.size - 1)
        }
        elements.add(obj)
        index = elements.size -1
    }
}
