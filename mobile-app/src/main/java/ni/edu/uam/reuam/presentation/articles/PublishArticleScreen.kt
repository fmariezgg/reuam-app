package ni.edu.uam.reuam.presentation.articles

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ni.edu.uam.reuam.data.ApiResult
import ni.edu.uam.reuam.data.remote.dto.CategoryResponse
import ni.edu.uam.reuam.data.remote.dto.ItemCondition
import ni.edu.uam.reuam.data.remote.dto.ItemTransactionType
import ni.edu.uam.reuam.presentation.components.ErrorContent
import ni.edu.uam.reuam.presentation.components.LoadingContent
import ni.edu.uam.reuam.presentation.components.ReUAMButton
import ni.edu.uam.reuam.presentation.components.ReUAMTextField
import ni.edu.uam.reuam.presentation.home.toDisplayLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishArticleScreen(
    onBackClick: () -> Unit,
    onPublished: () -> Unit = onBackClick,
    viewModel: PublishArticleViewModel = viewModel(),
) {
    val categoriesState by viewModel.categoriesState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val isPublishing by viewModel.isPublishing.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Publicar artículo") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = categoriesState) {
                is ApiResult.Loading -> LoadingContent(message = "Cargando categorías...")

                is ApiResult.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadCategories() }
                )

                is ApiResult.Success -> PublishForm(
                    categories = state.data,
                    formState = formState,
                    isPublishing = isPublishing,
                    onTitleChange = viewModel::onTitleChange,
                    onDescriptionChange = viewModel::onDescriptionChange,
                    onCategorySelected = viewModel::onCategorySelected,
                    onConditionSelected = viewModel::onConditionSelected,
                    onTransactionTypeSelected = viewModel::onTransactionTypeSelected,
                    onPriceChange = viewModel::onPriceChange,
                    onLocationChange = viewModel::onLocationChange,
                    onPublishClick = {
                        viewModel.publish(
                            onSuccess = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("¡Artículo publicado!")
                                    onPublished()
                                }
                            },
                            onError = { message ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                            }
                        )
                    },
                    onBackClick = onBackClick,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PublishForm(
    categories: List<CategoryResponse>,
    formState: PublishFormState,
    isPublishing: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategorySelected: (CategoryResponse) -> Unit,
    onConditionSelected: (ItemCondition) -> Unit,
    onTransactionTypeSelected: (ItemTransactionType) -> Unit,
    onPriceChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onPublishClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ReUAMTextField(
            value = formState.title,
            onValueChange = onTitleChange,
            label = "Título del artículo",
            isError = formState.fieldErrors.containsKey("title"),
            supportingText = formState.fieldErrors["title"],
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ReUAMTextField(
            value = formState.description,
            onValueChange = onDescriptionChange,
            label = "Descripción",
            singleLine = false,
            isError = formState.fieldErrors.containsKey("description"),
            supportingText = formState.fieldErrors["description"],
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (categories.isNotEmpty()) {
            EnumDropdown(
                label = "Categoría",
                selectedLabel = formState.selectedCategory?.name ?: "Selecciona una categoría",
                options = categories,
                optionLabel = { it.name },
                onOptionSelected = onCategorySelected,
            )
            Box(modifier = Modifier.padding(bottom = 12.dp))
        }

        EnumDropdown(
            label = "Condición del artículo",
            selectedLabel = formState.condition.toDisplayLabel(),
            options = ItemCondition.entries,
            optionLabel = { it.toDisplayLabel() },
            onOptionSelected = onConditionSelected,
        )
        Box(modifier = Modifier.padding(bottom = 12.dp))

        EnumDropdown(
            label = "Tipo de transacción",
            selectedLabel = formState.transactionType.toDisplayLabel(),
            options = ItemTransactionType.entries,
            optionLabel = { it.toDisplayLabel() },
            onOptionSelected = onTransactionTypeSelected,
        )
        Box(modifier = Modifier.padding(bottom = 12.dp))

        if (formState.transactionType == ItemTransactionType.SYMBOLIC_SALE) {
            ReUAMTextField(
                value = formState.priceText,
                onValueChange = onPriceChange,
                label = "Precio simbólico (en córdobas, sin centavos)",
                placeholder = "Ej. 50",
                isError = formState.fieldErrors.containsKey("priceCents"),
                supportingText = formState.fieldErrors["priceCents"]
                    ?: "Se guarda como monto simbólico, no es un precio de mercado",
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        ReUAMTextField(
            value = formState.location,
            onValueChange = onLocationChange,
            label = "Lugar de entrega (opcional)",
            placeholder = "Ej. Edificio de Ingeniería, UAM",
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isPublishing) {
            Box(modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
            }
        } else {
            ReUAMButton(
                text = "Publicar artículo",
                onClick = onPublishClick,
            )
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Cancelar")
        }
    }
}

/**
 * Selector simple de una opción dentro de una lista fija, usando el
 * ExposedDropdownMenuBox estándar de Material 3. Genérico para no repetir
 * este bloque 3 veces (categoría, condición, tipo de transacción).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    selectedLabel: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
