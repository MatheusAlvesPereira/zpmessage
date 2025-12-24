package com.example.zapmessage

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zapmessage.ui.theme.ZapmessageTheme
import com.google.firebase.database.*

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
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    val databaseReference = remember { FirebaseDatabase.getInstance().getReference("PhoneNumbers") }

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
                        factory = { context ->
                            val viewMainLayout = View.inflate(context, R.layout.mainlayout, null)
                            val editText = viewMainLayout.findViewById<EditText>(R.id.inputText)
                            val button = viewMainLayout.findViewById<Button>(R.id.btn_search)
                            button.setOnClickListener {
                                val phoneNumber = editText.text.toString()
                                if (phoneNumber.isNotBlank()) {
                                    val phoneData = PhoneData(phoneNumber)
                                    databaseReference.child(phoneNumber).setValue(phoneData).addOnSuccessListener {
                                        editText.text.clear()
                                    }

                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = "https://wa.me/55$phoneNumber".toUri()
                                    }
                                    context.startActivity(intent)
                                }
                            }
                            viewMainLayout
                        }
                    )
                }

                AppDestinations.FAVORITES -> {
                    var phoneList by remember { mutableStateOf<List<PhoneData>>(emptyList()) }

                    DisposableEffect(databaseReference) {
                        val valueEventListener = object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val newList = snapshot.children.mapNotNull { it.getValue(PhoneData::class.java) }
                                phoneList = newList
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        }

                        databaseReference.addValueEventListener(valueEventListener)

                        onDispose {
                            databaseReference.removeEventListener(valueEventListener)
                        }
                    }

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            val viewRecent = View.inflate(context, R.layout.recentlayout, null)
                            val recyclerView = viewRecent.findViewById<RecyclerView>(R.id.recyclerViewRecents)
                            recyclerView.layoutManager = LinearLayoutManager(context)
                            viewRecent
                        },
                        update = { viewRecent ->
                            val recyclerView = viewRecent.findViewById<RecyclerView>(R.id.recyclerViewRecents)
                            recyclerView.adapter = RecentsAdapter(phoneList)
                        }
                    )
                }

                AppDestinations.PROFILE -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Profile Screen")
                    }
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
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}
