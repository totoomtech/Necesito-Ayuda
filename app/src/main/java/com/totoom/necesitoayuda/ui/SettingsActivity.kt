package com.totoom.necesitoayuda.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.totoom.necesitoayuda.data.DatabaseProvider
import com.totoom.necesitoayuda.data.Contact
import com.totoom.necesitoayuda.databinding.ActivitySettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import android.view.LayoutInflater
import com.totoom.necesitoayuda.databinding.ItemSettingsContactBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        
        binding.btnPermissionsShortcut.setOnClickListener {
            startActivity(Intent(this, PermissionsActivity::class.java))
        }

        loadData()
    }

    private fun loadData() {
        val db = DatabaseProvider.getDatabase(this)
        CoroutineScope(Dispatchers.Main).launch {
            val contacts = db.appDao().getTopContacts().first()
            populateContacts(contacts)
        }
    }

    private fun populateContacts(contacts: List<Contact>) {
        val container = binding.contactsEditContainer
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)

        contacts.forEach { contact ->
            val itemBinding = ItemSettingsContactBinding.inflate(inflater, container, true)
            itemBinding.etName.setText(contact.name)
            itemBinding.etPhone.setText(contact.phone)
            
            itemBinding.btnSaveContact.setOnClickListener {
                val newName = itemBinding.etName.text.toString()
                val newPhone = itemBinding.etPhone.text.toString()
                saveContact(contact.copy(name = newName, phone = newPhone))
            }
        }
    }

    private fun saveContact(contact: Contact) {
        val db = DatabaseProvider.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            db.appDao().insertContact(contact)
        }
    }
}
