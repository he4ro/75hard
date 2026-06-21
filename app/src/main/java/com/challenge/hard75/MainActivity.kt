package com.challenge.hard75

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.challenge.hard75.ui.AppNav
import com.challenge.hard75.viewmodel.ChallengeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ChallengeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AppNav(viewModel)
        }
    }
}
