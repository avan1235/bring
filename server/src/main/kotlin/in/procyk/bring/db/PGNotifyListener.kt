package `in`.procyk.bring.db

import com.impossibl.postgres.api.jdbc.PGConnection
import com.impossibl.postgres.api.jdbc.PGNotificationListener
import kotlin.uuid.Uuid


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

    private val uniqueName: String = Uuid.random().toHexString()

    init {
        pgConnection.addNotificationListener(uniqueName, channelName, listener)
        pgConnection.createStatement().use { it.executeUpdate("LISTEN $channelName") }
    }

    override fun close() {
        pgConnection.createStatement().use { it.executeUpdate("UNLISTEN $channelName") }
        pgConnection.removeNotificationListener(uniqueName)
        pgConnection.close()
    }

    protected abstract fun notification(payload: String)
}