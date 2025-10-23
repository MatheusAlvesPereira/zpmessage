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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.viewinterop.AndroidView
import com.example.zapmessage.ui.theme.ZapmessageTheme
import androidx.core.net.toUri
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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

    lateinit var databaseReference: DatabaseReference

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
                                    databaseReference = FirebaseDatabase.getInstance().getReference("PhoneNumbers")
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
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            val viewRecent = View.inflate(context, R.layout.recentlayout, null)
                            viewRecent

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
