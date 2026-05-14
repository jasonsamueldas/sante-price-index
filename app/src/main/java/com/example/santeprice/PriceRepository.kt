package com.example.santeprice

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// --- DATA MODELS ---

data class MandiPrice(
    val commodity: String,
    val market: String,
    val state: String,
    val minPrice: Double,
    val maxPrice: Double,
    val modalPrice: Double,
    val arrivalDate: String
)

data class PriceState(
    val prices: List<MandiPrice> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: String? = null,
    val isOffline: Boolean = false
)

// --- REPOSITORY ---

object PriceRepository {

    // data.gov.in Agmarknet API
    // Register at https://data.gov.in to get a free API key
    // Replace YOUR_API_KEY below with your actual key
    private const val API_KEY = "WRITE YOUR API KEY HERE"
    private const val BASE_URL = "https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070"

    /**
     * Fetches current mandi prices for a given commodity.
     * @param commodity e.g. "Onion", "Tomato", "Potato"
     * @param limit number of records to fetch (default 10)
     */
    suspend fun fetchPrices(commodity: String, limit: Int = 10): Result<List<MandiPrice>> {
        return withContext(Dispatchers.IO) {
            try {
                val encodedCommodity = commodity.replace(" ", "%20")
                val url = "$BASE_URL?api-key=$API_KEY" +
                        "&format=json" +
                        "&filters[commodity]=$encodedCommodity" +
                        "&limit=$limit" +
                        "&fields=commodity,market,state,min_price,max_price,modal_price,arrival_date"

                val response = URL(url).readText()
                val json = JSONObject(response)
                val records = json.getJSONArray("records")

                val prices = mutableListOf<MandiPrice>()
                for (i in 0 until records.length()) {
                    val record = records.getJSONObject(i)
                    prices.add(
                        MandiPrice(
                            commodity = record.optString("commodity", commodity),
                            market = record.optString("market", "—"),
                            state = record.optString("state", "—"),
                            minPrice = record.optString("min_price", "0").toDoubleOrNull() ?: 0.0,
                            maxPrice = record.optString("max_price", "0").toDoubleOrNull() ?: 0.0,
                            modalPrice = record.optString("modal_price", "0").toDoubleOrNull() ?: 0.0,
                            arrivalDate = record.optString("arrival_date", "—")
                        )
                    )
                }
                Result.success(prices)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Fetches prices for the default watchlist of commodities.
     */
    suspend fun fetchWatchlistPrices(): Result<List<MandiPrice>> {
        val commodities = listOf("Onion", "Tomato", "Potato")
        val all = mutableListOf<MandiPrice>()

        for (commodity in commodities) {
            fetchPrices(commodity, limit = 1).onSuccess { prices ->
                prices.firstOrNull()?.let { all.add(it) }
            }
        }

        return if (all.isNotEmpty()) Result.success(all)
        else Result.failure(Exception("No data returned"))
    }

    suspend fun fetchCommodityNames(): Result<List<String>> {

        return withContext(Dispatchers.IO) {

            try {

                val url =
                    "$BASE_URL?api-key=$API_KEY" +
                            "&format=json" +
                            "&limit=200" +
                            "&fields=commodity"

                val response = URL(url).readText()

                val json = JSONObject(response)

                val records = json.getJSONArray("records")

                val commodities = mutableSetOf<String>()

                for (i in 0 until records.length()) {

                    val record = records.getJSONObject(i)

                    val commodity =
                        record.optString("commodity", "")

                    if (commodity.isNotBlank()) {
                        commodities.add(commodity)
                    }
                }

                Result.success(
                    commodities.sorted()
                )

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }
}
