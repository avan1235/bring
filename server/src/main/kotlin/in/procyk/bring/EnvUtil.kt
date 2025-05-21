package `in`.procyk.bring

import io.github.cdimascio.dotenv.Dotenv

internal inline fun <reified T : Any> Dotenv.env(name: String): T {
    val value = get(name) ?: error("Environment variable $name is not defined in the system")
    return when (T::class) {
        String::class -> value as T
        Int::class -> value.toIntOrNull() as? T ?: error("$value cannot be converted to ${T::class.simpleName}")
        else -> throw IllegalArgumentException("Unsupported type ${T::class.simpleName}")
    }
}
