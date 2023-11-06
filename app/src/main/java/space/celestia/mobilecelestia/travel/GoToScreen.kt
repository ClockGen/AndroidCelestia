/*
 * GoToScreen.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.travel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import space.celestia.celestia.GoToLocation
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.ObjectNameAutoComplete
import space.celestia.mobilecelestia.compose.OptionSelect
import space.celestia.mobilecelestia.travel.viewmodel.GoToViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.toDoubleOrNull
import java.io.Serializable
import java.text.NumberFormat

class GoToData(var objectName: String, var objectPath: String, var longitude: Float, var latitude: Float, var distance: Double, var distanceUnit: GoToLocation.DistanceUnit) :
    Serializable

@Composable
fun GoToScreen(initialData: GoToData, paddingValues: PaddingValues, handler: (GoToData) -> Unit, modifier: Modifier = Modifier) {
    val viewModel: GoToViewModel = hiltViewModel()
    val displayNumberFormat = remember {
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.maximumFractionDigits = 2
        numberFormat.isGroupingUsed = false
        numberFormat
    }
    val parseNumberFormat = remember {
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.isGroupingUsed = false
        numberFormat
    }
    val distanceUnits = remember {
        listOf(
            GoToLocation.DistanceUnit.radii,
            GoToLocation.DistanceUnit.km,
            GoToLocation.DistanceUnit.au
        )
    }

    var objectName by remember {
        mutableStateOf(initialData.objectName)
    }
    var objectPath by remember {
        mutableStateOf(initialData.objectPath)
    }
    var longitudeString by rememberSaveable {
        mutableStateOf(displayNumberFormat.format(initialData.longitude))
    }
    var latitudeString by rememberSaveable {
        mutableStateOf(displayNumberFormat.format(initialData.latitude))
    }
    var distanceString by rememberSaveable {
        mutableStateOf(displayNumberFormat.format(initialData.distance))
    }
    var distanceUnit by rememberSaveable {
        mutableStateOf(initialData.distanceUnit)
    }
    val textViewModifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
            vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
        )
    val currentLongitudeValue = longitudeString.toDoubleOrNull(parseNumberFormat)
    val currentLatitudeValue = latitudeString.toDoubleOrNull(parseNumberFormat)
    val currentDistanceValue = distanceString.toDoubleOrNull(parseNumberFormat)
    val isLongitudeValid = currentLongitudeValue != null && currentLongitudeValue >= -180.0 && currentLongitudeValue <= 180.0
    val isLatitudeValid = currentLatitudeValue != null && currentLatitudeValue >= -90.0 && currentLatitudeValue <= 90.0
    val isDistanceValid = currentDistanceValue != null && currentDistanceValue >= 0.0
    Column(modifier = modifier
        .verticalScroll(state = rememberScrollState(), enabled = true)
        .padding(paddingValues)) {
        Header(text = CelestiaString("Object", ""))
        ObjectNameAutoComplete(executor = viewModel.executor, core = viewModel.appCore, name = objectName, path = objectPath, modifier = textViewModifier, inputUpdated = {
            objectName = it
        }, objectPathUpdated = {
            objectPath = it
        })
        Header(text = CelestiaString("Coordinates", ""))
        Row(modifier = textViewModifier, horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_horizontal))) {
            OutlinedTextField(value = latitudeString, label = { Text(text = CelestiaString("Latitude", "")) }, onValueChange = {
                latitudeString = it
            }, isError = !isLatitudeValid, modifier = Modifier.weight(1.0f))
            OutlinedTextField(value = longitudeString, label = { Text(text = CelestiaString("Longitude", "")) }, onValueChange = {
                longitudeString = it
            }, isError = !isLongitudeValid, modifier = Modifier.weight(1.0f))
        }
        Header(text = CelestiaString("Distance", ""))
        Row(modifier = textViewModifier, horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_horizontal))) {
            OutlinedTextField(value = distanceString, onValueChange = {
                distanceString = it
            }, isError = !isDistanceValid, modifier = Modifier.weight(1.0f))
            OptionSelect(options = distanceUnits.map { CelestiaString(it.name, "") }, selectedIndex = distanceUnits.indexOf(distanceUnit) , selectionChange = {
                distanceUnit = distanceUnits[it]
            })
        }
        FilledTonalButton(
            enabled = isLatitudeValid && isLongitudeValid && isDistanceValid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                    top = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
                    end = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                    bottom = dimensionResource(id = R.dimen.common_page_medium_margin_vertical)
                ),
            onClick = {
                val longitude = currentLongitudeValue ?: return@FilledTonalButton
                val latitude = currentLatitudeValue ?: return@FilledTonalButton
                val distance = currentDistanceValue ?: return@FilledTonalButton
                handler(GoToData(
                    objectName,
                    objectPath,
                    longitude = longitude.toFloat(),
                    latitude = latitude.toFloat(),
                    distance = distance,
                    distanceUnit = distanceUnit
                ))
            }
        ) {
            Text(text = CelestiaString("Go", ""))
        }
    }
}