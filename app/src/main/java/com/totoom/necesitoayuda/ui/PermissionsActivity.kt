package com.totoom.necesitoayuda.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.totoom.necesitoayuda.R
import com.totoom.necesitoayuda.databinding.ActivityPermissionsBinding

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

        // Battery optimization needs a special intent
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
        setPermissionItemStatus(binding.itemUbicacion, Manifest.permission.ACCESS_FINE_LOCATION, getString(R.string.permiso_ubicacion))
        setPermissionItemStatus(binding.itemSms, Manifest.permission.SEND_SMS, getString(R.string.permiso_sms))
        setPermissionItemStatus(binding.itemTelefono, Manifest.permission.CALL_PHONE, getString(R.string.permiso_telefono))
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setPermissionItemStatus(binding.itemNotificaciones, Manifest.permission.POST_NOTIFICATIONS, getString(R.string.permiso_notificaciones))
        } else {
            binding.itemNotificaciones.root.visibility = android.view.View.GONE
        }

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val isIgnoringBattery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm.isIgnoringBatteryOptimizations(packageName)
        } else true
        
        binding.itemBateria.tvPermissionName.text = getString(R.string.permiso_bateria)
        binding.itemBateria.cbStatus.isChecked = isIgnoringBattery
        binding.itemBateria.tvStatusLabel.text = if (isIgnoringBattery) getString(R.string.activado) else getString(R.string.desactivado)
    }

    private fun setPermissionItemStatus(itemBinding: com.totoom.necesitoayuda.databinding.ItemPermissionCheckBinding, permission: String, name: String) {
        val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        itemBinding.tvPermissionName.text = name
        itemBinding.cbStatus.isChecked = granted
        itemBinding.tvStatusLabel.text = if (granted) getString(R.string.activado) else getString(R.string.desactivado)
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }
}
