package com.example.zapmessage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zapmessage.ui.theme.ZapmessageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZapmessageTheme {
                MessageApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun MessageApp() {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("zap_message_prefs", Context.MODE_PRIVATE) }
    
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    
    // Estado da lista de telefones (carregado do cache local)
    var phoneList by remember {
        val savedString = sharedPreferences.getString("phone_list", "") ?: ""
        val initialList = if (savedString.isEmpty()) {
            emptyList<PhoneData>()
        } else {
            savedString.split(",").map { PhoneData(it) }
        }
        mutableStateOf(initialList)
    }

    // Função para atualizar a lista local e o cache
    val saveToCache = { number: String ->
        val currentString = sharedPreferences.getString("phone_list", "") ?: ""
        val currentList = if (currentString.isEmpty()) mutableListOf() else currentString.split(",").toMutableList()
        
        // Remove se já existir para reinserir no topo (garante unicidade e ordem de recentes)
        currentList.remove(number)
        currentList.add(0, number)
        
        // Limita aos 50 mais recentes
        val limitedList = currentList.take(50)
        
        sharedPreferences.edit().putString("phone_list", limitedList.joinToString(",")).apply()
        
        // Atualiza o estado da UI
        phoneList = limitedList.map { PhoneData(it) }
    }

    NavigationSuiteScaffold(
        modifier = Modifier.safeDrawingPadding(),
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentDestination) {
                AppDestinations.HOME -> {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val viewMainLayout = View.inflate(ctx, R.layout.mainlayout, null)
                            val editText = viewMainLayout.findViewById<EditText>(R.id.inputText)
                            val button = viewMainLayout.findViewById<Button>(R.id.btn_search)
                            button.setOnClickListener {
                                val phoneNumber = editText.text.toString()
                                if (phoneNumber.isNotBlank()) {
                                    // Salva localmente
                                    saveToCache(phoneNumber)

                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = "https://wa.me/55$phoneNumber".toUri()
                                    }
                                    ctx.startActivity(intent)
                                    editText.text.clear()
                                }
                            }
                            viewMainLayout
                        }
                    )
                }

                AppDestinations.RECENTS -> {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val viewRecent = View.inflate(ctx, R.layout.recentlayout, null)
                            val recyclerView = viewRecent.findViewById<RecyclerView>(R.id.recyclerViewRecents)
                            recyclerView.layoutManager = LinearLayoutManager(ctx)
                            recyclerView.adapter = RecentsAdapter(phoneList)
                            viewRecent
                        },
                        update = { viewRecent ->
                            // Atualiza o RecyclerView sempre que a phoneList mudar
                            val recyclerView = viewRecent.findViewById<RecyclerView>(R.id.recyclerViewRecents)
                            recyclerView.adapter = RecentsAdapter(phoneList)
                        }
                    )
                }

                AppDestinations.PROFILE -> {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val viewProfile = View.inflate(ctx, R.layout.profilelayout, null)
                            viewProfile
                        }
                    )
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    RECENTS("Recents", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}
