package app.breeze.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    infoItems: List<String>,
    modifier: Modifier = Modifier
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                LazyColumn() {
                    items(infoItems) { item ->
                        Text(item)
                    }
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
