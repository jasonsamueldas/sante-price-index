package com.example.santeprice

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// --- THEME ---

val SafetyYellow = Color(0xFFFFB800)
val DarkSurface = Color(0xFF131313)
val CardBackground = Color(0xFF1F2020)
val ErrorRed = Color(0xFF93000A)

@Composable
fun SantePriceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = SafetyYellow,
            surface = DarkSurface,
            background = Color.Black,
            onSurface = Color.White,
            onBackground = Color.White,
            onPrimary = Color.Black
        ),
        content = content
    )
}

// --- APP ENTRY ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SantePriceApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var openWatchDialog by remember {
        mutableStateOf(false)
    }
    var openBoardDialog by remember {
        mutableStateOf(false)
    }
    var openTrendsDialog by remember {
        mutableStateOf(false)
    }
    SantePriceTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "SANTE-PRICE INDEX",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontSize = 18.sp,
                            color = SafetyYellow
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* Drawer */ }) {
                            Icon(Icons.Default.Menu, "Menu", tint = SafetyYellow)
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Cycle Lang */ }) {
                            Icon(Icons.Default.Language, "Language", tint = SafetyYellow)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
                )
            },
            bottomBar = { BottomNavigationBar(navController, currentRoute) },
            floatingActionButton = {

                if (
                    currentRoute == "watch" ||
                    currentRoute == "board" ||
                    currentRoute == "trends"
                ) {

                    FloatingActionButton(

                        onClick = {

                            if (currentRoute == "watch") {
                                openWatchDialog = true
                            }

                            if (currentRoute == "board") {
                                openBoardDialog = true
                            }
                            if (currentRoute == "trends") {
                                openTrendsDialog = true
                            }
                        },

                        containerColor = SafetyYellow,

                        contentColor = Color.Black,

                        shape = RectangleShape,

                        modifier = Modifier.border(
                            2.dp,
                            Color.Black
                        )
                    ) {

                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add"
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "watch",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("watch") {

                    val vm: WatchViewModel = viewModel()

                    WatchScreen(
                        vm = vm,

                        openDialog = openWatchDialog,

                        onDialogHandled = {
                            openWatchDialog = false
                        }
                    )
                }
                composable("calc") {
                    val vm: CalcViewModel = viewModel()
                    CalcScreen(vm)
                }
                composable("board") {

                    val vm: BoardViewModel = viewModel()

                    BoardScreen(
                        vm = vm,

                        openDialog = openBoardDialog,

                        onDialogHandled = {
                            openBoardDialog = false
                        }
                    )
                }
                composable("trends") {
                    val vm: TrendsViewModel = viewModel()
                    TrendsScreen(
                        vm = vm,
                        openDialog = openTrendsDialog,
                        onDialogHandled = {
                            openTrendsDialog = false
                        }
                    )
                }
                composable("learn") {
                    LearnScreen { navController.navigate("calc") }
                }
            }
        }
    }
}

// --- NAVIGATION ---

