package com.example.zipcklad.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.zipcklad.data.local.ZIPItemEntity

@Composable
fun EditQuantityDialog(
    item: ZIPItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val quantityState = remember { mutableStateOf(item.quantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Изменить количество") },
        text = {
            Column {
                Text(text = "Название: ${item.name}")
                TextField(
                    value = quantityState.value,
                    onValueChange = { quantityState.value = it },
                    label = { Text("Количество") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newQuantity = quantityState.value.toIntOrNull() ?: item.quantity
                onConfirm(newQuantity)
                onDismiss()
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}