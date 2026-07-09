package com.cc.skillapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GreetingScreen()
        }
    }
}

@Composable
private fun GreetingScreen() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Hello World!",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "文字2",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = maxHeight / 3),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "文字3",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(
                    x = (-100).dp,
                    y = maxHeight * 4 / 5
                ),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
