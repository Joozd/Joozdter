package nl.joozd.joozdter.utils.extensions

/**
 * Marge two maps into one
 */
operator fun <K,V> Map<K,V>.plus(other: Map<K,V>): Map<K,V>{
    return (this.toList() + other.toList()).toMap()
}