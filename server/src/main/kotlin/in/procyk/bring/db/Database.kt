package `in`.procyk.bring.db

import com.impossibl.postgres.api.jdbc.PGConnection
import com.impossibl.postgres.jdbc.PGDataSource
import `in`.procyk.bring.env
import io.github.cdimascio.dotenv.Dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.properties.Delegates


object Database {
    private var ds by Delegates.notNull<PGDataSource>()
    private var db by Delegates.notNull<Database>()

    fun init(dotenv: Dotenv) {
        ds = PGDataSource().apply {
            url = dotenv.databaseUrl
            user = dotenv.env("POSTGRES_USER")
            password = dotenv.env("POSTGRES_PASSWORD")
            sslMode = dotenv.env("POSTGRES_SSL_MODE")
        }
        db = Database.connect(ds)
        @Suppress("DEPRECATION")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(FavoriteElementsTable)
            SchemaUtils.createMissingTablesAndColumns(ShoppingListItemsTable)
            SchemaUtils.createMissingTablesAndColumns(ShoppingListsTable)
            exec(CREATE_NOTIFY_SHOPPING_LIST_ITEMS_FUNCTION)
            exec(CREATE_SHOPPING_LIST_ITEMS_TRIGGER)
        }
    }

    fun createListener(
        channelName: String,
        onPayload: (String) -> Unit
    ): PGNotifyListener =
        object : PGNotifyListener(ds.connection.unwrap(PGConnection::class.java), channelName) {
            override fun notification(payload: String) {
                onPayload(payload)
            }
        }
}

private val Dotenv.databaseUrl: String
    get() {
        val host = env<String>("POSTGRES_HOST")
        val port = env<String>("POSTGRES_PORT")
        val db = env<String>("POSTGRES_DB")
        return "jdbc:pgsql://$host:$port/$db"
    }

private const val CREATE_NOTIFY_SHOPPING_LIST_ITEMS_FUNCTION: String = """
CREATE OR REPLACE FUNCTION notify_shopping_list_items_trigger() RETURNS trigger AS
${'$'}trigger${'$'}
DECLARE
    list_id uuid;
    event_timestamp text;
BEGIN
    event_timestamp := CURRENT_TIMESTAMP::text;
    
    CASE TG_OP
        WHEN 'UPDATE' THEN 
            PERFORM pg_notify('event_'::text || REPLACE(OLD.list_id::text, '-', '_'), event_timestamp);
            list_id := NEW.list_id;
        WHEN 'INSERT' THEN list_id := NEW.list_id;
        WHEN 'DELETE' THEN list_id := OLD.list_id;
        ELSE RAISE EXCEPTION 'Unknown TG_OP: "%". Should not occur!', TG_OP;
        END CASE;
        
    PERFORM pg_notify('event_'::text || REPLACE(list_id::text, '-', '_'), event_timestamp);

    RETURN NULL;
END;
${'$'}trigger${'$'} LANGUAGE plpgsql;
"""

private const val CREATE_SHOPPING_LIST_ITEMS_TRIGGER: String = """
CREATE OR REPLACE TRIGGER shopping_list_items_trigger
    AFTER INSERT OR UPDATE OR DELETE
    ON shopping_list_items
    FOR EACH ROW
EXECUTE PROCEDURE notify_shopping_list_items_trigger();
"""
