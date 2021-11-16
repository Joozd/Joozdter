package nl.joozd.joozdter.mockcalendar

class MockCalendar: MockCalendarEntity(){
    override val columnNames: Set<String> =
        ColumnNameSets.baseColumns +
        ColumnNameSets.syncColumns +
        ColumnNameSets.calendarColumns
}