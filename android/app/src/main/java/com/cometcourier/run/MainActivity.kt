package com.cometcourier.run

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cometcourier.run.core.di.ServiceLocator
import com.cometcourier.run.core.theme.AppTheme
import com.cometcourier.run.presentation.app.AppRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ServiceLocator.initialize(applicationContext)
        setContent { AppTheme { AppRoot() } }
    }
}
