package com.looker.droidify.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.CancellationSignal
import com.looker.droidify.entity.Repository
import com.looker.droidify.utility.extension.android.asSequence
import com.looker.droidify.utility.extension.android.firstOrNull
import io.reactivex.rxjava3.core.Observable

object Database {
    fun init(context: Context): Boolean {
        val helper = Helper(context)
        db = helper.writableDatabase
        if (helper.created) {
            for (repository in Repository.defaultRepositories) {
                //RepositoryAdapter.put(repository)
            }
        }
        return helper.created || helper.updated
    }

    private lateinit var db: SQLiteDatabase

    // not needed with Room
    private interface Table {
        val memory: Boolean
        val innerName: String
        val createTable: String
        val createIndex: String?
            get() = null

        val databasePrefix: String
            get() = if (memory) "memory." else ""

        val name: String
            get() = "$databasePrefix$innerName"

        fun formatCreateTable(name: String): String {
            return "CREATE TABLE $name (${QueryBuilder.trimQuery(createTable)})"
        }

        val createIndexPairFormatted: Pair<String, String>?
            get() = createIndex?.let {
                Pair(
                    "CREATE INDEX ${innerName}_index ON $innerName ($it)",
                    "CREATE INDEX ${name}_index ON $innerName ($it)"
                )
            }
    }

    private object Schema {
        // implemented
        object Repository : Table {
            const val ROW_ID = "_id"
            const val ROW_ENABLED = "enabled"
            const val ROW_DELETED = "deleted"
            const val ROW_DATA = "data"

            override val memory = false
            override val innerName = "repository"
            override val createTable = """
        $ROW_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $ROW_ENABLED INTEGER NOT NULL,
        $ROW_DELETED INTEGER NOT NULL,
        $ROW_DATA BLOB NOT NULL
      """
        }

        // implemented
        object Product : Table {
            const val ROW_REPOSITORY_ID = "repository_id"
            const val ROW_PACKAGE_NAME = "package_name"
            const val ROW_NAME = "name"
            const val ROW_SUMMARY = "summary"
            const val ROW_DESCRIPTION = "description"
            const val ROW_ADDED = "added"
            const val ROW_UPDATED = "updated"
            const val ROW_VERSION_CODE = "version_code"
            const val ROW_SIGNATURES = "signatures"
            const val ROW_COMPATIBLE = "compatible"
            const val ROW_DATA = "data"
            const val ROW_DATA_ITEM = "data_item"

            override val memory = false
            override val innerName = "product"
            override val createTable = """
        $ROW_REPOSITORY_ID INTEGER NOT NULL,
        $ROW_PACKAGE_NAME TEXT NOT NULL,
        $ROW_NAME TEXT NOT NULL,
        $ROW_SUMMARY TEXT NOT NULL,
        $ROW_DESCRIPTION TEXT NOT NULL,
        $ROW_ADDED INTEGER NOT NULL,
        $ROW_UPDATED INTEGER NOT NULL,
        $ROW_VERSION_CODE INTEGER NOT NULL,
        $ROW_SIGNATURES TEXT NOT NULL,
        $ROW_COMPATIBLE INTEGER NOT NULL,
        $ROW_DATA BLOB NOT NULL,
        $ROW_DATA_ITEM BLOB NOT NULL,
        PRIMARY KEY ($ROW_REPOSITORY_ID, $ROW_PACKAGE_NAME)
      """
            override val createIndex = ROW_PACKAGE_NAME
        }

        // implemented
        object Category : Table {
            const val ROW_REPOSITORY_ID = "repository_id"
            const val ROW_PACKAGE_NAME = "package_name"
            const val ROW_NAME = "name"

            override val memory = false
            override val innerName = "category"
            override val createTable = """
        $ROW_REPOSITORY_ID INTEGER NOT NULL,
        $ROW_PACKAGE_NAME TEXT NOT NULL,
        $ROW_NAME TEXT NOT NULL,
        PRIMARY KEY ($ROW_REPOSITORY_ID, $ROW_PACKAGE_NAME, $ROW_NAME)
      """
            override val createIndex = "$ROW_PACKAGE_NAME, $ROW_NAME"
        }

        // implemented
        object Installed : Table {
            const val ROW_PACKAGE_NAME = "package_name"
            const val ROW_VERSION = "version"
            const val ROW_VERSION_CODE = "version_code"
            const val ROW_SIGNATURE = "signature"

            override val memory = true
            override val innerName = "installed"
            override val createTable = """
        $ROW_PACKAGE_NAME TEXT PRIMARY KEY,
        $ROW_VERSION TEXT NOT NULL,
        $ROW_VERSION_CODE INTEGER NOT NULL,
        $ROW_SIGNATURE TEXT NOT NULL
      """
        }

        // implemented
        object Lock : Table {
            const val ROW_PACKAGE_NAME = "package_name"
            const val ROW_VERSION_CODE = "version_code"

            override val memory = true
            override val innerName = "lock"
            override val createTable = """
        $ROW_PACKAGE_NAME TEXT PRIMARY KEY,
        $ROW_VERSION_CODE INTEGER NOT NULL
      """
        }
    }

