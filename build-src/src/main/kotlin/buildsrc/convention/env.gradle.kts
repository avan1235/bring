package buildsrc.convention

object Env {

    val BringVersion: String
        get() = System.getenv("VERSION") ?: "1.0.0"

    val BringPackageName: String
        get() = "in.procyk.bring"
}
