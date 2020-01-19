package nl.joozd.joozdter.comm.utils

import nl.joozd.joozdter.data.MetaData
import nl.joozd.joozdter.utils.toJson

fun String.addMetaData(metaData: MetaData): String {
    return this + "|${metaData.toJson()}"
}