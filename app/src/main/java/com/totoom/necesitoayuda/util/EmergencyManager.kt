package com.totoom.necesitoayuda.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import com.totoom.necesitoayuda.R
import kotlinx.coroutines.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class EmergencyManager(private val context: Context) {

    private val TAG = "NECESITO_AYUDA"
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun executeFullEmergencyFlow(targetPhone: String, allContactPhones: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Executing Full Emergency Flow")
            
            // 1. Get Location (with timeout and fallback)
            val location = getBestLocation()
            val lat = location?.latitude ?: 0.0
            val lon = location?.longitude ?: 0.0

            // 2. Send SMS to all 3 contacts
            sendSmsToAll(lat, lon, allContactPhones)

            // 3. Open WhatsApp (Prefilled)
            withContext(Dispatchers.Main) {
                openWhatsApp(lat, lon, targetPhone)
            }

            // 4. Start Call Cascade
            delay(2000) 
            startCallCascade(listOf(targetPhone) + (allContactPhones.filter { it != targetPhone }))
        }
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
                Log.d(TAG, "Location timeout, falling back to last known")
                suspendCancellableCoroutine { continuation ->
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location -> continuation.resume(location) {} }
                        .addOnFailureListener { continuation.resume(null) {} }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Location error", e)
            null
        }
    }

    private fun sendSmsToAll(lat: Double, lon: Double, phoneNumbers: List<String>) {
        Log.d(TAG, "Step 2: Sending SMS to all contacts")
        val message = context.getString(R.string.sms_msg, lat, lon)
        val smsManager = context.getSystemService(SmsManager::class.java)
        phoneNumbers.forEach { phone ->
            try {
                smsManager.sendTextMessage(phone, null, message, null, null)
                Log.d(TAG, "SMS sent to $phone")
            } catch (e: Exception) {
                Log.e(TAG, "Failed send SMS to $phone", e)
            }
        }
    }

    fun openWhatsApp(lat: Double, lon: Double, targetPhone: String) {
        Log.d(TAG, "Step 3: Opening WhatsApp")
        val message = context.getString(R.string.sms_msg, lat, lon)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=${Uri.encode(message)}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "WhatsApp error", e)
        }
    }

    fun startCallCascade(targetPhones: List<String>) {
        Log.d(TAG, "Step 4: Starting Call Cascade")
        CoroutineScope(Dispatchers.Main).launch {
            for (phone in targetPhones) {
                if (phone.isBlank()) continue
                Log.d(TAG, "Calling $phone...")
                makePhoneCall(phone)
                delay(15000) 
            }
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
            Log.e(TAG, "Call error", e)
        }
    }
}
