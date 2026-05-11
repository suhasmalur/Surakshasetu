package com.example.sosemergency

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

@Composable
fun ProfileDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },

        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                }
            ) {
                Text("Close")
            }
        },

        title = {
            Text(
                text = "User Profile",
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pooja",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("🩸 Blood Group: O+")
                Text("🏢 Workspace: Infosys")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "📍 2nd cross, Gurunath Layout,\n" +
                            "Hurulichikkanahalli,\n" +
                            "Hessaraghatta Main Road,\n" +
                            "Bengaluru 560090.",
                    textAlign = TextAlign.Center,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.clickable {
                        val gmmIntentUri =
                            "geo:0,0?q=2nd cross Gurunath Layout Hurulichikkanahalli Bengaluru 560090".toUri()

                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

                        context.startActivity(mapIntent)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Emergency Contacts",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("📞 Mother: +91 9876543210")
                Text("📞 Father: +91 9876543211")
                Text("📞 Friend: +91 9876543212")
            }
        }
    )
}