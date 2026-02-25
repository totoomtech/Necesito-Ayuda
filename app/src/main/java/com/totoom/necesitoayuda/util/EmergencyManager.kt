package com.totoom.necesitoayuda.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.totoom.necesitoayuda.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class EmergencyManager(private val context: Context) {

    private val tag = "NECESITO_AYUDA"
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun executeFullEmergencyFlow(targetPhone: String, allContactPhones: List<String>) {
        Log.d(tag, "Executing Full Emergency Flow")

        val location = getBestLocation()
        val lat = location?.latitude ?: 0.0
        val lon = location?.longitude ?: 0.0

        sendSmsToAll(lat, lon, allContactPhones)

        withContext(Dispatchers.Main) {
            openWhatsApp(lat, lon, targetPhone)
        }

        delay(2000)
        startCallCascade(listOf(targetPhone) + allContactPhones.filter { it != targetPhone })
    }

    private suspend fun getBestLocation(): android.location.Location? {
        return try {
            withTimeoutOrNull(5000) {
                suspendCancellableCoroutine { continuation ->
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location -> continuation.resume(location) {} }
                        .addOnFailureListener { continuation.resume(null) {} }
                }
            } ?: run {
                Log.d(tag, "Location timeout, falling back to last known")
                suspendCancellableCoroutine { continuation ->
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location -> continuation.resume(location) {} }
                        .addOnFailureListener { continuation.resume(null) {} }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Location error", e)
            null
        }
    }

    private fun sendSmsToAll(lat: Double, lon: Double, phoneNumbers: List<String>) {
        Log.d(tag, "Step 2: Sending SMS to all contacts")
        val message = context.getString(R.string.sms_msg, lat, lon)
        val smsManager = context.getSystemService(SmsManager::class.java)
        phoneNumbers.forEach { phone ->
            try {
                smsManager.sendTextMessage(phone, null, message, null, null)
                Log.d(tag, "SMS sent to $phone")
            } catch (e: Exception) {
                Log.e(tag, "Failed send SMS to $phone", e)
            }
        }
    }

    private fun openWhatsApp(lat: Double, lon: Double, targetPhone: String) {
        Log.d(tag, "Step 3: Opening WhatsApp")
        val message = context.getString(R.string.sms_msg, lat, lon)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=${Uri.encode(message)}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(tag, "WhatsApp error", e)
        }
    }

    private suspend fun startCallCascade(targetPhones: List<String>) {
        Log.d(tag, "Step 4: Starting Call Cascade")
        targetPhones.forEach { phone ->
            if (phone.isBlank()) return@forEach
            Log.d(tag, "Calling $phone...")
            withContext(Dispatchers.Main) {
                makePhoneCall(phone)
            }
            delay(15000)
        }
    }

    private fun makePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(tag, "Call error", e)
        }
    }
}
