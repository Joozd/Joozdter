package nl.joozd.joozdter.mockcalendar

class MockCalendarValues() {
    private val contents = ArrayList<String>()

    fun putValues(values: List<String>){
        require (contents.isEmpty()) { "Trying to fill a MockCalendarValues object that is already filled" }
        contents.addAll(values)
    }

    /**
     * This is the only function which will not fail if empty
     */
    fun getString(index: Int): String = contents[index]

    fun getShort(index: Int) = contents[index].toShort()

    fun getInt(index: Int): Int = contents[index].toInt()

    fun getLong(index: Int): Long = contents[index].toLong()

    fun getFloat(index: Int): Float = contents[index].toFloat()

    fun getDouble(index: Int): Double = contents[index].toDouble()

    fun getBoolean(index: Int): Boolean = contents[index].toInt() != 0

    fun isNull(index: Int) = false

}