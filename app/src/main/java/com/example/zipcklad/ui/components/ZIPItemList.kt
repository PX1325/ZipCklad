package com.example.zipcklad.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.zipcklad.data.local.ZIPItemEntity

@Composable
fun ZIPItemList(
    items: List<ZIPItemEntity>,
    onEditQuantity: (ZIPItemEntity, Int) -> Unit,
    onLongClick: (ZIPItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
        LazyColumn(modifier = modifier.padding(8.dp)) {
            items(items) { item ->
                ZIPItemCard(
                    item = item,
                    onEditQuantity = onEditQuantity,
                    onLongClick = { onLongClick(item) }
            )
        }
    }
}
