package buildsrc.convention

object Env {

    val BringVersion: String
        get() = System.getenv("VERSION") ?: "dev"

    val BringPackageName: String
        get() = "in.procyk.bring"
}
