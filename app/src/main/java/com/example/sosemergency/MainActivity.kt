package com.example.sosemergency

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    @Composable
    fun SOSScreen(
        fusedLocationClient: FusedLocationProviderClient,
        hasPermissions: Boolean,
        onRequestPermissions: () -> Unit,
        onPlayAlarm: () -> Unit
    ) {

        val emergencyPhoneNumber = "+917019267392"

        var alertSent by remember { mutableStateOf(false) }
        var statusMessage by remember { mutableStateOf("") }
        var locationText by remember { mutableStateOf("Live location not detected yet") }
        var latestMapsLink by remember { mutableStateOf("") }
        var showProfile by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // Background Image
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Background Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Profile Button
            Button(
                onClick = {
                    showProfile = true
                },

                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 40.dp, start = 20.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                )
            ) {
                Text("👤")
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Open Map Button
                Button(
                    onClick = {

                        if (latestMapsLink.isNotEmpty()) {

                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(latestMapsLink)
                            )

                            context.startActivity(intent)

                        } else {

                            Toast.makeText(
                                context,
                                "Location not detected yet",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },

                    modifier = Modifier
                        .padding(top = 40.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1565C0),
                        contentColor = Color.White
                    )

                ) {
                    Text("Open Live Location Map")
                }

                Spacer(modifier = Modifier.weight(1f))

                // SOS Button
                Button(
                    onClick = {

                        if (!hasPermissions) {
                            onRequestPermissions()
                            return@Button
                        }

                        onPlayAlarm()

                        statusMessage = "Getting location..."
                        alertSent = false

                        coroutineScope.launch {

                            try {

                                val location =
                                    fusedLocationClient.getCurrentLocation(
                                        Priority.PRIORITY_HIGH_ACCURACY,
                                        CancellationTokenSource().token
                                    ).await()

                                val message: String

                                if (location != null) {

                                    val latitude = location.latitude
                                    val longitude = location.longitude

                                    latestMapsLink =
                                        "https://maps.google.com/?q=$latitude,$longitude"

                                    locationText =
                                        "Live Location:\nLatitude: $latitude\nLongitude: $longitude"

                                    message =
                                        "🚨 EMERGENCY SOS ALERT 🚨\n\n" +
                                                "I need help! My location:\n$latestMapsLink"

                                } else {

                                    message =
                                        "🚨 EMERGENCY SOS ALERT 🚨\n\n" +
                                                "I need help! Location unavailable."

                                    locationText = "Location unavailable"
                                }

                                val smsManager =
                                    context.getSystemService(SmsManager::class.java)

                                smsManager.sendTextMessage(
                                    emergencyPhoneNumber,
                                    null,
                                    message,
                                    null,
                                    null
                                )

                                statusMessage =
                                    "Alert Sent\nHelp is on the way\nPolice notified\nLocation sent to family"

                                alertSent = true

                            } catch (e: Exception) {

                                statusMessage = "Error: ${e.message}"
                                alertSent = false
                            }
                        }
                    },

                    modifier = Modifier.size(200.dp),

                    shape = CircleShape,

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )

                ) {

                    Text(
                        text = "SOS",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (hasPermissions)
                        "Tap in Emergency"
                    else
                        "Grant Permissions First",

                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (statusMessage.isNotEmpty()) {

                    Text(
                        text = statusMessage,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,

                        color =
                            if (alertSent)
                                Color.Black
                            else
                                Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Live Location Card
                Card(
                    modifier = Modifier.fillMaxWidth(),

                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.65f)
                    )
                ) {

                    Text(
                        text = locationText,

                        modifier = Modifier.padding(16.dp),

                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            // Profile Popup
            if (showProfile) {

                ProfileDialog(
                    onDismiss = {
                        showProfile = false
                    }
                )
            }
        }
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var hasPermissions by mutableStateOf(false)
    private var mediaPlayer: MediaPlayer? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                    permissions[Manifest.permission.SEND_SMS] == true

        if (!hasPermissions) {
            Toast.makeText(this, "Permissions required", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkAndRequestPermissions()

        setContent {
            SOSEmergencyTheme {
                SOSScreen(
                    fusedLocationClient = fusedLocationClient,
                    hasPermissions = hasPermissions,
                    onRequestPermissions = { checkAndRequestPermissions() },
                    onPlayAlarm = { playAlarmSound() }
                )
            }
        }
    }

    private fun playAlarmSound() {

        try {

            Toast.makeText(
                this,
                "Trying to play alarm",
                Toast.LENGTH_SHORT
            ).show()

            mediaPlayer?.release()

            mediaPlayer = MediaPlayer.create(
                this,
                R.raw.alarm
            )

            mediaPlayer?.setVolume(1.0f, 1.0f)

            mediaPlayer?.start()

            Toast.makeText(
                this,
                "Alarm started",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Sound error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun checkAndRequestPermissions() {
        val fineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val sms = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
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
    onRequestPermissions: () -> Unit,
    onPlayAlarm: () -> Unit
) {
    val emergencyPhoneNumber = "+917019267392"

    var alertSent by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("Live location not detected yet") }
    var latestMapsLink by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = {
                    if (latestMapsLink.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(latestMapsLink))
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(
                            context,
                            "Location not detected yet",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },

                modifier = Modifier
                    .padding(top = 40.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0),
                    contentColor = Color.White
                )
            ) {
                Text("Open Live Location Map")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (!hasPermissions) {
                        onRequestPermissions()
                        return@Button
                    }

                    onPlayAlarm()
                    statusMessage = "Getting location..."
                    alertSent = false

                    coroutineScope.launch {
                        try {
                            val location = fusedLocationClient.getCurrentLocation(
                                Priority.PRIORITY_HIGH_ACCURACY,
                                CancellationTokenSource().token
                            ).await()

                            val message: String

                            if (location != null) {
                                val latitude = location.latitude
                                val longitude = location.longitude

                                latestMapsLink =
                                    "https://maps.google.com/?q=$latitude,$longitude"

                                locationText =
                                    "Live Location:\nLatitude: $latitude\nLongitude: $longitude"

                                message =
                                    "🚨 EMERGENCY SOS ALERT 🚨\n\n" +
                                            "I need help! My location:\n$latestMapsLink"
                            } else {
                                message =
                                    "🚨 EMERGENCY SOS ALERT 🚨\n\n" +
                                            "I need help! Location unavailable."

                                locationText = "Location unavailable"
                            }

                            val smsManager =
                                context.getSystemService(SmsManager::class.java)

                            smsManager.sendTextMessage(
                                emergencyPhoneNumber,
                                null,
                                message,
                                null,
                                null
                            )

                            statusMessage =
                                "Alert Sent\nHelp is on the way\nPolice notified\nLocation sent to family"

                            alertSent = true

                        } catch (e: Exception) {
                            statusMessage = "Error: ${e.message}"
                            alertSent = false
                        }
                    }
                },
                modifier = Modifier.size(200.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "SOS",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (hasPermissions) "Tap in Emergency" else "Grant Permissions First",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alertSent) Color(0xFF000000) else Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.65f)
                )
            ) {
                Text(
                    text = locationText,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}