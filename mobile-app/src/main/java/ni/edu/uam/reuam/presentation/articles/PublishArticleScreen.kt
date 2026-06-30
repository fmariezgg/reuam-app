package ni.edu.uam.reuam.presentation.articles

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import ni.edu.uam.reuam.ui.theme.ReUAMAccent
import ni.edu.uam.reuam.ui.theme.ReUAMBackground
import ni.edu.uam.reuam.ui.theme.ReUAMError
import ni.edu.uam.reuam.ui.theme.ReUAMGreen
import ni.edu.uam.reuam.ui.theme.ReUAMGreenSoft
import ni.edu.uam.reuam.ui.theme.ReUAMSurface
import ni.edu.uam.reuam.ui.theme.ReUAMTextPrimary
import ni.edu.uam.reuam.ui.theme.ReUAMTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishArticleScreen(
    onBackClick: () -> Unit,
    onPublished: () -> Unit = onBackClick,
    viewModel: PublishArticleViewModel = viewModel(factory = PublishArticleViewModel.Factory),
) {
    val context = LocalContext.current
    val categoriesState by viewModel.categoriesState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val isPublishing by viewModel.isPublishing.collectAsState()
    val isLoadingItemToEdit by viewModel.isLoadingItemToEdit.collectAsState()
    val loadItemError by viewModel.loadItemError.collectAsState()
    val isEditMode = viewModel.isEditMode

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEditMode) "Editar artículo" else "Publicar artículo",
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (isEditMode) "Actualiza los datos de tu publicación" else "Comparte algo útil con la comunidad",
                            style = MaterialTheme.typography.bodySmall,
                            color = ReUAMTextSecondary,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ReUAMSurface),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ReUAMBackground,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            val itemLoadFailed = loadItemError != null
            when {
                itemLoadFailed -> ErrorContent(
                    message = loadItemError ?: "No se pudo cargar el artículo a editar.",
                    onRetry = onBackClick,
                )

                isLoadingItemToEdit -> LoadingContent(message = "Cargando artículo...")

                categoriesState is ApiResult.Loading -> LoadingContent(message = "Cargando categorías...")

                categoriesState is ApiResult.Error -> ErrorContent(
                    message = (categoriesState as ApiResult.Error).message,
                    onRetry = { viewModel.loadCategories() }
                )

                else -> PublishForm(
                    categories = (categoriesState as ApiResult.Success).data,
                    formState = formState,
                    isPublishing = isPublishing,
                    isEditMode = isEditMode,
                    onTitleChange = viewModel::onTitleChange,
                    onDescriptionChange = viewModel::onDescriptionChange,
                    onCategorySelected = viewModel::onCategorySelected,
                    onConditionSelected = viewModel::onConditionSelected,
                    onTransactionTypeSelected = viewModel::onTransactionTypeSelected,
                    onPriceChange = viewModel::onPriceChange,
                    onLocationChange = viewModel::onLocationChange,
                    onPhotosSelected = { viewModel.onPhotoUrisSelected(context, it) },
                    onRemoveSelectedPhoto = viewModel::removeSelectedPhoto,
                    onPublishClick = {
                        viewModel.publish(
                            context = context,
                            onSuccess = { _ ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (isEditMode) "Cambios guardados" else "¡Artículo publicado!"
                                    )
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
    isEditMode: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategorySelected: (CategoryResponse) -> Unit,
    onConditionSelected: (ItemCondition) -> Unit,
    onTransactionTypeSelected: (ItemTransactionType) -> Unit,
    onPriceChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onPhotosSelected: (List<Uri>) -> Unit,
    onRemoveSelectedPhoto: (Uri) -> Unit,
    onPublishClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FormHintCard(isEditMode = isEditMode)

        FormSectionCard(title = "Información básica") {
            ReUAMTextField(
                value = formState.title,
                onValueChange = onTitleChange,
                label = "Título del artículo",
                placeholder = "Ej. Cálculo I - Larson 9na edición",
                isError = formState.fieldErrors.containsKey("title"),
                supportingText = formState.fieldErrors["title"],
            )

            ReUAMTextField(
                value = formState.description,
                onValueChange = onDescriptionChange,
                label = "Descripción",
                placeholder = "Describe el estado, uso y detalles importantes",
                singleLine = false,
                isError = formState.fieldErrors.containsKey("description"),
                supportingText = formState.fieldErrors["description"],
            )

            if (categories.isNotEmpty()) {
                EnumDropdown(
                    label = "Categoría",
                    selectedLabel = formState.selectedCategory?.name ?: "Selecciona una categoría",
                    options = categories,
                    optionLabel = { it.name },
                    onOptionSelected = onCategorySelected,
                )
            }
        }

        FormSectionCard(title = "Tipo de publicación") {
            OptionFlow(
                options = ItemTransactionType.entries,
                selected = formState.transactionType,
                label = { it.toDisplayLabel() },
                onSelected = onTransactionTypeSelected,
            )

            if (formState.transactionType == ItemTransactionType.SYMBOLIC_SALE) {
                ReUAMTextField(
                    value = formState.priceText,
                    onValueChange = onPriceChange,
                    label = "Precio simbólico",
                    placeholder = "Ej. 50",
                    isError = formState.fieldErrors.containsKey("priceCents"),
                    supportingText = formState.fieldErrors["priceCents"]
                        ?: "Monto de apoyo, no precio comercial.",
                )
            }
        }

        FormSectionCard(title = "Condición y entrega") {
            OptionFlow(
                options = ItemCondition.entries,
                selected = formState.condition,
                label = { it.toDisplayLabel() },
                onSelected = onConditionSelected,
            )

            ReUAMTextField(
                value = formState.location,
                onValueChange = onLocationChange,
                label = "Lugar sugerido de entrega",
                placeholder = "Ej. Biblioteca central, edificio A",
            )
        }

        FormSectionCard(title = "Fotos") {
            PhotoPickerSection(
                formState = formState,
                onPhotosSelected = onPhotosSelected,
                onRemoveSelectedPhoto = onRemoveSelectedPhoto,
            )
        }

        if (isPublishing) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = ReUAMGreenSoft,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = ReUAMGreen)
                    Text(
                        text = if (isEditMode) "Guardando cambios..." else "Publicando artículo...",
                        modifier = Modifier.padding(start = 12.dp),
                        color = ReUAMTextPrimary,
                    )
                }
            }
        } else {
            ReUAMButton(
                text = if (isEditMode) "Guardar cambios" else "Publicar artículo",
                onClick = onPublishClick,
            )
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Cancelar")
        }

        Spacer(modifier = Modifier.size(12.dp))
    }
}

