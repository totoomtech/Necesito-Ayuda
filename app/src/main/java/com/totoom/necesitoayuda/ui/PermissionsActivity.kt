package com.totoom.necesitoayuda.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.totoom.necesitoayuda.R
import com.totoom.necesitoayuda.data.AppSettings
import com.totoom.necesitoayuda.data.DatabaseProvider
import com.totoom.necesitoayuda.databinding.ActivityPermissionsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PermissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionsBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { updateStatus() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnActivarPermisos.setOnClickListener {
            requestPermissions()
        }

        binding.btnVolver.setOnClickListener { finish() }
        binding.ivBack.setOnClickListener { finish() }
        binding.btnGuardar.setOnClickListener { finish() }

        updateStatus()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun updateStatus() {
        val locationReady = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        val smsReady = isPermissionGranted(Manifest.permission.SEND_SMS)
        val phoneReady = isPermissionGranted(Manifest.permission.CALL_PHONE)
        val notificationsReady = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)

        setPermissionItemStatus(binding.itemUbicacion, getString(R.string.permiso_ubicacion), locationReady)
        setPermissionItemStatus(binding.itemSms, getString(R.string.permiso_sms), smsReady)
        setPermissionItemStatus(binding.itemTelefono, getString(R.string.permiso_telefono), phoneReady)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setPermissionItemStatus(binding.itemNotificaciones, getString(R.string.permiso_notificaciones), notificationsReady)
        } else {
            binding.itemNotificaciones.root.visibility = View.GONE
        }

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val batteryReady = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm.isIgnoringBatteryOptimizations(packageName)
        } else {
            true
        }

        setPermissionItemStatus(binding.itemBateria, getString(R.string.permiso_bateria), batteryReady)

        val isReady = locationReady && smsReady && phoneReady && notificationsReady && batteryReady
        renderReadyState(isReady)
        persistPermissionsReady(isReady)
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun setPermissionItemStatus(
        itemBinding: com.totoom.necesitoayuda.databinding.ItemPermissionCheckBinding,
        name: String,
        granted: Boolean
    ) {
        itemBinding.tvPermissionName.text = name
        itemBinding.cbStatus.isChecked = granted
        itemBinding.tvStatusLabel.text = if (granted) getString(R.string.activado) else getString(R.string.desactivado)
    }

    private fun renderReadyState(isReady: Boolean) {
        binding.tvReadyState.text = if (isReady) {
            getString(R.string.permisos_estado_listo)
        } else {
            getString(R.string.permisos_estado_no_listo)
        }
        val colorRes = if (isReady) R.color.green_status else R.color.red_emergency
        binding.tvReadyState.setTextColor(ContextCompat.getColor(this, colorRes))
    }

    private fun persistPermissionsReady(isReady: Boolean) {
        val db = DatabaseProvider.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val currentSettings = db.appDao().getSettings()
            if (currentSettings == null) {
                db.appDao().upsertSettings(AppSettings(hasCompletedPermissions = isReady))
            } else if (currentSettings.hasCompletedPermissions != isReady) {
                db.appDao().upsertSettings(currentSettings.copy(hasCompletedPermissions = isReady))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }
}
