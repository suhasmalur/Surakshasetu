package com.example.sosemergency

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var hasPermissions by mutableStateOf(false)

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                permissions[Manifest.permission.SEND_SMS] == true

        if (!hasPermissions) {
            Toast.makeText(this, "Permissions required for emergency alerts", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check and request permissions
        checkAndRequestPermissions()

        setContent {
            SOSEmergencyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SOSScreen(
                        fusedLocationClient = fusedLocationClient,
                        hasPermissions = hasPermissions,
                        onRequestPermissions = { checkAndRequestPermissions() }
                    )
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val fineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val sms = ContextCompat.checkSelfPermission(
            this, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        hasPermissions = fineLocation && sms

        if (!hasPermissions) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.SEND_SMS
                )
            )
        }
    }
}

@Composable
fun SOSEmergencyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}

@Composable
fun SOSScreen(
    fusedLocationClient: FusedLocationProviderClient,
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit
) {
    // HARDCODED PHONE NUMBER - CHANGE THIS TO YOUR EMERGENCY CONTACT
    val emergencyPhoneNumber = "+917019267392" // Replace with actual number

    // State variables
    var countdown by remember { mutableStateOf(0) }
    var alertSent by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large SOS Button
        Button(
            onClick = {
                if (!hasPermissions) {
                    onRequestPermissions()
                    return@Button
                }

                // Start countdown when button is pressed
                countdown = 3
                alertSent = false
                statusMessage = ""

                // Launch countdown coroutine
                coroutineScope.launch {
                    while (countdown > 0) {
                        delay(1000) // Wait 1 second
                        countdown--
                    }

                    // After countdown, get location and send SMS
                    try {
                        statusMessage = "Getting location..."

                        // Get current location
                        val location = fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            CancellationTokenSource().token
                        ).await()

                        if (location != null) {
                            val latitude = location.latitude
                            val longitude = location.longitude

                            // Create Google Maps link
                            val mapsLink = "https://maps.google.com/?q=13.07392285574783,77.49998795282163"

                            // Create SMS message
                            val message = "🚨 EMERGENCY SOS ALERT 🚨\n\n" +
                                    "I need help! My location:\n$mapsLink"

                            // Send SMS
                            val smsManager = context.getSystemService(SmsManager::class.java)
                            smsManager.sendTextMessage(
                                emergencyPhoneNumber,
                                null,
                                message,
                                null,
                                null
                            )

                            statusMessage = "Alert sent with location!"
                            alertSent = true

                        } else {
                            // Location failed, send SMS without location
                            val message = "🚨 EMERGENCY SOS ALERT 🚨\n\n" +
                                    "I need help! (Location unavailable)"

                            val smsManager = context.getSystemService(SmsManager::class.java)
                            smsManager.sendTextMessage(
                                emergencyPhoneNumber,
                                null,
                                message,
                                null,
                                null
                            )

                            statusMessage = "Alert sent (location unavailable)"
                            alertSent = true
                        }

                    } catch (e: Exception) {
                        statusMessage = "Error: ${e.message}"
                        alertSent = false
                    }
                }
            },
            modifier = Modifier
                .size(200.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            ),
            enabled = countdown == 0 // Disable button during countdown
        ) {
            Text(
                text = "SOS",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Text below button
        Text(
            text = if (hasPermissions) "Tap in Emergency" else "Grant Permissions First",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = if (hasPermissions) Color.Gray else Color.Red
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Show countdown or alert status
        when {
            countdown > 0 -> {
                // Show countdown: 3...2...1
                Text(
                    text = countdown.toString(),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
            statusMessage.isNotEmpty() -> {
                // Show status message
                Text(
                    text = statusMessage,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alertSent) Color(0xFF00C853) else Color.Red
                )
            }
        }
    }
}
