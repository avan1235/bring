package buildsrc.convention

object Env {

    val SavvryVersion: String
        get() = System.getenv("VERSION") ?: "1.0.0"

    val SavvryPackageName: String
        get() = "in.procyk.savvry"
}
