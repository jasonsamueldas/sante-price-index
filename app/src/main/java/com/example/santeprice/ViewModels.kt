package com.example.santeprice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// ═══════════════════════════════════════════════════════════════════════════
// DATA MODELS
// ═══════════════════════════════════════════════════════════════════════════

data class TrendPoint(
    val label: String,
    val price: Double
)

data class BoardItem(
    val name: String,
    val price: String
)

data class TrendsState(
    val commodity: String = "Onion",
    val points: List<TrendPoint> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false
)

data class CalcResult(
    val effectiveCostPerKg: Double,
    val breakEvenPrice: Double,
    val suggestedPrice: Double,
    val estimatedProfitPerKg: Double,
    val totalRevenue: Double,
    val totalCost: Double,
    val totalProfit: Double,
    val quantity: Double,
    val targetMargin: Int
)

// Extension function for rounding
private fun Double.round2(): Double {
    return (this * 100).roundToInt() / 100.0
}


// ═══════════════════════════════════════════════════════════════════════════
// WATCH VIEW MODEL
// ═══════════════════════════════════════════════════════════════════════════

class WatchViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(getApplication())

    private val _state = MutableStateFlow(PriceState(isLoading = true))
    val state: StateFlow<PriceState> = _state.asStateFlow()

    private val _watchlist = MutableStateFlow(listOf("Onion", "Tomato", "Potato"))
    val watchlist: StateFlow<List<String>> = _watchlist.asStateFlow()

    private var currentPrices: Map<String, Double> = emptyMap()
    private var previousPrices: Map<String, Double> = emptyMap()

    private val _availableCommodities = MutableStateFlow<List<String>>(emptyList())
    val availableCommodities: StateFlow<List<String>> = _availableCommodities.asStateFlow()

    init {
        loadCommodityNames()
        viewModelScope.launch {
            dataStoreManager.watchlistFlow.collect { savedWatchlist ->
                if (savedWatchlist.isNotEmpty()) {
                    _watchlist.value = savedWatchlist
                }
                loadPrices()
            }
        }
    }

    fun loadCommodityNames() {
        viewModelScope.launch {
            PriceRepository.fetchCommodityNames().onSuccess { commodities ->
                _availableCommodities.value = commodities
            }
        }
    }

    private fun currentTime(): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }

    fun loadPrices() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val allPrices = mutableListOf<MandiPrice>()

            _watchlist.value.forEach { commodity ->
                PriceRepository.fetchPrices(commodity, limit = 1)
                    .onSuccess { prices ->
                        prices.firstOrNull()?.let { allPrices.add(it) }
                    }
            }

            if (allPrices.isNotEmpty()) {
                previousPrices = currentPrices
                currentPrices = allPrices.associate { it.commodity to it.modalPrice }

                viewModelScope.launch {
                    dataStoreManager.saveCachedPrices(allPrices)
                }

                _state.value = PriceState(
                    prices = allPrices,
                    isLoading = false,
                    lastUpdated = currentTime()
                )
            } else {
                viewModelScope.launch {
                    dataStoreManager.cachedPricesFlow.collect { cachedPrices ->
                        if (cachedPrices.isNotEmpty()) {
                            _state.value = PriceState(
                                prices = cachedPrices,
                                isLoading = false,
                                isOffline = true
                            )
                        } else {
                            _state.value = PriceState(
                                isLoading = false,
                                error = "No data returned"
                            )
                        }
                    }
                }
            }
        }
    }

    fun getPriceChange(price: MandiPrice): String {
        val prev = previousPrices[price.commodity] ?: return "--"
        val delta = price.modalPrice - prev
        return if (delta >= 0) "+%.1f".format(delta) else "%.1f".format(delta)
    }

    fun addCommodity(name: String) {
        if (name.isBlank()) return

        val formatted = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        if (!_watchlist.value.contains(formatted)) {
            _watchlist.value = _watchlist.value + formatted
            viewModelScope.launch {
                dataStoreManager.saveWatchlist(_watchlist.value)
            }
            loadPrices()
        }
    }

    fun removeCommodity(name: String) {
        _watchlist.value = _watchlist.value - name
        viewModelScope.launch {
            dataStoreManager.saveWatchlist(_watchlist.value)
        }
        loadPrices()
    }
}


// ═══════════════════════════════════════════════════════════════════════════
// CALC VIEW MODEL
// ═══════════════════════════════════════════════════════════════════════════

class CalcViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(getApplication())

    val purchasePrice = MutableStateFlow("")
    val wastePercent = MutableStateFlow(15f)
    val transportCost = MutableStateFlow("")
    val quantity = MutableStateFlow("")
    val targetMargin = MutableStateFlow(20f)

    private val _result = MutableStateFlow<CalcResult?>(null)
    val result: StateFlow<CalcResult?> = _result.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreManager.calculatorPrefsFlow.collect { prefs ->
                targetMargin.value = prefs.first
                wastePercent.value = prefs.second
                transportCost.value = prefs.third
            }
        }
    }

    fun calculate() {
        val purchase = purchasePrice.value.toDoubleOrNull()
        if (purchase == null || purchase <= 0) {
            _error.value = "ENTER A VALID PURCHASE PRICE"
            return
        }

        val qty = quantity.value.toDoubleOrNull()
        if (qty == null || qty <= 0) {
            _error.value = "ENTER A VALID QUANTITY"
            return
        }

        _error.value = null

        val waste = wastePercent.value / 100.0
        val transport = transportCost.value.toDoubleOrNull() ?: 0.0

        if (transport < 0) {
            _error.value = "Transport cost cannot be negative"
            return
        }

        val margin = targetMargin.value / 100.0

        if (waste >= 1.0) {
            _error.value = "Waste cannot be 100%"
            return
        }

        if (margin >= 1.0) {
            _error.value = "Margin too high"
            return
        }

        val effectiveCost = (purchase + transport) / (1.0 - waste)
        val breakEven = effectiveCost
        val suggested = effectiveCost / (1.0 - margin)
        val profitPerKg = suggested - effectiveCost
        val totalRevenue = suggested * qty
        val totalCost = effectiveCost * qty
        val totalProfit = totalRevenue - totalCost

        _result.value = CalcResult(
            effectiveCostPerKg = effectiveCost.round2(),
            breakEvenPrice = breakEven.round2(),
            suggestedPrice = suggested.round2(),
            estimatedProfitPerKg = profitPerKg.round2(),
            totalRevenue = totalRevenue.round2(),
            totalCost = totalCost.round2(),
            totalProfit = totalProfit.round2(),
            quantity = qty.round2(),
            targetMargin = (margin * 100).roundToInt()
        )

        viewModelScope.launch {
            dataStoreManager.saveCalculatorPrefs(
                margin = targetMargin.value,
                waste = wastePercent.value,
                transport = transportCost.value
            )
        }
    }

    fun reset() {
        purchasePrice.value = ""
        wastePercent.value = 15f
        transportCost.value = ""
        targetMargin.value = 20f
        quantity.value = ""
        _result.value = null
        _error.value = null
    }
}


// ═══════════════════════════════════════════════════════════════════════════
// TRENDS VIEW MODEL (WITH OFFLINE CACHING)
// ═══════════════════════════════════════════════════════════════════════════

class TrendsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(getApplication())

    private val _commodities = MutableStateFlow(
        listOf("Onion", "Tomato", "Potato", "Garlic", "Ginger")
    )
    val commodities: StateFlow<List<String>> = _commodities.asStateFlow()

    private val _availableCommodities = MutableStateFlow<List<String>>(emptyList())
    val availableCommodities: StateFlow<List<String>> = _availableCommodities.asStateFlow()

    private val _state = MutableStateFlow(TrendsState(isLoading = true))
    val state: StateFlow<TrendsState> = _state.asStateFlow()

    init {
        loadCommodityNames()
        viewModelScope.launch {
            dataStoreManager.trendsFlow.collect { savedCommodities ->
                if (savedCommodities.isNotEmpty()) {
                    _commodities.value = savedCommodities
                }
                loadTrend(_commodities.value.first())
            }
        }
    }

    fun loadCommodityNames() {
        viewModelScope.launch {
            PriceRepository.fetchCommodityNames().onSuccess { names ->
                _availableCommodities.value = names
            }
        }
    }

    fun loadTrend(commodity: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                commodity = commodity,
                isLoading = true,
                error = null
            )

            PriceRepository.fetchPrices(commodity, limit = 7)
                .onSuccess { prices ->
                    val sortedPrices = prices.sortedBy { it.arrivalDate }
                    val points = sortedPrices.mapIndexed { idx, price ->
                        TrendPoint(
                            label = price.arrivalDate.takeLast(5).ifEmpty { "D${idx + 1}" },
                            price = price.modalPrice
                        )
                    }

                    // Save to offline cache
                    viewModelScope.launch {
                        dataStoreManager.saveTrendPoints(commodity, points)
                    }

                    _state.value = TrendsState(
                        commodity = commodity,
                        points = points,
                        isLoading = false
                    )
                }
                .onFailure { err ->
                    // Load cached data on failure
                    viewModelScope.launch {
                        dataStoreManager.getTrendPointsFlow(commodity).collect { cachedPoints ->
                            if (cachedPoints.isNotEmpty()) {
                                _state.value = TrendsState(
                                    commodity = commodity,
                                    points = cachedPoints,
                                    isLoading = false,
                                    isOffline = true
                                )
                            } else {
                                _state.value = TrendsState(
                                    commodity = commodity,
                                    isLoading = false,
                                    error = err.message ?: "Failed to load trend",
                                    isOffline = true
                                )
                            }
                        }
                    }
                }
        }
    }

    fun addCommodity(name: String) {

        if (name.isBlank()) return

        val formatted = name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase()
            else it.toString()
        }

        if (!_commodities.value.contains(formatted)) {

            // Update UI immediately
            _commodities.value =
                _commodities.value + formatted

            // Save tracker list
            viewModelScope.launch {
                dataStoreManager.saveTrends(
                    _commodities.value
                )
            }

            // Fetch/cache in background
            viewModelScope.launch {

                PriceRepository.fetchPrices(
                    formatted,
                    limit = 7
                ).onSuccess { prices ->

                    val sortedPrices =
                        prices.sortedBy {
                            it.arrivalDate
                        }

                    val points =
                        sortedPrices.mapIndexed { idx, price ->

                            TrendPoint(
                                label =
                                    price.arrivalDate
                                        .takeLast(5)
                                        .ifEmpty { "D${idx + 1}" },

                                price = price.modalPrice
                            )
                        }

                    dataStoreManager.saveTrendPoints(
                        formatted,
                        points
                    )
                }
            }
        }
    }

    fun removeCommodity(name: String) {
        _commodities.value = _commodities.value - name
        if (_commodities.value.isEmpty()) {
            _commodities.value = listOf("Onion")
        }

        viewModelScope.launch {
            dataStoreManager.saveTrends(_commodities.value)
        }

        if (state.value.commodity == name) {
            loadTrend(_commodities.value.first())
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════
// BOARD VIEW MODEL
// ═══════════════════════════════════════════════════════════════════════════

class BoardViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(getApplication())

    private val _availableCommodities = MutableStateFlow<List<String>>(emptyList())
    val availableCommodities: StateFlow<List<String>> = _availableCommodities.asStateFlow()

    private val _items = MutableStateFlow<List<BoardItem>>(emptyList())
    val items: StateFlow<List<BoardItem>> = _items.asStateFlow()

    private val _isPresentMode = MutableStateFlow(false)
    val isPresentMode: StateFlow<Boolean> = _isPresentMode.asStateFlow()

    init {
        loadCommodityNames()
        viewModelScope.launch {
            dataStoreManager.boardItemsFlow.collect { boardItems ->
                _items.value = boardItems
            }
        }
    }

    fun loadCommodityNames() {
        viewModelScope.launch {
            PriceRepository.fetchCommodityNames().onSuccess { names ->
                _availableCommodities.value = names
            }
        }
    }

    fun addItem(name: String, price: String) {
        val parsed = price.toDoubleOrNull()
        if (name.isBlank() || parsed == null || parsed <= 0) return
        if (_items.value.any { it.name.equals(name, ignoreCase = true) }) return

        _items.value = _items.value + BoardItem(name.trim(), price.trim())

        viewModelScope.launch {
            dataStoreManager.saveBoardItems(_items.value)
        }
    }

    fun removeItem(item: BoardItem) {
        _items.value = _items.value - item
        viewModelScope.launch {
            dataStoreManager.saveBoardItems(_items.value)
        }
    }

    fun moveUp(item: BoardItem) {
        val current = _items.value.toMutableList()
        val index = current.indexOf(item)
        if (index > 0) {
            current.removeAt(index)
            current.add(index - 1, item)
            _items.value = current
            viewModelScope.launch {
                dataStoreManager.saveBoardItems(_items.value)
            }
        }
    }

    fun moveDown(item: BoardItem) {
        val current = _items.value.toMutableList()
        val index = current.indexOf(item)
        if (index < current.lastIndex) {
            current.removeAt(index)
            current.add(index + 1, item)
            _items.value = current
            viewModelScope.launch {
                dataStoreManager.saveBoardItems(_items.value)
            }
        }
    }

    fun updatePrice(item: BoardItem, newPrice: String) {
        val parsed = newPrice.toDoubleOrNull()
        if (newPrice.isNotBlank() && ((parsed == null && newPrice != ".") || (parsed != null && parsed <= 0))) {
            return
        }

        _items.value = _items.value.map { boardItem ->
            if (boardItem == item) {
                boardItem.copy(price = newPrice)
            } else {
                boardItem
            }
        }

        viewModelScope.launch {
            dataStoreManager.saveBoardItems(_items.value)
        }
    }

    fun togglePresentMode() {
        _isPresentMode.value = !_isPresentMode.value
    }
}