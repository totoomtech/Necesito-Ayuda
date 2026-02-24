package com.totoom.necesitoayuda.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.totoom.necesitoayuda.R
import com.totoom.necesitoayuda.data.DatabaseProvider
import com.totoom.necesitoayuda.databinding.ActivityCountdownBinding
import com.totoom.necesitoayuda.util.EmergencyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class CountdownActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityCountdownBinding
    private lateinit var tts: TextToSpeech
    private var countdownTimer: CountDownTimer? = null
    private val vibrator: Vibrator by lazy { getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
    
    private var targetName: String = ""
    private var targetPhone: String = ""
    private var countdownSeconds: Long = 3
    private var isSmsOnly: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Block interaction and make full screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        
        binding = ActivityCountdownBinding.inflate(layoutInflater)
        setContentView(binding.root)

        targetName = intent.getStringExtra("TARGET_NAME") ?: "AYUDA"
        targetPhone = intent.getStringExtra("TARGET_PHONE") ?: ""
        countdownSeconds = intent.getLongExtra("COUNTDOWN_SECONDS", 3)
        isSmsOnly = intent.getBooleanExtra("SMS_ONLY", false)

        binding.tvTargetName.text = targetName.uppercase()
        
        tts = TextToSpeech(this, this)

        binding.btnCancelar.setOnClickListener {
            cancelEverything()
        }

        startCountdown()
    }

    private fun startCountdown() {
        countdownTimer = object : CountDownTimer(countdownSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000) + 1
                binding.tvCountdownNumber.text = secondsRemaining.toString()
                playTickFeedback()
            }

            override fun onFinish() {
                binding.tvCountdownNumber.text = "0"
                if (targetName == "112") {
                    show112Confirmation()
                } else {
                    triggerEmergencyFlow()
                }
            }
        }.start()
    }

    private fun show112Confirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirmar_112)
            .setMessage(R.string.confirmar_112)
            .setPositiveButton(R.string.activar_permisos) { _, _ -> triggerEmergencyFlow() }
            .setNegativeButton(R.string.cancelar) { _, _ -> cancelEverything() }
            .setCancelable(false)
            .show()
    }

    private fun playTickFeedback() {
        // Vibrate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
        // Beep
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    private fun triggerEmergencyFlow() {
        Log.d("NECESITO_AYUDA", "Step 0: Countdown finished for $targetName")
        val emergencyManager = EmergencyManager(this)
        val db = DatabaseProvider.getDatabase(this)
        
        CoroutineScope(Dispatchers.Main).launch {
            val contacts = db.appDao().getTopContacts().first()
            val allPhones = contacts.map { it.phone }
            
            emergencyManager.executeFullEmergencyFlow(targetPhone, allPhones)
            finish()
        }
    }

    private fun cancelEverything() {
        countdownTimer?.cancel()
        if (::tts.isInitialized) tts.stop()
        finish()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.getDefault())
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                val msg = getString(R.string.enviando_ayuda_a, targetName)
                tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "emergency_msg")
            }
        }
    }

    override fun onBackPressed() {
        // Disable back button as per requirement
    }

    override fun onDestroy() {
        countdownTimer?.cancel()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        toneGenerator.release()
        super.onDestroy()
    }
}
