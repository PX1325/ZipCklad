package com.example.zipcklad.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.zipcklad.data.local.ZIPItemEntity
import com.example.zipcklad.ui.EditQuantityDialog

@Composable
fun ZIPItemCard(
    item: ZIPItemEntity,
    onEditQuantity: (ZIPItemEntity, Int) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showDialog = remember { mutableStateOf(false) }


    if (showDialog.value) {
        EditQuantityDialog(
            item = item,
            onDismiss = { showDialog.value = false },
            onConfirm = { newQuantity ->
                onEditQuantity(item, newQuantity)
                showDialog.value = false
            }
        )
    }

    Card(
        modifier = modifier
            .padding(8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showDialog.value = true },
                    onLongPress = { onLongClick() }
                )
            }
                //onClick = { showDialog.value = true } // Открываем диалог при клике
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Название: ${item.name}")
            Text(text = "Заказной номер: ${item.partNumber}")
            Text(text = "Количество: ${item.quantity}")
            Text(text = "Местоположение: ${item.location}")
        }
    }
}