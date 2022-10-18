package nl.joozd.joozdter.utils.enums

enum class Progress {
    // from ViewModel
    STARTED,
    GOT_FILE,
    DONE, // This one goes last
    ERROR, // if an error occurred

    //From parser
    READING_FILE,
    PARSING_ROSTER,

    // This done by CalendarWriter
    GETTING_OLD_ENTRIES,
    REMOVING_OLD_ENTRIES,
    SAVING_NEW_ENTRIES

}