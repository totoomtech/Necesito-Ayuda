package com.totoom.necesitoayuda.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.totoom.necesitoayuda.R
import com.totoom.necesitoayuda.data.Contact
import com.totoom.necesitoayuda.data.DatabaseProvider
import com.totoom.necesitoayuda.databinding.ActivityMainBinding
import com.totoom.necesitoayuda.databinding.ItemContactCardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
        observeContacts()
        checkFirstRun()
    }

    private fun setupButtons() {
        // AYUDA RÁPIDA (required by mockup)
        binding.btnAyudaRapida.setOnClickListener {
            startEmergency(getString(R.string.ayuda_rapida), "", 3)
        }

        // 112 (5s + confirmation handled in CountdownActivity)
        binding.btn112.setOnClickListener {
            startEmergency("112", "112", 5)
        }

        // MÉDICO (phone must be configurable later; keep placeholder for now)
        binding.btnMedico.setOnClickListener {
            startEmergency(getString(R.string.medico), "123456789", 3)
        }

        setupSettingsLongPress()
    }

    private fun checkFirstRun() {
        // Intentionally minimal to avoid forced navigation.
        // Permissions readiness enforcement will be handled in Permissions flow.
    }

    private fun observeContacts() {
        val db = DatabaseProvider.getDatabase(this)
        CoroutineScope(Dispatchers.Main).launch {
            db.appDao().getTopContacts().collectLatest { contacts ->
                if (contacts.isEmpty()) {
                    seedInitialContacts()
                } else {
                    updateContactUI(contacts)
                }
            }
        }
    }

    private fun seedInitialContacts() {
        val db = DatabaseProvider.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            db.appDao().insertContact(Contact(1, "Hijo", "+34 611 567 890", null, 0))
            db.appDao().insertContact(Contact(2, "Hija", "+34 622 098 765", null, 1))
            db.appDao().insertContact(Contact(3, "Vecina", "+34 633 567 123", null, 2))
        }
    }

    private fun updateContactUI(contacts: List<Contact>) {
        val container = binding.contactContainer
        container.removeAllViews()

        contacts.forEach { contact ->
            val rowView = layoutInflater.inflate(R.layout.item_contact_card, container, false)
            val itemBinding = ItemContactCardBinding.bind(rowView)

            itemBinding.tvContactName.text = contact.name
            itemBinding.tvContactPhone.text = contact.phone
            // TODO: set photo if present

            rowView.setOnClickListener {
                startEmergency(contact.name, contact.phone, 3)
            }

            container.addView(rowView)
        }
    }

    private fun setupSettingsLongPress() {
        val handler = Handler(Looper.getMainLooper())
        var isLongPressing = false

        val longPressRunnable = Runnable {
            if (isLongPressing) {
                startActivity(Intent(this, SettingsActivity::class.java))
                isLongPressing = false
            }
        }

        binding.ivSettings.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isLongPressing = true
                    handler.postDelayed(longPressRunnable, 5000)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isLongPressing = false
                    handler.removeCallbacks(longPressRunnable)
                }
            }
            true
        }
    }

    private fun startEmergency(name: String, phone: String, seconds: Long) {
        val intent = Intent(this, CountdownActivity::class.java).apply {
            putExtra("TARGET_NAME", name)
            putExtra("TARGET_PHONE", phone)
            putExtra("COUNTDOWN_SECONDS", seconds)
        }
        startActivity(intent)
    }
}