package app.breeze.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.breeze.data.AppTheme
import app.breeze.data.ImageDetails

@Composable
fun ConfirmDeleteDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    itemCount: Int,
    itemType: String
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete $itemCount selected $itemType(s)?") },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InfoDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    title: String,
    imageDetailsList: List<ImageDetails>,
    modifier: Modifier = Modifier
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                Column {
                     val details = imageDetailsList.first()
                        Text(
                            text = "Name",
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("${details.name}")
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    currentTheme: AppTheme
) {
    if (showDialog) {
        val themes = remember { AppTheme.values().toList() }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Theme") },
            text = {
                Column(Modifier.selectableGroup()) {
                    themes.forEach { themeOption ->
                        val themeDisplayName = themeOption.name
                            .replace("_", " ")
                            .lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (themeOption == currentTheme),
                                    onClick = { onThemeSelected(themeOption) },
                                    role = Role.RadioButton
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (themeOption == currentTheme),
                                onClick = null
                            )
                            Text(
                                text = themeDisplayName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
