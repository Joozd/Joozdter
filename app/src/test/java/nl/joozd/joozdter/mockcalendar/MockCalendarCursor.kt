package nl.joozd.joozdter.mockcalendar

import android.content.ContentResolver
import android.database.*
import android.net.Uri
import android.os.Bundle
import java.lang.IllegalArgumentException

class MockCalendarCursor(private val table: MockDatabase.Table): Cursor {
    private var currentPos = 0
    private var open = true
    override fun close() {
        open = false
    }

    override fun getCount(): Int = table.count

    override fun getPosition(): Int = currentPos

    override fun move(offset: Int): Boolean {
        val result = (offset + currentPos).inBounds()
        val reply = result == offset+currentPos
        currentPos = result
        return reply
    }

    override fun moveToPosition(position: Int): Boolean {
        if (position.inBounds() == position){
            currentPos = position
            return true
        }
        return false
    }

    override fun moveToFirst(): Boolean {
        currentPos = 0
        return count != 0
    }

    override fun moveToLast(): Boolean {
        currentPos = count -1
        return count != 0
    }

    /**
     * Will be empty if no data in database because it is not an actual database :)
     * If needed I can mock it of course
     */
    override fun getColumnNames(): Array<String>  = table.names ?: emptyArray()

    override fun getColumnCount(): Int = columnNames.size

    override fun getBlob(columnIndex: Int): ByteArray {
        error ("Not supported)")
    }

    override fun getString(column: Int): String =
        table[currentPos].getString(column)

    override fun copyStringToBuffer(columnIndex: Int, buffer: CharArrayBuffer?) {
        error ("Not supported)")
    }


    override fun getShort(column: Int): Short =
        table[currentPos].getShort(column)


    override fun getInt(column: Int): Int =
        table[currentPos].getInt(column)

    override fun getLong(column: Int): Long =
        table[currentPos].getLong(column)

    override fun getFloat(column: Int): Float =
        table[currentPos].getFloat(column)

    override fun getDouble(column: Int): Double =
        table[currentPos].getDouble(column)

    override fun getType(columnIndex: Int): Int {
        error ("Not supported, not a real DB")
    }

    override fun isNull(column: Int): Boolean =
        table[currentPos].isNull(column)

    override fun deactivate() {
        error ("Not supported)")
    }

    override fun requery(): Boolean {
        error ("Not supported)")
    }

    override fun isClosed(): Boolean = !open

    override fun registerContentObserver(observer: ContentObserver?) {
        error ("Not supported)")
    }

    override fun unregisterContentObserver(observer: ContentObserver?) {
        error ("Not supported)")
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        error ("Not supported)")
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        error ("Not supported)")
    }

    override fun setNotificationUri(cr: ContentResolver?, uri: Uri?) {
        error ("Not supported)")
    }

    override fun getNotificationUri(): Uri {
        error ("Not supported)")
    }

    override fun getWantsAllOnMoveCalls(): Boolean {
        error ("Not supported)")
    }

    override fun setExtras(extras: Bundle?) {
        error ("Not supported)")
    }

    override fun getExtras(): Bundle {
        error ("Not supported)")
    }

    override fun respond(extras: Bundle?): Bundle {
        error ("Not supported)")
    }

    override fun moveToNext(): Boolean{
        if (++currentPos >= count){
            return false
        }
        return true
    }

    override fun moveToPrevious(): Boolean {
        if (currentPos > 0){
            currentPos--
            return true
        }
        return false
    }

    override fun isFirst(): Boolean =
        currentPos == 0


    override fun isLast(): Boolean = currentPos == count - 1

    override fun isBeforeFirst(): Boolean = currentPos < 0

    override fun isAfterLast(): Boolean = currentPos >= count

    override fun getColumnIndex(columnName: String?): Int =
        columnNames.indexOf(columnName)

    override fun getColumnIndexOrThrow(columnName: String?): Int {
        if (columnName !in columnNames) throw(IllegalArgumentException("$columnName not in columns"))
        return getColumnIndex(columnName)
    }

    override fun getColumnName(columnIndex: Int): String =
        columnNames[columnIndex]

    private fun Int.inBounds(min: Int = -1, max: Int = count) = if (this < min) min else maxOf(max, this)

}