package com.raku

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var subtitleSize by remember { mutableStateOf(16.sp) }
    var selected by remember { mutableStateOf("Default") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
        	Text("This is your subtitle", fontSize = subtitleSize)
            Row(modifier = Modifier.fillMaxWidth()) {
                InputChip(
                    selected = selected == "Small",
                    onClick = {
                        selected = "Small"
                        subtitleSize = 12.sp
                    },
                    label = { Text("Small") }
                )
                Spacer(Modifier.width(8.dp))
                InputChip(
                    selected = selected == "Default",
                    onClick = {
                        selected = "Default"
                        subtitleSize = 16.sp
                    },
                    label = { Text("Default") }
                )
                Spacer(Modifier.width(8.dp))
                InputChip(
                    selected = selected == "Large",
                    onClick = {
                        selected = "Large"
                        subtitleSize = 20.sp
                    },
                    label = { Text("Large") }
                )
            }
        }
    }
}