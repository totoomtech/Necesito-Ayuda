package com.totoom.necesitoayuda.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.totoom.necesitoayuda.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        binding.btnBack.setOnClickListener { finish() }
    }
}
