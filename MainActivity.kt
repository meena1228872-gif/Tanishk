package com.gaurav.stonicai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status -> if (status == TextToSpeech.SUCCESS) tts?.language = Locale("hi", "IN") }
        setContent { StonicApp() }
    }

    private fun speak(text: String) { tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "stonic") }

    private fun listen(onText: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onResults(results: Bundle?) { results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(onText) }
                override fun onError(error: Int) {}
                override fun onReadyForSpeech(p: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(r: Float) {}
                override fun onBufferReceived(b: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(p: Bundle?) {}
                override fun onEvent(t: Int, p: Bundle?) {}
            })
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Boliye…")
            }
            startListening(i)
        }
    }

    private fun executeLocalCommand(text: String): String? {
        val q = text.lowercase(Locale.ROOT)
        if (q.contains("settings") || q.contains("setting") || q.contains("सेटिंग")) {
            startActivity(Intent(Settings.ACTION_SETTINGS)); return "Settings खोल रहा हूँ।"
        }
        val mappings = mapOf("youtube" to "com.google.android.youtube", "chrome" to "com.android.chrome", "whatsapp" to "com.whatsapp", "instagram" to "com.instagram.android", "camera" to "com.android.camera")
        for ((name, pkg) in mappings) if (q.contains(name)) {
            val intent = packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) { startActivity(intent); return "$name खोल रहा हूँ।" }
        }
        if (q.contains("wifi") || q.contains("वाईफाई")) { startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)); return "Wi‑Fi settings खोल रहा हूँ।" }
        if (q.contains("bluetooth") || q.contains("ब्लूटूथ")) { startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)); return "Bluetooth settings खोल रहा हूँ।" }
        return null
    }

    @Composable fun StonicApp() {
        var input by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("Ready") }
        val messages = remember { mutableStateListOf("Stonic: Namaste! Main ready hoon.") }
        val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

        MaterialTheme(colorScheme = darkColorScheme()) {
            Surface(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize().padding(18.dp)) {
                    Text("STONIC", style = MaterialTheme.typography.headlineLarge)
                    Text("Phone AI Assistant • $status", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                        items(messages) { Text(it, Modifier.padding(vertical = 8.dp)) }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(input, { input = it }, Modifier.weight(1f), placeholder = { Text("Ask anything…") })
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                permission.launch(Manifest.permission.RECORD_AUDIO)
                            }
                            status = "Listening…"
                            listen { heard -> input = heard; status = "Processing"; messages.add("You: $heard"); val local = executeLocalCommand(heard); val reply = local ?: "Gemini backend connect hone ke baad main iska AI answer dunga."; messages.add("Stonic: $reply"); speak(reply); status = "Ready" }
                        }) { Text("🎙") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val q = input.trim(); if (q.isEmpty()) return@Button
                        messages.add("You: $q"); val local = executeLocalCommand(q); val reply = local ?: "Gemini backend connect hone ke baad main iska AI answer dunga."; messages.add("Stonic: $reply"); speak(reply); input = ""
                    }, Modifier.fillMaxWidth()) { Text("Ask Stonic") }
                }
            }
        }
    }

    override fun onDestroy() { speechRecognizer?.destroy(); tts?.shutdown(); super.onDestroy() }
}
