package `in`.procyk.bring.db

import com.impossibl.postgres.api.jdbc.PGConnection
import com.impossibl.postgres.api.jdbc.PGNotificationListener


@Suppress("SqlSourceToSinkFlow")
abstract class PGNotifyListener(
    private val pgConnection: PGConnection,
    private val channelName: String,
) : AutoCloseable {

    private val listener = object : PGNotificationListener {
        override fun notification(processId: Int, channelName: String?, payload: String?) {
            if (payload.isNullOrBlank()) return
            if (channelName.isNullOrBlank()) return
            if (this@PGNotifyListener.channelName != channelName) return

            notification(payload)
        }
    }

    init {
        pgConnection.addNotificationListener(listener)
        pgConnection.createStatement().use { it.executeUpdate("LISTEN $channelName") }
    }

    override fun close() {
        pgConnection.createStatement().use { it.executeUpdate("UNLISTEN $channelName") }
        pgConnection.removeNotificationListener(listener)
    }

    protected abstract fun notification(payload: String)
}