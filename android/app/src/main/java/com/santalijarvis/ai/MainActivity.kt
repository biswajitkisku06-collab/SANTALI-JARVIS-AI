package com.santalijarvis.ai

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JarvisApp(this) }
    }
}

data class ChatMessage(val text: String, val fromUser: Boolean)

@Composable
fun JarvisApp(context: Context) {
    val prefs = context.getSharedPreferences("jarvis", Context.MODE_PRIVATE)
    var backendUrl by remember { mutableStateOf(prefs.getString("backend_url", "") ?: "") }
    var showSettings by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val messages = remember {
        mutableStateListOf(ChatMessage("নমস্কার! আমি SANTALI JARVIS AI। Backend URL সেট করে AI chat শুরু করো।", false))
    }
    val scope = rememberCoroutineScope()
    val bg = Color(0xFF07111F)
    val panel = Color(0xFF122238)
    val cyan = Color(0xFF55D6E8)

    MaterialTheme(colorScheme = darkColorScheme(
        primary = cyan,
        background = bg,
        surface = panel,
        onBackground = Color.White,
        onSurface = Color.White
    )) {
        Column(Modifier.fillMaxSize().background(bg).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("SANTALI JARVIS AI", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("PERSONAL AI ASSISTANT", color = cyan, fontSize = 10.sp, letterSpacing = 2.sp)
                }
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.fromUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            color = if (msg.fromUser) Color(0xFF15566B) else panel,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.widthIn(max = 310.dp)
                        ) {
                            Text(msg.text, color = Color.White, modifier = Modifier.padding(13.dp))
                        }
                    }
                }
                if (busy) item { Text("JARVIS is thinking…", color = cyan) }
            }
            Row(
                modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(24.dp)).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f).padding(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 15.sp),
                    decorationBox = { inner ->
                        if (input.isEmpty()) Text("Ask JARVIS anything…", color = Color.LightGray)
                        inner()
                    }
                )
                Button(
                    enabled = input.isNotBlank() && !busy,
                    onClick = {
                        val question = input.trim()
                        input = ""
                        messages.add(ChatMessage(question, true))
                        if (backendUrl.isBlank()) {
                            messages.add(ChatMessage("প্রথমে Settings থেকে Backend URL যোগ করো।", false))
                        } else {
                            busy = true
                            scope.launch {
                                val answer = withContext(Dispatchers.IO) {
                                    requestJarvis(backendUrl, question)
                                }
                                messages.add(ChatMessage(answer, false))
                                busy = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cyan, contentColor = Color.Black)
                ) { Text("Send") }
            }
        }

        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = { Text("Backend Settings") },
                text = {
                    Column {
                        Text("তোমার deployed backend-এর HTTPS URL দাও। Gemini API key এখানে দেবে না।")
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = backendUrl,
                            onValueChange = { backendUrl = it },
                            label = { Text("Backend URL") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        prefs.edit().putString("backend_url", backendUrl.trim().trimEnd('/')).apply()
                        showSettings = false
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showSettings = false }) { Text("Cancel") }
                }
            )
        }
    }
}

private fun requestJarvis(baseUrl: String, message: String): String {
    var connection: HttpURLConnection? = null
    return try {
        val url = URL(baseUrl.trimEnd('/') + "/api/chat")
        connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20000
            readTimeout = 60000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        val body = JSONObject().put("message", message).put("history", JSONArray()).toString()
        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val response = stream.bufferedReader().use { it.readText() }
        val json = JSONObject(response)
        if (code in 200..299) json.optString("reply", "খালি উত্তর পাওয়া গেছে।")
        else "Backend error ($code): " + json.optString("error", "Unknown error")
    } catch (e: Exception) {
        "সংযোগ করা যায়নি: ${e.message ?: "Unknown error"}"
    } finally {
        connection?.disconnect()
    }
}
