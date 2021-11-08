package nl.joozd.joozdter.data

/**
 *  Numbers are random values for serializing
 */
enum class EventTypes(val value: Int) {
    NO_DUTY(0),
    DUTY(1),            // Time at which checking happens untill time when checkout happens
    PICK_UP(3),         // Pickup. No end time (it will end at CHECK_IN)
    CHECK_IN(4),              // CHECK_IN utill CHECK_OUT makes a DUTY
    CHECK_OUT(5),
    SIMULATOR_DUTY(10), // without briefing, actual time in sim
    TRAINING(11),       // includes sim sessions + briefing
    FLIGHT(20),
    HOTEL(30),          // may not have an end time. In this case, look it up in next day. (or the day after, etc).
    STANDBY(40),
    LEAVE(100),
    CLICK(101),
    ROUTE_DAY(200),     // "dag over"
    UNKNOWN_EVENT(-1);
                              // Hotel ends with [PICK_UP]
    //TODO add all other possible events

    companion object{
        /**
         * Get the event type from an integer (eg. when it was stored in a DB)
         */
        fun of(v: Int): EventTypes = values().firstOrNull { it.value == v } ?: UNKNOWN_EVENT
    }

}