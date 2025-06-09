package com.example.zipcklad.ui.screens

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.zipcklad.ui.components.ZIPItemList
import com.example.zipcklad.ui.theme.ZipCkladTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.zipcklad.ui.components.SyncStatus
import com.example.zipcklad.ui.viewmodel.ZIPViewModel


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
    val isMaster by viewModel.isMaster.collectAsState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

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
        topBar = {
            AppBar(
                searchQuery = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onMenuClick = { showMenu = true },
                /*onAuthClick = {
                    if (isMaster) viewModel.logout()
                    else showPasswordDialog = true
                },*/
                isMaster = isMaster,
                showMenu = showMenu,
                onDismissMenu = { showMenu = false },
                onSyncClick = { viewModel.syncWithServer() },
                onImportClick = {
                    filePicker.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                },
                onAuthClick = { // Добавили обработчик для меню
                    if (isMaster) viewModel.logout()
                    else showPasswordDialog = true
                }
            )
        },
        floatingActionButton = {
            // Кнопка добавления только для мастера
            if (isMaster) {
                ExtendedFloatingActionButton(
                    onClick = { showAddItemDialog = true },
                    icon = { Icon(Icons.Default.Add, "Добавить") },
                    text = { Text("Добавить") }
                )
            }
        }
    ) { innerPadding ->
       Box(modifier = Modifier.padding(innerPadding)) {
           ZIPItemList(
               items = items,
               onEditQuantity = { item, newQuantity ->
                   viewModel.updateItem(item.copy(quantity = newQuantity))
               },
               onLongClick = { item ->
                   if (isMaster) {
                       viewModel.viewModelScope.launch {
                           viewModel.deleteItem(item)
                       }
                   }
               }
           )

           // Индикатор загрузки
           if (syncStatus is SyncStatus.LOADING) {
               Box(
                   modifier = Modifier
                       .fillMaxSize()
                       .padding(32.dp),
                   contentAlignment = Alignment.Center
               ) {
                   CircularProgressIndicator()
               }
           }
           // Диалог авторизации
           if (showPasswordDialog) {
               PasswordDialog(
                   onDismiss = { showPasswordDialog = false },
                   onConfirm = { password ->
                       if (viewModel.login(password)) {
                           Toast.makeText(context, "Авторизован как мастер", Toast.LENGTH_SHORT).show()
                           showPasswordDialog = false
                       } else {
                           Toast.makeText(context, "Неверный пароль", Toast.LENGTH_SHORT).show()
                       }
                   }
               )
           }
           // Диалог добавления элемента
           if (showAddItemDialog) {
               AddItemDialog(
                   onDismiss = { showAddItemDialog = false },
                   onConfirm = { name, partNumber, quantity, location ->
                       viewModel.addNewItem(name, partNumber, quantity, location)
                   }
               )
           }
       }
    }
}

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
        placeholder = { Text("Название, номер, место") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        singleLine = true,
        modifier = modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    onAuthClick: () -> Unit,
    isMaster: Boolean,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onSyncClick: () -> Unit,
    onImportClick: () -> Unit
) {

    TopAppBar(
        title = {
            SearchBar(
                query = searchQuery,
                onQueryChange = onQueryChange,
                modifier = Modifier.fillMaxWidth()
            )
        },
        actions = {
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.MoreVert, "Меню")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(
                        text = { Text("Синхронизация") }, // Исправлено
                        onClick = {
                            onSyncClick()
                            onDismissMenu()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Импорт Excel") }, // Исправлено
                        onClick = {
                            onImportClick()
                            onDismissMenu()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isMaster) "Выйти" else "Авторизация") }, // Исправлено
                        onClick = {
                            onAuthClick()
                            onDismissMenu()
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun PasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Авторизация мастера") },
        text = {
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(password) }) {
                Text("Войти")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var partNumber by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var partNumberError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить ЗИП") },
        text = {
            Column {
                // Поле названия
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Название", color = if (nameError) Color.Red else MaterialTheme.colorScheme.onSurface)
                    Text("*", color = Color.Red)
                }
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    isError = nameError,
                    singleLine = true
                )

                // Поле заказного номера
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Заказной номер", color = if (partNumberError) Color.Red else MaterialTheme.colorScheme.onSurface)
                    Text("*", color = Color.Red)
                }
                TextField(
                    value = partNumber,
                    onValueChange = {
                        partNumber = it
                        partNumberError = it.isBlank()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    isError = partNumberError,
                    singleLine = true
                )

                // Поле количества
                Text("Количество")
                TextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // Поле местоположения
                Text("Местоположение")
                TextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nameError = name.isBlank()
                    partNumberError = partNumber.isBlank()

                    if (nameError || partNumberError) {
                        Toast.makeText(
                            context,
                            "Заполните обязательные поля (отмечены *)",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val qty = quantity.toIntOrNull() ?: 0
                        onConfirm(name, partNumber, qty, location)
                        Toast.makeText(
                            context,
                            "ЗИП '$name' добавлен",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss()
                    }
                }
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}