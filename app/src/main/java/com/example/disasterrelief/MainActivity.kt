package com.example.disasterrelief

import android.os.Bundle
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.disasterrelief.ui.navigation.AppNavGraph
import com.example.disasterrelief.ui.theme.DisasterReliefTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DisasterReliefTheme {
                    AppNavGraph()

            }
        }
    }
}
