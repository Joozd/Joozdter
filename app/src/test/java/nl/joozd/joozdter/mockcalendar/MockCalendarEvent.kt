package nl.joozd.joozdter.mockcalendar

class MockCalendarEvent: MockCalendarEntity(){
    override val columnNames: Set<String> =
        ColumnNameSets.baseColumns +
        ColumnNameSets.syncColumns +
        ColumnNameSets.eventsColumns
}
