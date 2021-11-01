package com.looker.droidify.content

import android.content.Context
import android.content.SharedPreferences
import com.looker.droidify.database.DatabaseX
import com.looker.droidify.database.Lock
import com.looker.droidify.entity.ProductPreference
import com.looker.droidify.utility.extension.json.Json
import com.looker.droidify.utility.extension.json.parseDictionary
import com.looker.droidify.utility.extension.json.writeDictionary
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

object ProductPreferences {
    private val defaultProductPreference = ProductPreference(false, 0L)
    private lateinit var preferences: SharedPreferences
    private val subject = PublishSubject.create<Pair<String, Long?>>()
    lateinit var db: DatabaseX

    fun init(context: Context) {
        db = DatabaseX.getInstance(context)
        preferences = context.getSharedPreferences("product_preferences", Context.MODE_PRIVATE)
        db.lockDao.insert(*preferences.all.keys
            .mapNotNull { pName ->
                this[pName].databaseVersionCode?.let {
                    Lock().apply {
                        package_name = pName
                        version_code = it
                    }
                }
            }
            .toTypedArray()
        )
        subject
            .observeOn(Schedulers.io())
            .subscribe { (pName, versionCode) ->
                if (versionCode != null) {
                    db.lockDao.insert(Lock().apply {
                        package_name = pName
                        version_code = versionCode
                    })
                } else {
                    db.lockDao.delete(pName)
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
                Json.factory.createParser(preferences.getString(packageName, "{}"))
                    .use { it.parseDictionary(ProductPreference.Companion::deserialize) }
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
            .apply {
                Json.factory.createGenerator(this)
                    .use { it.writeDictionary(productPreference::serialize) }
            }
            .toByteArray().toString(Charset.defaultCharset())).apply()
        if (oldProductPreference.ignoreUpdates != productPreference.ignoreUpdates ||
            oldProductPreference.ignoreVersionCode != productPreference.ignoreVersionCode
        ) {
            subject.onNext(Pair(packageName, productPreference.databaseVersionCode))
        }
    }
}
