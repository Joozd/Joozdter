package nl.joozd.joozdter.data.extensions

import androidx.work.Data

fun collapseDataList(list: List<Data>) = Data.Builder().apply{
    putInt(LENGTH_DESCRIPTOR, list.size)
    list.forEachIndexed { index, data ->
        putByteArray("$index", data.toByteArray())
    }
}.build()



fun Data.expandCollapsedList(): List<Data>{
    require(getInt(LENGTH_DESCRIPTOR, -1) != -1) { "$this does not seem to be a collapsed List<Data>"}
    val result = mutableListOf<Data>()
    repeat(getInt(LENGTH_DESCRIPTOR, 0)){ index ->
        result.add(Data.fromByteArray(getByteArray("$index")!!))
    }
    return result
}

private const val LENGTH_DESCRIPTOR = "x"

