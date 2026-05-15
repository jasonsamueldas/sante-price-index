package com.example.santeprice
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import com.example.santeprice.MandiPrice

private val Context.dataStore by preferencesDataStore(
    name = "sante_prefs"
)

class DataStoreManager(
    private val context: Context
) {

    companion object {

        val BOARD_KEY =
            stringPreferencesKey("board_items")
        val WATCHLIST_KEY =
            stringPreferencesKey("watchlist")
        val TRENDS_KEY =
            stringPreferencesKey("trends")
        val CACHED_PRICES_KEY =
            stringPreferencesKey("cached_prices")
        val MARGIN_KEY =
            stringPreferencesKey("margin")
        val WASTE_KEY =
            stringPreferencesKey("waste")
        val TRANSPORT_KEY =
            stringPreferencesKey("transport")
        // NEW: Keys for storing trend points by commodity
        fun getTrendPointsKey(commodity: String) =
            stringPreferencesKey("trend_points_$commodity")
    }

    suspend fun saveBoardItems(
        items: List<BoardItem>
    ) {

        val jsonArray = JSONArray()

        items.forEach {

            val obj = JSONObject()

            obj.put("name", it.name)
            obj.put("price", it.price)

            jsonArray.put(obj)
        }

        context.dataStore.edit { prefs ->

            prefs[BOARD_KEY] =
                jsonArray.toString()
        }
    }

    val boardItemsFlow = context.dataStore.data.map { prefs ->
        val json =
            prefs[BOARD_KEY] ?: "[]"
        val jsonArray = JSONArray(json)

        buildList {

            for (i in 0 until jsonArray.length()) {

                val obj =
                    jsonArray.getJSONObject(i)

                add(
                    BoardItem(
                        name = obj.getString("name"),
                        price = obj.getString("price")
                    )
                )
            }
        }
    }
    suspend fun saveWatchlist(
        items: List<String>
    ) {

        val jsonArray = JSONArray()

        items.forEach {
            jsonArray.put(it)
        }

        context.dataStore.edit { prefs ->

            prefs[WATCHLIST_KEY] =
                jsonArray.toString()
        }
    }
    val watchlistFlow = context.dataStore.data.map { prefs ->

        val json =
            prefs[WATCHLIST_KEY] ?: "[]"

        val jsonArray = JSONArray(json)

        buildList {

            for (i in 0 until jsonArray.length()) {

                add(
                    jsonArray.getString(i)
                )
            }
        }
    }
    suspend fun saveTrends(
        items: List<String>
    ) {

        val jsonArray = JSONArray()

        items.forEach {
            jsonArray.put(it)
        }

        context.dataStore.edit { prefs ->

            prefs[TRENDS_KEY] =
                jsonArray.toString()
        }
    }

    // NEW: Save trend points for a specific commodity
    suspend fun saveTrendPoints(
        commodity: String,
        points: List<TrendPoint>
    ) {
        val jsonArray = JSONArray()
        points.forEach { point ->
            val obj = JSONObject()
            obj.put("label", point.label)
            obj.put("price", point.price)
            jsonArray.put(obj)
        }

        context.dataStore.edit { prefs ->
            prefs[getTrendPointsKey(commodity)] =
                jsonArray.toString()
        }
    }

    // NEW: Retrieve cached trend points for a commodity
    suspend fun getTrendPoints(
        commodity: String
    ): List<TrendPoint> {
        return try {
            val prefs = context.dataStore.data.map { it }.collect {
                val json = it[getTrendPointsKey(commodity)] ?: "[]"
                val jsonArray = JSONArray(json)
                buildList {
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        add(
                            TrendPoint(
                                label = obj.getString("label"),
                                price = obj.getDouble("price")
                            )
                        )
                    }
                }
            }
            emptyList() // This is a placeholder - see alternative below
        } catch (e: Exception) {
            emptyList()
        }
    }

    // BETTER: Return flow directly for cached trend points
    fun getTrendPointsFlow(commodity: String) =
        context.dataStore.data.map { prefs ->
            val json = prefs[getTrendPointsKey(commodity)] ?: "[]"
            val jsonArray = JSONArray(json)

            buildList {
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    add(
                        TrendPoint(
                            label = obj.getString("label"),
                            price = obj.getDouble("price")
                        )
                    )
                }
            }
        }

    suspend fun saveCachedPrices(
        items: List<MandiPrice>
    ) {
        val jsonArray = JSONArray()
        items.forEach {
            val obj = JSONObject()
            obj.put("commodity", it.commodity)
            obj.put("market", it.market)
            obj.put("state", it.state)
            obj.put("modalPrice", it.modalPrice)
            obj.put("minPrice", it.minPrice)
            obj.put("maxPrice", it.maxPrice)
            obj.put("arrivalDate", it.arrivalDate)
            jsonArray.put(obj)
        }
        context.dataStore.edit { prefs ->
            prefs[CACHED_PRICES_KEY] =
                jsonArray.toString()
        }
    }

    suspend fun saveCalculatorPrefs(
        margin: Float,
        waste: Float,
        transport: String
    ) {

        context.dataStore.edit { prefs ->

            prefs[MARGIN_KEY] =
                margin.toString()

            prefs[WASTE_KEY] =
                waste.toString()

            prefs[TRANSPORT_KEY] =
                transport
        }
    }

    val trendsFlow = context.dataStore.data.map { prefs ->

        val json =
            prefs[TRENDS_KEY] ?: "[]"

        val jsonArray = JSONArray(json)

        buildList {

            for (i in 0 until jsonArray.length()) {

                add(
                    jsonArray.getString(i)
                )
            }
        }
    }
    val cachedPricesFlow =
        context.dataStore.data.map { prefs ->

            val json =
                prefs[CACHED_PRICES_KEY] ?: "[]"

            val jsonArray = JSONArray(json)

            buildList {

                for (i in 0 until jsonArray.length()) {

                    val obj =
                        jsonArray.getJSONObject(i)

                    add(
                        MandiPrice(
                            commodity =
                                obj.getString("commodity"),
                            market =
                                obj.getString("market"),
                            state =
                                obj.getString("state"),
                            modalPrice =
                                obj.getDouble("modalPrice"),
                            minPrice =
                                obj.getDouble("minPrice"),
                            maxPrice =
                                obj.getDouble("maxPrice"),
                            arrivalDate =
                                obj.getString("arrivalDate")
                        )
                    )
                }
            }
        }
    val calculatorPrefsFlow =
        context.dataStore.data.map { prefs ->

            Triple(

                prefs[MARGIN_KEY]
                    ?.toFloatOrNull() ?: 20f,

                prefs[WASTE_KEY]
                    ?.toFloatOrNull() ?: 15f,

                prefs[TRANSPORT_KEY] ?: ""
            )
        }
}