package com.example.zapmessage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.core.content.edit

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
    var searchQuery by remember { mutableStateOf("") }
    
    // state of list of the phones (loading local cache)
    var phoneList by remember {
        val savedString = sharedPreferences.getString("phone_list", "") ?: ""
        val initialList = if (savedString.isEmpty()) {
            emptyList()
        } else {
            savedString.split(",").map { PhoneData(it) }
        }
        mutableStateOf(initialList)
    }

    // Filter of lists of phones
    val filteredList = if (searchQuery.isEmpty()) {
        phoneList
    } else {
        phoneList.filter { it.phoneNumber?.contains(searchQuery, ignoreCase = true) == true }
    }

    // Function for update the local cache and phone numbers
    val saveToCache = { number: String ->
        val currentString = sharedPreferences.getString("phone_list", "") ?: ""
        val currentList = if (currentString.isEmpty()) mutableListOf() else currentString.split(",").toMutableList()
        
        currentList.remove(number)
        currentList.add(0, number)
        
        val limitedList = currentList.take(50)
        sharedPreferences.edit { putString("phone_list", limitedList.joinToString(",")) }
        phoneList = limitedList.map { PhoneData(it) }
    }

    // Function for delete a phone number
    val deleteFromCache = { number: String ->
        val currentString = sharedPreferences.getString("phone_list", "") ?: ""
        val currentList = if (currentString.isEmpty()) mutableListOf() else currentString.split(",").toMutableList()
        
        currentList.remove(number)
        
        sharedPreferences.edit { putString("phone_list", currentList.joinToString(",")) }
        phoneList = currentList.map { PhoneData(it) }
    }

    // Function for open WhatsApp from api
    val openWhatsApp = { number: String ->
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://wa.me/55$number".toUri()
        }
        context.startActivity(intent)
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
                                    saveToCache(phoneNumber)
                                    openWhatsApp(phoneNumber)
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
                            val editSearch = viewRecent.findViewById<EditText>(R.id.editSearch)
                            
                            recyclerView.layoutManager = LinearLayoutManager(ctx)
                            recyclerView.adapter = RecentsAdapter(
                                filteredList,
                                onMessageClick = { openWhatsApp(it) },
                                onDeleteClick = { deleteFromCache(it) }
                            )

                            editSearch.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                    searchQuery = s.toString()
                                }
                                override fun afterTextChanged(s: Editable?) {}
                            })
                            
                            viewRecent
                        },
                        update = { viewRecent ->
                            val recyclerView = viewRecent.findViewById<RecyclerView>(R.id.recyclerViewRecents)
                            recyclerView.adapter = RecentsAdapter(
                                filteredList,
                                onMessageClick = { openWhatsApp(it) },
                                onDeleteClick = { deleteFromCache(it) }
                            )
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
