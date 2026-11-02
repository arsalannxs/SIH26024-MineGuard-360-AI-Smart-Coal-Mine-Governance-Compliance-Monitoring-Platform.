package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.repository.MineGuardRepository
import com.example.ui.MineGuardApp
import com.example.ui.theme.CloudBackground
import com.example.ui.theme.MineGuardTheme

class MainActivity : ComponentActivity() {

    private val repository = MineGuardRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MineGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CloudBackground
                ) {
                    MineGuardApp(repository = repository)
                }
            }
        }
    }
}
