/*
 * CommonSettingsScreen.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.CheckboxRow
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.RadioButtonRow
import space.celestia.mobilecelestia.compose.SelectionInputDialog
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.SliderRow
import space.celestia.mobilecelestia.compose.SwitchRow
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.PreferenceManager

@Composable
fun CommonSettingsScreen(paddingValues: PaddingValues, item: SettingsCommonItem, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, contentPadding = paddingValues) {
        for (index in item.sections.indices) {
            val section = item.sections[index]
            item {
                val header = section.header
                if (header.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                } else {
                    Header(text = header)
                }
            }
            items(section.rows) { item ->
                SettingEntry(item = item)
            }
            item {
                val footer = section.footer
                if (!footer.isNullOrEmpty()) {
                    Footer(text = footer)
                }
                if (index == item.sections.size - 1) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
                } else {
                    val nextHeader = item.sections[index + 1].header
                    if (nextHeader.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                        if (footer.isNullOrEmpty()) {
                            Separator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingEntry(item: SettingsItem) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    if (item is SettingsSwitchItem) {
        var on by remember {
            mutableStateOf(viewModel.appCore.getBooleanValueForPield(item.key))
        }
        when (item.representation) {
            SettingsSwitchItem.Representation.Switch -> {
                SwitchRow(primaryText = item.name, checked = on, onCheckedChange = { newValue ->
                    on = newValue
                    if (!item.volatile)
                        viewModel.coreSettings[PreferenceManager.CustomKey(item.key)] = if (newValue) "1" else "0"
                    scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.setBooleanValueForField(item.key, newValue)
                    }
                })
            }
            SettingsSwitchItem.Representation.Checkmark -> {
                CheckboxRow(primaryText = item.name, checked = on, onCheckedChange = { newValue ->
                    on = newValue
                    if (!item.volatile)
                        viewModel.coreSettings[PreferenceManager.CustomKey(item.key)] = if (newValue) "1" else "0"
                    scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.setBooleanValueForField(item.key, newValue)
                    }
                })
            }
        }
    } else if (item is SettingsSelectionSingleItem) {
        var selected by remember {
            val value = viewModel.appCore.getIntValueForField(item.key)
            mutableIntStateOf(
                if (item.options.any { it.first == value }) {
                    value
                } else {
                    item.defaultSelection
                }
            )
        }
        if (item.showTitle) {
            TextRow(primaryText = item.name, secondaryText = item.subtitle)
        }
        for (option in item.options) {
            RadioButtonRow(primaryText = option.second, selected = option.first == selected) {
                selected = option.first
                viewModel.coreSettings[PreferenceManager.CustomKey(item.key)] = option.first.toString()
                scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.setIntValueForField(item.key, option.first)
                }
            }
        }
    } else if (item is SettingsPreferenceSwitchItem) {
        var on by remember {
            mutableStateOf(when (viewModel.appSettings[item.key]) { "true" -> true "false" -> false else -> item.defaultOn })
        }
        SwitchRow(primaryText = item.name, secondaryText = item.subtitle, checked = on, onCheckedChange = { newValue ->
            on = newValue
            viewModel.appSettings[item.key] = if (newValue) "true" else "false"
        })
    } else if (item is SettingsPreferenceSelectionItem) {
        var selected by remember {
            mutableIntStateOf(viewModel.appSettings[item.key]?.toIntOrNull() ?: item.defaultSelection)
        }
        var showSelectionDialog by remember {
            mutableStateOf(false)
        }
        TextRow(
            primaryText = item.name,
            secondaryText = item.options.firstOrNull { it.first == selected }?.second,
            modifier = Modifier.clickable {
                showSelectionDialog = true
            }
        )
        if (showSelectionDialog) {
            var pendingSelectionIndex by remember {
                mutableIntStateOf(item.options.indexOfFirst { it.first == selected })
            }
            SelectionInputDialog(
                onDimissRequest = { showSelectionDialog = false },
                selectedIndex = pendingSelectionIndex,
                items = item.options.map { it.second },
                selectionChangeHandler = {
                    pendingSelectionIndex = it
                }
            ) {
                showSelectionDialog = false
                if (pendingSelectionIndex >= 0 && pendingSelectionIndex < item.options.size) {
                    val value = item.options[pendingSelectionIndex].first
                    selected = value
                    viewModel.appSettings[item.key] = value.toString()
                }
            }
        }
    } else if (item is SettingsActionItem) {
        TextRow(primaryText = item.name, modifier = Modifier.clickable {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                viewModel.appCore.charEnter(item.action)
            }
        })
    } else if (item is SettingsUnknownTextItem) {
        TextRow(primaryText = item.name, modifier = Modifier.clickable {
            when (item.id) {
                settingUnmarkAllID -> {
                    scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.simulation.universe.unmarkAll()
                    }
                }
            }
        })
    } else if (item is SettingsSliderItem) {
        var value by remember {
            mutableFloatStateOf(viewModel.appCore.getDoubleValueForField(item.key).toFloat())
        }
        SliderRow(primaryText = item.name, value = value, valueRange = item.minValue.toFloat()..item.maxValue.toFloat(), onValueChange = { newValue ->
            value = newValue
            viewModel.coreSettings[PreferenceManager.CustomKey(item.key)] = newValue.toString()
            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                viewModel.appCore.setDoubleValueForField(item.key, newValue.toDouble())
            }
        })
    } else if (item is SettingsPreferenceSliderItem) {
        var value by remember {
            mutableFloatStateOf(viewModel.appSettings[item.key]?.toFloat() ?: item.defaultValue.toFloat())
        }
        SliderRow(primaryText = item.name, secondaryText = item.subtitle, value = value, valueRange = item.minValue.toFloat()..item.maxValue.toFloat(), onValueChange = { newValue ->
            value = newValue
            viewModel.appSettings[item.key] = newValue.toString()
        })
    }
}