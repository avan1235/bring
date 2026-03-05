package `in`.procyk.bring

inline fun <T, U> T.runIf(condition: Boolean, block: T.() -> U): U where T : U =
    if (condition) block() else this
