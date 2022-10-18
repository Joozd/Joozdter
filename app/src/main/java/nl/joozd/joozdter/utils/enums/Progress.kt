package nl.joozd.joozdter.utils.enums

enum class Progress {
    // from ViewModel
    STARTED,
    GOT_FILE,

    //From parser
    READING_FILE,
    PARSING_ROSTER,

    // This done by CalendarWriter
    SAVING_ROSTER,

    DONE,  // This one goes last
    ERROR  // if an error occurred
}