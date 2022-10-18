package nl.joozd.joozdter.utils.extensions

/**
 * Replace any occurence of [oldValue] with [newValue]
 */
fun <T> MutableList<T>.replaceValue(oldValue: T, newValue: T){
    indices.forEach {
        if (get(it) == oldValue) set(it, newValue)
    }
}