@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String?) {
    val items = listOf(
        NavigationItem("WATCH", "watch", Icons.Default.Dashboard),
        NavigationItem("CALC", "calc", Icons.Default.Calculate),
        NavigationItem("BOARD", "board", Icons.Default.BarChart),
        NavigationItem("TRENDS", "trends", Icons.Default.TrendingUp),
        NavigationItem("LEARN", "learn", Icons.Default.School)
    )
    NavigationBar(containerColor = Color.Black, tonalElevation = 0.dp) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color.Black,
                    indicatorColor = SafetyYellow,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

data class NavigationItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// ─────────────────────────────────────────────
// WATCH SCREEN
// ─────────────────────────────────────────────

@Composable
fun WatchScreen(
    vm: WatchViewModel = viewModel(),
    openDialog: Boolean,
    onDialogHandled: () -> Unit
) {

    val state by vm.state.collectAsState()
    val availableCommodities
            by vm.availableCommodities.collectAsState()

    var showDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(openDialog) {
        if (openDialog) {
            showDialog = true
            onDialogHandled()
        }
    }

    // ADD COMMODITY DIALOG

    if (showDialog) {

        var commodity by remember {
            mutableStateOf("")
        }

        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },

            confirmButton = {
                Button(
                    onClick = {
                        vm.addCommodity(commodity)
                        showDialog = false
                    }
                ) {
                    Text("ADD")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("CANCEL")
                }
            },

            title = {
                Text("Add Commodity")
            },

            text = {

                var expanded by remember {
                    mutableStateOf(false)
                }

                Column {

                    OutlinedTextField(

                        value = commodity,

                        onValueChange = {},

                        readOnly = true,

                        label = {
                            Text("Select Commodity")
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    expanded = true
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false

                    )

                    DropdownMenu(

                        expanded = expanded,

                        onDismissRequest = {
                            expanded = false
                        }
                    ) {

                        availableCommodities.forEach { item ->

                            DropdownMenuItem(

                                text = {
                                    Text(item)
                                },

                                onClick = {

                                    commodity = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        )
    }

    // MAIN UI

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // HEADER

        Row(
            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement = Arrangement.SpaceBetween,

            verticalAlignment = Alignment.Bottom
        ) {

            Column {

                Text(
                    "PRICE WATCH",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Column {
                    Text(
                        if (state.isLoading)
                            "FETCHING DATA..."
                        else {
                            if (state.isOffline)
                                "OFFLINE | Displaying Cached Data"
                            else
                                "LIVE FROM AGMARKNET"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    state.lastUpdated?.let {
                        Text(
                            "LAST UPDATED: $it",
                            fontSize = 9.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    vm.loadPrices()
                }
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = SafetyYellow
                )
            }
        }

        // LOADING

        when {

            state.isLoading -> {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),

                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = SafetyYellow
                    )
                }
            }

            // ERROR

            state.error != null -> {

                ErrorBanner(state.error!!) {
                    vm.loadPrices()
                }
            }

            // EMPTY

            state.prices.isEmpty() -> {

                Text(
                    "NO DATA AVAILABLE.",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // DATA

            else -> {

                state.prices.forEach { price ->

                    Column {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier.weight(1f)
                            ) {

                                val change = vm.getPriceChange(price)
                                val changeVal =
                                    change.toDoubleOrNull() ?: 0.0

                                PriceCardUI(
                                    name = price.commodity,
                                    price = "%.1f".format(price.modalPrice),

                                    mandi =
                                        "${price.market}, ${price.state}",

                                    change = change,

                                    isDown = changeVal < 0,

                                    isStable = change == "--" || changeVal == 0.0
                                )
                            }

                            IconButton(
                                onClick = {
                                    vm.removeCommodity(
                                        price.commodity
                                    )
                                }
                            ) {

                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = ErrorRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // MIN MAX STRIP

        if (
            state.prices.isNotEmpty() &&
            !state.isLoading
        ) {

            Spacer(
                Modifier.height(8.dp)
            )

            state.prices.forEach { p ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground)
                        .border(
                            1.dp,
                            Color(0xFF353535)
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 10.dp
                        ),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        p.commodity.uppercase(),

                        fontSize = 11.sp,

                        color = Color.Gray,

                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(16.dp)
                    ) {

                        PriceTag(
                            "MIN",
                            "₹${p.minPrice.toInt()}",
                            Color(0xFF4CAF50)
                        )

                        PriceTag(
                            "MAX",
                            "₹${p.maxPrice.toInt()}",
                            ErrorRed
                        )

                        PriceTag(
                            "MODAL",
                            "₹${p.modalPrice.toInt()}",
                            SafetyYellow
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PriceTag(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ErrorRed)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("ERROR: $message", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(
            "Make sure your API key is set in PriceRepository.kt\nGet a free key at data.gov.in",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp
        )
        TextButton(onClick = onRetry) {
            Text("RETRY", color = SafetyYellow, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun PriceCardUI(
    name: String,
    price: String,
    mandi: String,
    change: String,
    isDown: Boolean,
    isStable: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(CardBackground)
            .border(1.dp, Color(0xFF353535)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier
            .weight(1f)
            .padding(16.dp)) {
            Text(name.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("₹$price", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = SafetyYellow)
                Text("/qtl", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
            }
            Text(mandi.uppercase(), fontSize = 10.sp, color = Color.DarkGray)
        }
        val bgColor = if (isStable) Color(0xFF353535) else if (isDown) ErrorRed else SafetyYellow
        val iconColor = if (isDown && !isStable) Color.White else Color.Black

        Column(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
                .background(bgColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                if (isStable) Icons.Default.Remove
                else if (isDown) Icons.Default.TrendingDown
                else Icons.Default.TrendingUp,
                contentDescription = null,
                tint = iconColor
            )
            Text(change, color = iconColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────
// CALC SCREEN
// ─────────────────────────────────────────────

@Composable
fun CalcScreen(vm: CalcViewModel = viewModel()) {
    val purchasePrice by vm.purchasePrice.collectAsState()
    val wastePercent by vm.wastePercent.collectAsState()
    val transportCost by vm.transportCost.collectAsState()
    val quantity by vm.quantity.collectAsState()
    val targetMargin by vm.targetMargin.collectAsState()
    val result by vm.result.collectAsState()
    val error by vm.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("PROFIT\nCALCULATOR", fontSize = 28.sp, fontWeight = FontWeight.Black, color = SafetyYellow, lineHeight = 30.sp)
            if (result != null) {
                TextButton(onClick = { vm.reset() }) {
                    Text("RESET", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Purchase Price
        OutlinedTextField(
            value = purchasePrice,
            onValueChange = { vm.purchasePrice.value = it },
            label = { Text("PURCHASE PRICE / KG (₹)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            prefix = { Text("₹", color = SafetyYellow) },
            isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SafetyYellow,
                unfocusedBorderColor = Color(0xFF353535),
                focusedLabelColor = SafetyYellow
            )
        )

        // Transport Cost (optional)
        OutlinedTextField(
            value = transportCost,
            onValueChange = {
                vm.transportCost.value = it
            },
            label = {
                Text("TRANSPORT COST (₹)")
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Decimal
                ),
            prefix = {
                Text(
                    "₹",
                    color = SafetyYellow
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SafetyYellow,
                unfocusedBorderColor = Color(0xFF353535),
                focusedLabelColor = SafetyYellow
            )
        )
        Spacer(
            modifier = Modifier.height(12.dp)
        )
        OutlinedTextField(
            value = quantity,
            onValueChange = {
                vm.quantity.value = it
            },
            label = {
                Text("QUANTITY (KG)")
            },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Decimal
                ),
            modifier = Modifier.fillMaxWidth()
        )

        // Waste Slider
        Column {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("WASTE / SPOILAGE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("${wastePercent.toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = SafetyYellow)
            }
            Slider(
                value = wastePercent,
                onValueChange = { vm.wastePercent.value = it },
                valueRange = 0f..60f,
                colors = SliderDefaults.colors(
                    thumbColor = SafetyYellow,
                    activeTrackColor = SafetyYellow,
                    inactiveTrackColor = Color(0xFF353535)
                )
            )
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("0%", fontSize = 10.sp, color = Color.DarkGray)
                Text("30%", fontSize = 10.sp, color = Color.DarkGray)
                Text("60%", fontSize = 10.sp, color = Color.DarkGray)
            }
        }

        // Target Margin Slider
        Column {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("TARGET MARGIN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("${targetMargin.toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = SafetyYellow)
            }
            Slider(
                value = targetMargin,
                onValueChange = { vm.targetMargin.value = it },
                valueRange = 5f..60f,
                colors = SliderDefaults.colors(
                    thumbColor = SafetyYellow,
                    activeTrackColor = SafetyYellow,
                    inactiveTrackColor = Color(0xFF353535)
                )
            )
        }

        if (error != null) {
            Text(error!!, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = { vm.calculate() },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = SafetyYellow, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.Calculate, "Calc", modifier = Modifier.padding(end = 8.dp))
            Text("CALCULATE", fontWeight = FontWeight.Black, fontSize = 16.sp)
        }

        // Results
        AnimatedVisibility(
            visible = result != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            result?.let { r ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground)
                        .border(1.dp, SafetyYellow)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("RESULTS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = SafetyYellow, letterSpacing = 2.sp)
                    HorizontalDivider(color = Color(0xFF353535))

                    ResultRow("EFFECTIVE COST / KG", "₹${r.effectiveCostPerKg}", Color.White, "After ${wastePercent.toInt()}% waste")
                    ResultRow("BREAK-EVEN PRICE", "₹${r.breakEvenPrice}", Color(0xFFFF6B35), "Zero profit threshold")
                    ResultRow("SUGGESTED SELL PRICE", "₹${r.suggestedPrice}", SafetyYellow, "At ${r.targetMargin}% margin", large = true)
                    InfoRow(
                        "PROFIT / KG",
                        "₹${r.estimatedProfitPerKg}"
                    )

                    InfoRow(
                        "QUANTITY",
                        "${r.quantity} KG"
                    )

                    InfoRow(
                        "TOTAL COST",
                        "₹${r.totalCost}"
                    )

                    InfoRow(
                        "GROSS SALES",
                        "₹${r.totalRevenue}"
                    )

                    InfoRow(
                        "NET PROFIT",
                        "₹${r.totalProfit}"
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Text(
            value,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun ResultRow(label: String, value: String, valueColor: Color, subtitle: String, large: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            if (subtitle.isNotEmpty()) Text(subtitle, fontSize = 9.sp, color = Color.DarkGray)
        }
        Text(
            value,
            fontSize = if (large) 24.sp else 18.sp,
            fontWeight = FontWeight.Black,
            color = valueColor
        )
    }
}

// ─────────────────────────────────────────────
// BOARD SCREEN  (uses same WatchViewModel)
// ─────────────────────────────────────────────

@Composable
fun BoardScreen(
    vm: BoardViewModel = viewModel(),
    openDialog: Boolean,
    onDialogHandled: () -> Unit
) {

    val items by vm.items.collectAsState()
    val isPresentMode by vm.isPresentMode.collectAsState()

    var showDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(openDialog) {

        if (openDialog) {
            showDialog = true
            onDialogHandled()
        }
    }
    // ADD PRODUCT DIALOG

    if (showDialog) {
        var expanded by remember {
            mutableStateOf(false)
        }
        var selectedCommodity by remember {
            mutableStateOf("")
        }
        var price by remember {
            mutableStateOf("")
        }
        val availableCommodities
                by vm.availableCommodities.collectAsState()

        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.addItem(
                            selectedCommodity,
                            price
                        )
                        showDialog = false
                    }
                ) {
                    Text("ADD")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("CANCEL")
                }
            },
            title = {
                Text("Add Product")
            },
            text = {

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    // PRODUCT DROPDOWN

                    OutlinedTextField(
                        value = selectedCommodity,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Select Product")
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    expanded = true
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        availableCommodities.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text(item)
                                },
                                onClick = {
                                    selectedCommodity = item
                                    expanded = false
                                }
                            )
                        }
                    }

                    // PRICE FIELD

                    OutlinedTextField(

                        value = price,
                        onValueChange = {
                            price = it
                        },
                        label = {
                            Text("Selling Price (₹/kg)")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType =
                                KeyboardType.Decimal
                        )
                    )
                }
            }
        )
    }
    // ─────────────────────────────────────────────
// EDIT MODE - Vendor can edit prices
// ─────────────────────────────────────────────



    if (isPresentMode) {
        // PRESENT MODE - Customer-facing display
        PresentModeBoard(items = items, onExitPresentMode = { vm.togglePresentMode() })
    } else {
        // EDIT MODE - Vendor management
        EditModeBoard(items = items, vm = vm)
    }
}

@Composable
fun EditModeBoard(items: List<BoardItem>, vm: BoardViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        item {

            Column(
                modifier = Modifier.padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 24.dp,
                    bottom = 12.dp
                )
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "SHOP BOARD",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyYellow
                        )

                        Text(
                            "YOUR SELLING PRICES",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    // PRESENT MODE BUTTON
                    Button(
                        onClick = { vm.togglePresentMode() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafetyYellow,
                            contentColor = Color.Black
                        ),
                        shape = RectangleShape,
                        modifier = Modifier.border(2.dp, Color.Black)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Present",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("PRESENT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        items(items) { item ->
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 20.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // PRODUCT NAME

                    Text(
                        item.name.uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    // PRICE + DELETE

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = item.price,
                            onValueChange = {
                                vm.updatePrice(item, it)
                            },
                            modifier = Modifier.width(120.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Decimal
                            )
                        )
                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )
                        Text(
                            "/kg",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Column {
                            IconButton(
                                onClick = {
                                    vm.moveUp(item)
                                }
                            ) {

                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Move Up",
                                    tint = SafetyYellow
                                )
                            }

                            IconButton(
                                onClick = {
                                    vm.moveDown(item)
                                }
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Move Down",
                                    tint = SafetyYellow
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                vm.removeItem(item)
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = ErrorRed
                            )
                        }
                    }
                }
                HorizontalDivider(
                    color = Color(0xFF1F1F1F)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// PRESENT MODE - Customer-facing display
// ─────────────────────────────────────────────

@Composable
fun PresentModeBoard(items: List<BoardItem>, onExitPresentMode: () -> Unit) {

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {

        // Exit button (small, top-right corner)
        IconButton(
            onClick = onExitPresentMode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(48.dp)
                .background(Color(0xFF1F1F1F), RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Default.Close, "Exit", tint = SafetyYellow, modifier = Modifier.size(24.dp))
        }

        // Main price display
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 80.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "TODAY'S PRICES",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = SafetyYellow,
                        letterSpacing = 4.sp,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                    HorizontalDivider(
                        thickness = 3.dp,
                        color = SafetyYellow,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }

            items(items) { item ->
                PresentModeItem(item)
            }

            // Footer spacing
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

}
@Composable
fun PresentModeItem(item: BoardItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .border(3.dp, SafetyYellow)
            .padding(horizontal = 40.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Product name
        Text(
            text = item.name.uppercase(),
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 3.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Price
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "₹",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = SafetyYellow,
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
            )
            Text(
                text = item.price,
                fontSize = 96.sp,
                fontWeight = FontWeight.Black,
                color = SafetyYellow,
                lineHeight = 96.sp
            )
            Text(
                text = "/kg",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
        }
    }
}



// ─────────────────────────────────────────────
// TRENDS SCREEN  (Canvas line chart — no lib)
// ─────────────────────────────────────────────

@Composable
fun TrendsScreen(
    vm: TrendsViewModel = viewModel(),
    openDialog: Boolean,
    onDialogHandled: () -> Unit
) {
    val state by vm.state.collectAsState()
    val commodities by vm.commodities.collectAsState()

    val availableCommodities
            by vm.availableCommodities.collectAsState()

    var showDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(openDialog) {

        if (openDialog) {

            showDialog = true
            onDialogHandled()
        }
    }
    if (showDialog) {

        var selectedCommodity by remember {
            mutableStateOf("")
        }

        var expanded by remember {
            mutableStateOf(false)
        }

        AlertDialog(

            onDismissRequest = {
                showDialog = false
            },

            confirmButton = {

                Button(
                    onClick = {

                        vm.addCommodity(
                            selectedCommodity
                        )

                        showDialog = false
                    }
                ) {
                    Text("ADD")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("CANCEL")
                }
            },

            title = {
                Text("Add Tracker Commodity")
            },

            text = {

                Column {

                    OutlinedTextField(

                        value = selectedCommodity,

                        onValueChange = {},

                        readOnly = true,

                        label = {
                            Text("Select Commodity")
                        },

                        trailingIcon = {

                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,

                                modifier = Modifier.clickable {
                                    expanded = true
                                }
                            )
                        },

                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(

                        expanded = expanded,

                        onDismissRequest = {
                            expanded = false
                        }
                    ) {

                        availableCommodities.forEach { item ->

                            DropdownMenuItem(

                                text = {
                                    Text(item)
                                },

                                onClick = {

                                    selectedCommodity = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        )
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Text("7-DAY TRACKER", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)

        // Commodity tabs
        ScrollableTabRow(
            selectedTabIndex = commodities.indexOf(state.commodity),
            containerColor = Color.Black,
            contentColor = SafetyYellow,
            edgePadding = 0.dp
        ) {
            commodities.forEachIndexed { _, commodity ->
                Tab(
                    selected = state.commodity == commodity,
                    onClick = { vm.loadTrend(commodity) },
                    text = {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                commodity.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color =
                                    if (state.commodity == commodity)
                                        SafetyYellow
                                    else
                                        Color.Gray
                            )

                            Spacer(
                                modifier = Modifier.width(4.dp)
                            )

                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = ErrorRed,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable {

                                        vm.removeCommodity(
                                            commodity
                                        )
                                    }
                            )
                        }
                    }
                )
            }
        }

        when {
            state.isLoading -> Box(Modifier
                .fillMaxWidth()
                .height(200.dp), Alignment.Center) {
                CircularProgressIndicator(color = SafetyYellow)
            }
            state.error != null -> ErrorBanner(state.error!!) { vm.loadTrend(state.commodity) }
            state.points.isEmpty() -> Box(Modifier
                .fillMaxWidth()
                .height(200.dp), Alignment.Center) {
                Text("NO TREND DATA", color = Color.Gray)
            }
            else -> {
                LineChartCanvas(points = state.points)

                // Summary stats
                val prices = state.points.map { it.price }
                val minP = prices.minOrNull() ?: 0.0
                val maxP = prices.maxOrNull() ?: 0.0
                val avgP = prices.average()
                val trend = if (prices.size >= 2) prices.last() - prices.first() else 0.0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox("7D LOW", "₹${minP.toInt()}", Color(0xFF4CAF50))
                    StatBox("7D HIGH", "₹${maxP.toInt()}", ErrorRed)
                    StatBox("AVG", "₹${avgP.toInt()}", SafetyYellow)
                    StatBox("CHANGE", "${if (trend >= 0) "+" else ""}${trend.toInt()}", if (trend >= 0) SafetyYellow else ErrorRed)
                }
            }
        }
    }
}

@Composable
fun LineChartCanvas(points: List<TrendPoint>) {
    val yellow = SafetyYellow
    val gridColor = Color(0xFF2A2A2A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(CardBackground)
            .border(1.dp, Color(0xFF353535))
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            if (points.size < 2) return@Canvas

            val prices = points.map { it.price }
            val minP = prices.min()
            val maxP = prices.max()
            val range = (maxP - minP).coerceAtLeast(1.0)

            val w = size.width
            val h = size.height

            // Grid lines
            for (i in 0..3) {
                val y = h * i / 3
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }

            // Path
            val path = Path()
            points.forEachIndexed { i, point ->
                val x = w * i / (points.size - 1)
                val y = h - (h * ((point.price - minP) / range)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, yellow, style = Stroke(width = 3f))

            // Dots
            points.forEachIndexed { i, point ->
                val x = w * i / (points.size - 1)
                val y = h - (h * ((point.price - minP) / range)).toFloat()
                drawCircle(yellow, radius = 5f, center = Offset(x, y))
                drawCircle(Color.Black, radius = 3f, center = Offset(x, y))
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Text(pt.label, fontSize = 8.sp, color = Color.Gray, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .background(CardBackground)
            .border(1.dp, Color(0xFF353535))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
    }
}

// ─────────────────────────────────────────────
// LEARN SCREEN
// ─────────────────────────────────────────────

@Composable
fun LearnScreen(onNavigateToCalc: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("LEARN", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = SafetyYellow)

        val concepts = listOf(
            Triple("GROSS SALES", "Total money collected from all sales before any costs are deducted.", Icons.Default.Payments),
            Triple("NET PROFIT", "Money left after subtracting purchase cost, transport, and waste losses.", Icons.Default.AccountBalance),
            Triple("WASTE FACTOR", "Spoilage or damage reduces usable stock. A 20% waste means only 80 kg sellable from 100 kg purchased.", Icons.Default.DeleteSweep),
            Triple("BREAK-EVEN", "The minimum price at which you recover all costs. Selling below this means a loss.", Icons.Default.Warning),
            Triple("MODAL PRICE", "The most common/frequent price in a mandi on a given day. Use this as your market reference.", Icons.Default.BarChart),
            Triple("MARGIN", "Profit as % of selling price. A 20% margin on ₹50/kg means ₹10 profit per kg.", Icons.Default.Percent)
        )

        concepts.forEach { (title, desc, icon) ->
            LearnCard(title, desc, icon)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onNavigateToCalc,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = SafetyYellow, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.Calculate, "Calc", modifier = Modifier.padding(end = 8.dp))
            Text("TRY THE CALCULATOR", fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}

@Composable
fun LearnCard(title: String, desc: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground)
            .border(1.dp, Color(0xFF353535))
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = SafetyYellow, modifier = Modifier.padding(top = 2.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = SafetyYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(desc, color = Color.Gray, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}