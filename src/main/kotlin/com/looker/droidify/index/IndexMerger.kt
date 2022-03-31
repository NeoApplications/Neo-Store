package com.looker.droidify.index

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.looker.droidify.database.Converters.toByteArray
import com.looker.droidify.database.Converters.toReleases
import com.looker.droidify.database.entity.Release
import com.looker.droidify.entity.Product
import com.looker.droidify.utility.extension.android.asSequence
import com.looker.droidify.utility.extension.android.execWithResult
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File

class IndexMerger(file: File) : Closeable {
    private val db = SQLiteDatabase.openOrCreateDatabase(file, null)

    init {
        db.execWithResult("PRAGMA synchronous = OFF")
        db.execWithResult("PRAGMA journal_mode = OFF")
        db.execSQL("CREATE TABLE product (package_name TEXT PRIMARY KEY, description TEXT NOT NULL, data BLOB NOT NULL)")
        db.execSQL("CREATE TABLE releases (package_name TEXT PRIMARY KEY, data BLOB NOT NULL)")
        db.beginTransaction()
    }

    fun addProducts(products: List<Product>) {
        for (product in products) {
            val outputStream = ByteArrayOutputStream()
            outputStream.write(product.toJSON().toByteArray())
            db.insert("product", null, ContentValues().apply {
                put("package_name", product.packageName)
                put("description", product.description)
                put("data", outputStream.toByteArray())
            })
        }
    }

    fun addReleases(pairs: List<Pair<String, List<Release>>>) {
        for (pair in pairs) {
            val (packageName, releases) = pair
            val outputStream = ByteArrayOutputStream()
            outputStream.write(toByteArray(releases))
            db.insert("releases", null, ContentValues().apply {
                put("package_name", packageName)
                put("data", outputStream.toByteArray())
            })
        }
    }

    private fun closeTransaction() {
        if (db.inTransaction()) {
            db.setTransactionSuccessful()
            db.endTransaction()
        }
    }

    fun forEach(repositoryId: Long, windowSize: Int, callback: (List<Product>, Int) -> Unit) {
        closeTransaction()
        db.rawQuery(
            """SELECT product.description, product.data AS pd, releases.data AS rd FROM product
      LEFT JOIN releases ON product.package_name = releases.package_name""", null
        )
            ?.use { it ->
                it.asSequence().map {
                    val description = it.getString(0)
                    val product = Product.fromJson(String(it.getBlob(1))).apply {
                        this.repositoryId = repositoryId
                        this.description = description
                    }
                    val releases = it.getBlob(2)?.let { toReleases(it) }.orEmpty()
                    product.copy(releases = releases)
                }.windowed(windowSize, windowSize, true)
                    .forEach { products -> callback(products, it.count) }
            }
    }

    override fun close() {
        db.use { closeTransaction() }
    }
}
