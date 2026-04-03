package `in`.procyk.bring

inline fun <T, U> T.runIf(condition: Boolean, block: T.() -> U): U where T : U =
    if (condition) block() else this

inline fun <T, U, V : Any> T.runIfNotNull(v: V?, block: T.(V) -> U): U where T : U =
    if (v != null) block(v) else this
