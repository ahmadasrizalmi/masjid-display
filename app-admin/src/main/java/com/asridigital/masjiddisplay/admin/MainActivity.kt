package com.asridigital.masjiddisplay.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val AdminBackground = Color(0xFFF6F7F5)
private val AdminSurface = Color(0xFFFFFFFF)
private val AdminSurfaceMuted = Color(0xFFEEF1ED)
private val AdminTextPrimary = Color(0xFF17201B)
private val AdminTextSecondary = Color(0xFF68736D)
private val AdminAccent = Color(0xFF176B45)
private val AdminOnAccent = Color(0xFFFFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PairTvOnboardingScreen() }
    }
}

@Composable
internal fun PairTvOnboardingScreen(onScanQr: () -> Unit = {}) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = AdminBackground) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text("Masjid Display", color = AdminTextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(20.dp))
                OnboardingStepper(currentStep = 2)
                Spacer(Modifier.height(32.dp))
                Text("Hubungkan TV", style = MaterialTheme.typography.headlineMedium, color = AdminTextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Hubungkan aplikasi Admin dengan Masjid Display TV di jaringan lokal yang sama.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AdminTextSecondary,
                )
                Spacer(Modifier.height(28.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = AdminSurface,
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Scan QR dari TV", style = MaterialTheme.typography.titleLarge, color = AdminTextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Buka menu pairing pada TV, lalu scan QR yang tampil. HP dan TV harus berada pada LAN/Wi-Fi yang dapat saling terhubung. Anda tidak perlu memasukkan alamat IP.",
                            color = AdminTextSecondary,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onScanQr,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccent, contentColor = AdminOnAccent),
                ) {
                    Text("Scan QR dari TV", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepper(currentStep: Int) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        (1..4).forEach { step ->
            val active = step <= currentStep
            Surface(
                modifier = Modifier.size(if (step == currentStep) 12.dp else 9.dp),
                shape = CircleShape,
                color = if (active) AdminAccent else AdminSurfaceMuted,
            ) {}
            if (step < 4) {
                Spacer(
                    Modifier.weight(1f).height(2.dp).background(if (step < currentStep) AdminAccent else AdminSurfaceMuted),
                )
            }
        }
    }
}
