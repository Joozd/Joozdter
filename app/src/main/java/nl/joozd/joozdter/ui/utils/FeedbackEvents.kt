package nl.joozd.joozdter.ui.utils

object FeedbackEvents {
    interface Event

    enum class GeneralEvents :
        Event {
        DONE,
        NOT_IMPLEMENTED,
        ERROR,
        OK
    }

    enum class PdfParserActivityEvents: Event{
        NO_VALID_CALENDAR_PICKED,
        FILE_NOT_FOUND,
        FILE_ERROR,
        NOT_A_KLC_ROSTER,
        DONE
    }
}