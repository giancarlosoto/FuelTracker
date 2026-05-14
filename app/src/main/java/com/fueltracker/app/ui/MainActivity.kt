package com.fueltracker.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fueltracker.app.data.FuelEntry
import com.fueltracker.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main Activity — the phone experience.
 * Shows a premium dashboard with fuel efficiency stats,
 * spending charts, and the complete refueling history.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FuelTrackerTheme {
                FuelTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelTrackerApp(viewModel: MainViewModel = viewModel()) {
    val entries by viewModel.allEntries.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val totalFuel by viewModel.totalFuelConsumed.collectAsState()
    val avgPrice by viewModel.averageUnitPrice.collectAsState()
    val efficiency by viewModel.currentEfficiency.collectAsState()
    val costPerKm by viewModel.costPerKm.collectAsState()
    val showDialog by viewModel.showAddDialog.collectAsState()

    Scaffold(
        containerColor = PrimaryDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = PrimaryAccent,
                contentColor = PrimaryDark,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Registrar carga")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== HEADER =====
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "⛽ FuelTracker",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toyota Yaris Cross Híbrido",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ===== EFFICIENCY HERO CARD =====
            item {
                HeroEfficiencyCard(
                    efficiency = efficiency,
                    costPerKm = costPerKm
                )
            }

            // ===== STATS GRID =====
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Payments,
                        label = "Total Gastado",
                        value = "S/ %.2f".format(totalSpent),
                        accentColor = SecondaryOrange
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.LocalGasStation,
                        label = "Galones Total",
                        value = "%.1f gal".format(totalFuel),
                        accentColor = SecondaryBlue
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.TrendingUp,
                        label = "Precio Prom.",
                        value = "S/ %.2f/gal".format(avgPrice),
                        accentColor = SecondaryPurple
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.FormatListNumbered,
                        label = "Total Cargas",
                        value = "${entries.size}",
                        accentColor = SecondaryGreen
                    )
                }
            }

            // ===== HISTORY =====
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Historial de Cargas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
            }

            if (entries.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    FuelEntryCard(
                        entry = entry,
                        previousEntry = entries.getOrNull(entries.indexOf(entry) + 1),
                        onDelete = { viewModel.deleteEntry(entry) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // ===== ADD ENTRY DIALOG =====
    if (showDialog) {
        AddFuelEntryDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { cost, unitPrice, odometer, station, notes ->
                viewModel.addEntry(cost, unitPrice, odometer, station, notes)
            }
        )
    }
}

// ==========================================
// COMPONENTS
// ==========================================

@Composable
fun HeroEfficiencyCard(
    efficiency: Double?,
    costPerKm: Double?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PrimaryAccent.copy(alpha = 0.15f),
                            SecondaryBlue.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Rendimiento Actual",
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryAccent
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = efficiency?.let { "%.1f".format(it) } ?: "—",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = if (efficiency != null && efficiency > 40) SecondaryGreen
                        else if (efficiency != null) SecondaryOrange
                        else TextMuted
                    )
                    Text(
                        text = "km/gal",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Costo por km",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = costPerKm?.let { "S/ %.3f".format(it) } ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Fuente de datos",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "📱 Manual / Voz",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryMedium)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
fun FuelEntryCard(
    entry: FuelEntry,
    previousEntry: FuelEntry?,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "PE")) }
    val efficiency = if (previousEntry != null) {
        val distance = entry.odometerKm - previousEntry.odometerKm
        if (distance > 0 && entry.quantityAdded > 0) distance / entry.quantityAdded else null
    } else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryMedium)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row: date and delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Data source badge
                    val (badgeText, badgeColor) = when (entry.dataSource) {
                        "auto" -> "🚗 Auto" to SecondaryGreen
                        "voice" -> "🎤 Voz" to SecondaryBlue
                        else -> "📱 Manual" to TextMuted
                    }
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Eliminar",
                            tint = SecondaryRed.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main data row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DataChip(
                    icon = Icons.Outlined.Payments,
                    value = "S/ %.2f".format(entry.totalCost),
                    label = "Monto",
                    color = SecondaryOrange
                )
                DataChip(
                    icon = Icons.Outlined.LocalGasStation,
                    value = "%.2f gal".format(entry.quantityAdded),
                    label = "Cantidad",
                    color = SecondaryBlue
                )
                DataChip(
                    icon = Icons.Outlined.Speed,
                    value = "%.0f km".format(entry.odometerKm),
                    label = "Odómetro",
                    color = PrimaryAccent
                )
            }

            // Efficiency row (if calculable)
            if (efficiency != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (efficiency > 40) SecondaryGreen.copy(alpha = 0.1f)
                            else SecondaryOrange.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⚡ Rendimiento este tramo",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "%.1f km/gal".format(efficiency),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (efficiency > 40) SecondaryGreen else SecondaryOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Station name
            if (!entry.stationName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📍 ${entry.stationName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DataChip(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "⛽", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin registros todavía",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Presiona + para registrar tu primera carga de combustible.\n\nTambién puedes decir: \"Hey Google, registrar carga en FuelTracker\"",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        totalCost: Double,
        unitPrice: Double,
        odometerKm: Double,
        stationName: String?,
        notes: String?
    ) -> Unit
) {
    var totalCost by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var odometerKm by remember { mutableStateOf("") }
    var stationName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PrimaryMedium,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text("⛽ Registrar Carga", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = totalCost,
                    onValueChange = { totalCost = it; hasError = false },
                    label = { Text("Monto Total (S/)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = hasError && totalCost.toDoubleOrNull() == null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        cursorColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    )
                )
                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it; hasError = false },
                    label = { Text("Precio Unitario (S/ por galón)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = hasError && unitPrice.toDoubleOrNull() == null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        cursorColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    )
                )
                OutlinedTextField(
                    value = odometerKm,
                    onValueChange = { odometerKm = it; hasError = false },
                    label = { Text("Kilometraje Actual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = hasError && odometerKm.toDoubleOrNull() == null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        cursorColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    )
                )
                OutlinedTextField(
                    value = stationName,
                    onValueChange = { stationName = it },
                    label = { Text("Grifo (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        cursorColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    )
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        cursorColor = PrimaryAccent,
                        focusedLabelColor = PrimaryAccent
                    )
                )

                // Calculated gallons preview
                val cost = totalCost.toDoubleOrNull()
                val price = unitPrice.toDoubleOrNull()
                if (cost != null && price != null && price > 0) {
                    val gallons = cost / price
                    Text(
                        text = "📊 Galones calculados: %.2f gal".format(gallons),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cost = totalCost.toDoubleOrNull()
                    val price = unitPrice.toDoubleOrNull()
                    val odo = odometerKm.toDoubleOrNull()
                    if (cost != null && price != null && price > 0 && odo != null) {
                        onConfirm(
                            cost,
                            price,
                            odo,
                            stationName.ifBlank { null },
                            notes.ifBlank { null }
                        )
                    } else {
                        hasError = true
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryAccent)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextMuted)
            ) {
                Text("Cancelar")
            }
        }
    )
}
