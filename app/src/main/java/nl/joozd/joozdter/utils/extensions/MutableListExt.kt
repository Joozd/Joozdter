package nl.joozd.joozdter.utils.extensions

/**
 * Replace any occurence of [oldValue] with [newValue]
 */
fun <T> MutableList<T>.replaceValue(oldValue: T, newValue: T){
    indices.forEach {
        if (get(it) == oldValue) set(it, newValue)
    }
}

/**
 * Replace any occurence for which [predicate] yields `true` with [newValue]
 */
fun <T> MutableList<T>.replaceFiltered(predicate: (T)-> Boolean, newValue: T){
    indices.forEach {
        if (predicate(get(it))) set(it, newValue)
    }
}

/**
 * Add an item to a MutableCollection if it is not null
 */
fun <T> MutableCollection<T>.addNotNull(item: T?) =
    item?.let { add(it) }