@Composable
private fun FormHintCard(isEditMode: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = ReUAMGreenSoft,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = ReUAMGreen, modifier = Modifier.size(20.dp))
            Column {
                Text(
                    text = if (isEditMode) "Revisa la información antes de guardar" else "Publica con información clara",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ReUAMTextPrimary,
                )
                Text(
                    text = "Mientras más completo sea el artículo, más fácil será que otro estudiante lo solicite con confianza.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ReUAMTextSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun FormSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ReUAMSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = ReUAMTextPrimary,
            )
            content()
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun <T> OptionFlow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Surface(
                onClick = { onSelected(option) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) ReUAMGreen else ReUAMSurface,
                border = BorderStroke(1.dp, if (isSelected) ReUAMGreen else ReUAMGreen.copy(alpha = 0.18f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (isSelected) {
                        Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = label(option),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) Color.White else ReUAMTextPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoPickerSection(
    formState: PublishFormState,
    onPhotosSelected: (List<Uri>) -> Unit,
    onRemoveSelectedPhoto: (Uri) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = onPhotosSelected,
    )
    val totalPhotos = formState.existingPhotos.size + formState.selectedPhotos.size

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = ReUAMBackground,
            border = BorderStroke(1.dp, ReUAMGreen.copy(alpha = 0.14f)),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fotos del artículo", fontWeight = FontWeight.SemiBold, color = ReUAMTextPrimary)
                    Text(
                        text = "$totalPhotos de 8 seleccionadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = ReUAMTextSecondary,
                    )
                }
                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    enabled = totalPhotos < 8,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Agregar", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }

        formState.fieldErrors["photos"]?.let { error ->
            Text(text = error, color = ReUAMError, style = MaterialTheme.typography.bodySmall)
        }

        if (formState.existingPhotos.isNotEmpty()) {
            Text(
                text = "${formState.existingPhotos.size} foto(s) guardada(s) en este artículo",
                style = MaterialTheme.typography.bodySmall,
                color = ReUAMTextSecondary,
            )
        }

        formState.selectedPhotos.forEach { photo ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = ReUAMBackground,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = photo.displayName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ReUAMTextPrimary,
                    )
                    IconButton(onClick = { onRemoveSelectedPhoto(photo.uri) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Close, contentDescription = "Quitar foto")
                    }
                }
            }
        }
    }
}

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
            shape = RoundedCornerShape(16.dp),
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
