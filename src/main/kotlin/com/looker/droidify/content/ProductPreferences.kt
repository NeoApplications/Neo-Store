package com.looker.droidify.content

import android.content.Context
import android.content.SharedPreferences
import com.looker.droidify.database.DatabaseX
import com.looker.droidify.database.entity.Ignored
import com.looker.droidify.entity.ProductPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

object ProductPreferences {
    private val defaultProductPreference = ProductPreference(false, 0L)
    private lateinit var preferences: SharedPreferences
    private val mutableSubject = MutableSharedFlow<Pair<String, Long?>>()
    private val subject = mutableSubject.asSharedFlow()
    lateinit var db: DatabaseX

    fun init(context: Context) {
        db = DatabaseX.getInstance(context)
        preferences = context.getSharedPreferences("product_preferences", Context.MODE_PRIVATE)
        CoroutineScope(Dispatchers.Default).launch {
            db.lockDao.insert(*preferences.all.keys
                .mapNotNull { pName ->
                    this@ProductPreferences[pName].databaseVersionCode?.let {
                        Ignored(pName, it)
                    }
                }
                .toTypedArray()
            )
            subject.collect { (packageName, versionCode) ->
                if (versionCode != null) db.lockDao.insert(Ignored(packageName, versionCode))
                else db.lockDao.delete(packageName)
            }
        }
    }

    private val ProductPreference.databaseVersionCode: Long?
        get() = when {
            ignoreUpdates -> 0L
            ignoreVersionCode > 0L -> ignoreVersionCode
            else -> null
        }

    operator fun get(packageName: String): ProductPreference {
        return if (preferences.contains(packageName)) {
            try {
                ProductPreference.fromJson(preferences.getString(packageName, "{}") ?: "{}")
            } catch (e: Exception) {
                e.printStackTrace()
                defaultProductPreference
            }
        } else {
            defaultProductPreference
        }
    }

    operator fun set(packageName: String, productPreference: ProductPreference) {
        val oldProductPreference = this[packageName]
        preferences.edit().putString(packageName, ByteArrayOutputStream()
            .apply { write(productPreference.toJSON().toByteArray()) }
            .toByteArray().toString(Charset.defaultCharset())).apply()
        if (oldProductPreference.ignoreUpdates != productPreference.ignoreUpdates ||
            oldProductPreference.ignoreVersionCode != productPreference.ignoreVersionCode
        ) {
            CoroutineScope(Dispatchers.Default).launch {
                mutableSubject.emit(Pair(packageName, productPreference.databaseVersionCode))
            }
        }
    }
}
