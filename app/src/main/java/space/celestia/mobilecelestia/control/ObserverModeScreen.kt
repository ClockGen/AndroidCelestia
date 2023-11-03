/*
 * ObserverModeScreen.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.control

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.celestia.Observer
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.FooterLink
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.ObjectNameAutoComplete
import space.celestia.mobilecelestia.compose.OptionSelect
import space.celestia.mobilecelestia.control.viewmodel.CameraControlViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
fun ObserverModeScreen(paddingValues: PaddingValues, openLink: (String) -> Unit, modifier: Modifier = Modifier) {
    val viewModel: CameraControlViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val internalViewModifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
            vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
        )
    val coordinateSystems = listOf(
        Pair(Observer.COORDINATE_SYSTEM_UNIVERSAL, CelestiaString("Free Flight", "")),
        Pair(Observer.COORDINATE_SYSTEM_ECLIPTICAL, CelestiaString("Follow", "")),
        Pair(Observer.COORDINATE_SYSTEM_BODY_FIXED, CelestiaString("Sync Orbit", "")),
        Pair(Observer.COORDINATE_SYSTEM_PHASE_LOCK, CelestiaString("Phase Lock", "")),
        Pair(Observer.COORDINATE_SYSTEM_CHASE, CelestiaString("Chase", "")),
    )
    var referenceObjectName by remember {
        mutableStateOf("")
    }
    var referenceObjectPath by remember {
        mutableStateOf("")
    }
    var targetObjectName by remember {
        mutableStateOf("")
    }
    var selectedCoordinateIndex by remember {
        mutableStateOf(0)
    }
    var targetObjectPath by remember {
        mutableStateOf("")
    }
    val selectedCoordinateSystem = coordinateSystems[selectedCoordinateIndex].first
    Column(modifier = modifier
        .verticalScroll(state = rememberScrollState(), enabled = true)
        .padding(paddingValues)) {
        Header(text = CelestiaString("Coordinate System", ""))
        OptionSelect(options = coordinateSystems.map { it.second }, selectedIndex = selectedCoordinateIndex, selectionChange = {
            selectedCoordinateIndex = it
        }, modifier = internalViewModifier)

        if (selectedCoordinateSystem != Observer.COORDINATE_SYSTEM_UNIVERSAL) {
            Header(text = CelestiaString("Reference Object", ""))
            ObjectNameAutoComplete(executor = viewModel.executor, core = viewModel.appCore, name = referenceObjectName, path = referenceObjectPath, inputUpdated = {
                referenceObjectName = it
            }, objectPathUpdated = {
                referenceObjectPath = it
            }, modifier = internalViewModifier)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        }

        if (selectedCoordinateSystem == Observer.COORDINATE_SYSTEM_PHASE_LOCK) {
            Header(text = CelestiaString("Target Object", ""))
            ObjectNameAutoComplete(executor = viewModel.executor, core = viewModel.appCore, name = targetObjectName, path = targetObjectPath, inputUpdated = {
                targetObjectName = it
            }, objectPathUpdated = {
                targetObjectPath = it
            }, modifier = internalViewModifier)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        }

        val infoText = CelestiaString("Flight mode decides how you move around in Celestia. Learn more…", "")
        val infoLinkText = CelestiaString("Learn more…", "")
        FooterLink(text = infoText, linkText = infoLinkText, link = "https://celestia.mobi/help/flight-mode?lang=${AppCore.getLanguage()}", action = { link ->
            openLink(link)
        })

        FilledTonalButton(modifier = internalViewModifier, onClick = {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                applyObserverMode(referenceObjectPath = referenceObjectPath, targetObjectPath = targetObjectPath, coordinateSystem = selectedCoordinateSystem, viewModel.appCore)
            }
        }) {
            Text(text = CelestiaString("OK", ""))
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
    }
}

private fun applyObserverMode(referenceObjectPath: String, targetObjectPath: String, coordinateSystem: Int, appCore: AppCore) {
    val ref = if (referenceObjectPath.isEmpty()) Selection() else appCore.simulation.findObject(referenceObjectPath)
    val target = if (targetObjectPath.isEmpty()) Selection() else appCore.simulation.findObject(targetObjectPath)
    appCore.simulation.activeObserver.setFrame(coordinateSystem, ref, target)
}