    // not needed remove after migration
    private class Helper(context: Context) : SQLiteOpenHelper(context, "droidify", null, 1) {
        var created = false
            private set
        var updated = false
            private set

        override fun onCreate(db: SQLiteDatabase) = Unit
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) =
            onVersionChange(db)

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) =
            onVersionChange(db)

        private fun onVersionChange(db: SQLiteDatabase) {
            handleTables(db, true, Schema.Product, Schema.Category)
            this.updated = true
        }

        override fun onOpen(db: SQLiteDatabase) {
            val create = handleTables(db, false, Schema.Repository)
            val updated = handleTables(db, create, Schema.Product, Schema.Category)
            db.execSQL("ATTACH DATABASE ':memory:' AS memory")
            handleTables(db, false, Schema.Installed, Schema.Lock)
            handleIndexes(
                db,
                Schema.Repository,
                Schema.Product,
                Schema.Category,
                Schema.Installed,
                Schema.Lock
            )
            dropOldTables(db, Schema.Repository, Schema.Product, Schema.Category)
            this.created = this.created || create
            this.updated = this.updated || create || updated
        }
    }

    // not needed remove after migration
    private fun handleTables(db: SQLiteDatabase, recreate: Boolean, vararg tables: Table): Boolean {
        val shouldRecreate = recreate || tables.any {
            val sql = db.query(
                "${it.databasePrefix}sqlite_master", columns = arrayOf("sql"),
                selection = Pair("type = ? AND name = ?", arrayOf("table", it.innerName))
            )
                .use { it.firstOrNull()?.getString(0) }.orEmpty()
            it.formatCreateTable(it.innerName) != sql
        }
        return shouldRecreate && run {
            val shouldVacuum = tables.map {
                db.execSQL("DROP TABLE IF EXISTS ${it.name}")
                db.execSQL(it.formatCreateTable(it.name))
                !it.memory
            }
            if (shouldVacuum.any { it } && !db.inTransaction()) {
                db.execSQL("VACUUM")
            }
            true
        }
    }

    // not needed remove after migration
    private fun handleIndexes(db: SQLiteDatabase, vararg tables: Table) {
        val shouldVacuum = tables.map {
            val sqls = db.query(
                "${it.databasePrefix}sqlite_master", columns = arrayOf("name", "sql"),
                selection = Pair("type = ? AND tbl_name = ?", arrayOf("index", it.innerName))
            )
                .use {
                    it.asSequence()
                        .mapNotNull { it.getString(1)?.let { sql -> Pair(it.getString(0), sql) } }
                        .toList()
                }
                .filter { !it.first.startsWith("sqlite_") }
            val createIndexes = it.createIndexPairFormatted?.let { listOf(it) }.orEmpty()
            createIndexes.map { it.first } != sqls.map { it.second } && run {
                for (name in sqls.map { it.first }) {
                    db.execSQL("DROP INDEX IF EXISTS $name")
                }
                for (createIndexPair in createIndexes) {
                    db.execSQL(createIndexPair.second)
                }
                !it.memory
            }
        }
        if (shouldVacuum.any { it } && !db.inTransaction()) {
            db.execSQL("VACUUM")
        }
    }

    // TODO not needed, remove after migration
    private fun dropOldTables(db: SQLiteDatabase, vararg neededTables: Table) {
        val tables = db.query(
            "sqlite_master", columns = arrayOf("name"),
            selection = Pair("type = ?", arrayOf("table"))
        )
            .use { it.asSequence().mapNotNull { it.getString(0) }.toList() }
            .filter { !it.startsWith("sqlite_") && !it.startsWith("android_") }
            .toSet() - neededTables.mapNotNull { if (it.memory) null else it.name }
        if (tables.isNotEmpty()) {
            for (table in tables) {
                db.execSQL("DROP TABLE IF EXISTS $table")
            }
            if (!db.inTransaction()) {
                db.execSQL("VACUUM")
            }
        }
    }

    sealed class Subject {
        object Repositories : Subject()
        data class Repository(val id: Long) : Subject()
        object Products : Subject()
    }

    private val observers = mutableMapOf<Subject, MutableSet<() -> Unit>>()


    // TODO not needed remove after migration (replaced by LiveData)
    private fun dataObservable(subject: Subject): (Boolean, () -> Unit) -> Unit =
        { register, observer ->
            synchronized(observers) {
                val set = observers[subject] ?: run {
                    val set = mutableSetOf<() -> Unit>()
                    observers[subject] = set
                    set
                }
                if (register) {
                    set += observer
                } else {
                    set -= observer
                }
            }
        }

    // TODO not needed remove after migration (replaced by LiveData)
    fun observable(subject: Subject): Observable<Unit> {
        return Observable.create {
            val callback: () -> Unit = { it.onNext(Unit) }
            val dataObservable = dataObservable(subject)
            dataObservable(true, callback)
            it.setCancellable { dataObservable(false, callback) }
        }
    }

    private fun SQLiteDatabase.query(
        table: String, columns: Array<String>? = null,
        selection: Pair<String, Array<String>>? = null, orderBy: String? = null,
        signal: CancellationSignal? = null
    ): Cursor {
        return query(
            false,
            table,
            columns,
            selection?.first,
            selection?.second,
            null,
            null,
            orderBy,
            null,
            signal
        )
    }
}
