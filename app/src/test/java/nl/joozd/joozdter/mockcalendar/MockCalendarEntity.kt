package nl.joozd.joozdter.mockcalendar

abstract class MockCalendarEntity {
    abstract val columnNames: Set<String>
    protected open val row = HashMap<String, String>()

    /**
     * Get values from [projection] and put them in a [MockCalendarValues] object
     * Will throw NoSuchElementException if Column names from Projection not in [columnNames]
     * If no data but solumn exists, will give ""
     */
    fun getValues(projection: Array<String>?) = MockCalendarValues().apply{
        putValues((projection ?: columnNames.toTypedArray()).map{ row[it] ?: emptyOrFail(it) })
    }

    operator fun get(key: String) = row[key]!!.trim()

    operator fun set(key: String, value: String){
        row[key] = value
    }

    private fun emptyOrFail(key: String) = if (key in columnNames) "" else throw NoSuchElementException("key $key not found in $this")
}