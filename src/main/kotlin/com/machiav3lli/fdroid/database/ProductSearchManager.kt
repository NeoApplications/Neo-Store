package com.machiav3lli.fdroid.database

import android.content.Context
import androidx.appsearch.app.AppSearchSession
import androidx.appsearch.app.PutDocumentsRequest
import androidx.appsearch.app.SearchSpec
import androidx.appsearch.app.SetSchemaRequest
import androidx.appsearch.localstorage.LocalStorage
import com.machiav3lli.fdroid.database.entity.ProductAS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductSearchManager(
    private val appContext: Context
) {
    private var session: AppSearchSession? = null

    suspend fun init() {
        withContext(Dispatchers.IO) {
            val future = LocalStorage.createSearchSessionAsync(
                LocalStorage.SearchContext.Builder(
                    appContext,
                    "product_search",
                ).build()
            )
            val request = SetSchemaRequest.Builder()
                .addDocumentClasses(ProductAS::class.java)
                .build()

            session = future.get()
            session?.setSchemaAsync(request)
        }
    }

    suspend fun insertProducts(prods: List<ProductAS>): Boolean {
        return withContext(Dispatchers.IO) {
            session?.putAsync(
                PutDocumentsRequest.Builder()
                    .addDocuments(prods)
                    .build()
            )?.get()?.isSuccess == true
        }
    }

    suspend fun searchProducts(query: String): List<ProductAS> {
        return withContext(Dispatchers.IO) {
            val searchSpec = SearchSpec.Builder()
                //.addFilterNamespaces("") TODO check possible usages
                .setRankingStrategy(SearchSpec.RANKING_STRATEGY_DOCUMENT_SCORE) // TODO check other strategies
                .setOrder(SearchSpec.ORDER_ASCENDING) // TODO reflect preference
                .build()

            val result = session?.search(
                query,
                searchSpec
            ) ?: return@withContext emptyList()

            result.nextPageAsync.get().mapNotNull {
                if (it.genericDocument.schemaType == ProductAS::class.java.simpleName) {
                    it.getDocument(ProductAS::class.java)
                } else null
            }
        }
    }

    fun closeSession() {
        session?.close()
        session = null
    }
}