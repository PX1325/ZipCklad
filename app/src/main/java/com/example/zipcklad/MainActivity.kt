package com.example.zipcklad

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.TextField
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.zipcklad.ui.theme.ZIPItemList
import com.example.zipcklad.ui.theme.ZipCkladTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Sync
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZipCkladTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: ZIPViewModel = hiltViewModel()) {
    // Состояние элементов и логика
    val items by viewModel.items.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val syncStatus by viewModel.syncStatus.collectAsState()
    val context = LocalContext.current

    // Обработка статусов
    LaunchedEffect(syncStatus) {
        when (val status = syncStatus) {
            is SyncStatus.SUCCESS -> {
                snackbarHostState.showSnackbar("Синхронизация завершена")
                viewModel.resetSyncStatus()
            }
            is SyncStatus.ERROR -> {
                snackbarHostState.showSnackbar("Ошибка: ${status.message}")
                viewModel.resetSyncStatus()
            }
            else -> {}
        }
    }

    // Ланчер для выбора файла
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                viewModel.importExcel(stream)
                Toast.makeText(context, "Импорт завершен!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Структура экрана
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Кнопка синхронизации
                FloatingActionButton(
                    onClick = { viewModel.syncWithServer() },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.Sync, "Синхронизация")
                }

                ExtendedFloatingActionButton(
                    onClick = {
                        filePicker.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Импорт Excel"
                        )
                    },
                    text = { Text("Импорт Excel") }
                )
            }
        },
                topBar = {
                SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )


    { innerPadding ->
       Box(modifier = Modifier.padding(innerPadding)) {
           ZIPItemList(
               items = items,
               onEditQuantity = { item, newQuantity ->
                   viewModel.updateItem(item.copy(quantity = newQuantity))
               }
           )
           // Индикатор загрузки
           if (syncStatus is SyncStatus.LOADING) {
               CircularProgressIndicator(
                   modifier = Modifier
                       .align(Alignment.TopCenter)
                       .padding(16.dp)
               )
           }
       }
    }
}

/*@Composable
fun SyncFAB(status: SyncStatus, onSyncClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (status is SyncStatus.LOADING) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    FloatingActionButton(
        onClick = { if (status !is SyncStatus.LOADING) onSyncClick() },
        containerColor = MaterialTheme.colorScheme.primary, // Исправлено здесь
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Default.Sync,
            contentDescription = "Синхронизация",
            modifier = Modifier.rotate(rotation)
        )
    }
}*/
// Остальной код оставляем без изменений
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ZipCkladTheme {
        Greeting("Android")
    }
}
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Названию, номеру или место") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        singleLine = true,
        modifier = modifier
    )
}