package com.example.bethedonor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bethedonor.utils.ValidationResult
import com.example.bethedonor.ui.theme.ErrorColor
import com.example.bethedonor.ui.theme.Gray1
import com.example.bethedonor.ui.theme.fadeBlue1
import com.example.bethedonor.ui.theme.fadeBlue11
import com.example.bethedonor.ui.theme.teal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectStateDistrictCityField(
    label: String,
    options: List<String>,
    selectedValue: String,
    onSelection: (String) -> ValidationResult,
    recheckField: Boolean = false,
    modifier: Modifier,
    onSearchTextFieldClicked: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isErrorState by rememberSaveable { mutableStateOf(false) }
    var supportingTextState by rememberSaveable { mutableStateOf("") }
    var searchText by remember { mutableStateOf(selectedValue) } // Initialize with selectedValue
    var dropdownSearchText by rememberSaveable { mutableStateOf("") }

    // Update searchText whenever selectedValue changes
    val updatedSelectedValue by rememberUpdatedState(selectedValue)
    LaunchedEffect(updatedSelectedValue) {
        searchText = updatedSelectedValue
    }

    val filteredOptions = options.filter {
        it.contains(dropdownSearchText, ignoreCase = true)
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val textFieldWidth = configuration.screenWidthDp.dp
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            readOnly = true,
            label = { Text(text = label, fontSize = 14.sp) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedLabelColor = teal,
                focusedBorderColor = teal,
                unfocusedBorderColor = Gray1,
                unfocusedLabelColor = Color.Gray,
                cursorColor = teal,
                focusedLeadingIconColor = Color.White,
                unfocusedLeadingIconColor = Color.Gray,
                focusedTrailingIconColor = Color.White,
                unfocusedTrailingIconColor = Color.Gray,
                errorLabelColor = ErrorColor,
                errorBorderColor = ErrorColor,
                errorLeadingIconColor = ErrorColor,
                errorCursorColor = ErrorColor
            ),
            textStyle = TextStyle(color = Color.White),
            shape = RoundedCornerShape(16.dp),
            isError = if (recheckField) {
                val result = onSelection(searchText)
                supportingTextState = result.errorComment
                isErrorState = !result.status
                isErrorState
            } else isErrorState,
            supportingText = {
                if (isErrorState) {
                    Text(
                        text = supportingTextState,
                        modifier = Modifier.fillMaxWidth(),
                        color = ErrorColor
                    )
                }
            },
            value = searchText,
            onValueChange = { /* No action needed since it is read-only */ },
            modifier = Modifier
                .fillMaxWidth(),
            interactionSource = remember { MutableInteractionSource() }
                .also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        keyboardController?.show()
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                expanded = !expanded
                            }
                        }
                    }
                },
        )
        if (expanded) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(fadeBlue11)
                    .width(textFieldWidth * 0.8f)
                    .wrapContentHeight(),
                scrollState = rememberScrollState()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TextField(
                        value = dropdownSearchText,
                        onValueChange = { searchText ->
                            dropdownSearchText = searchText
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "search",
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        placeholder = {
                            Text(
                                text = "Search",
                                style = TextStyle(color = Gray1, fontSize = 16.sp)
                            )
                        },
                        textStyle = TextStyle(
                            textIndent = TextIndent(),
                            textAlign = TextAlign.Start,
                            fontSize = 16.sp
                        ),
                        trailingIcon = {
                            if (dropdownSearchText.isNotEmpty())
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "clear",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable {
                                            dropdownSearchText = ""
                                        }
                                )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .focusRequester(focusRequester),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = fadeBlue1,
                            unfocusedContainerColor = fadeBlue1,
                            disabledContainerColor = fadeBlue1,
                            focusedTextColor = Color.White,
                            disabledTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            unfocusedLeadingIconColor = Gray1,
                            focusedLeadingIconColor = Gray1,
                            unfocusedTrailingIconColor = Color.White,
                            focusedTrailingIconColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        interactionSource = remember { MutableInteractionSource() }
                            .also { interactionSource ->
                                LaunchedEffect(interactionSource) {
                                    focusRequester.requestFocus()
                                    interactionSource.interactions.collect {
                                        if (it is PressInteraction.Release) {
                                            onSearchTextFieldClicked()
                                        }
                                    }
                                }
                            },
                    )
                    LazyColumn(
                        modifier = Modifier
                            .width(textFieldWidth * 0.8f)
                            .height(screenHeight * 0.6f)
                    ) {
                        items(filteredOptions) { option ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expanded = false
                                        searchText = option
                                        dropdownSearchText = ""
                                        val validationResult = onSelection(option)
                                        supportingTextState = validationResult.errorComment
                                        isErrorState = !validationResult.status
                                    }
                            ) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = option,
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                        .width(textFieldWidth),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
