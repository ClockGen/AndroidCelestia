/*
 * AboutScreen.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.MultiLineTextRow
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.AssetUtils
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.versionCode
import space.celestia.mobilecelestia.utils.versionName

@Composable
fun AboutScreen(paddingValues: PaddingValues, urlHandler: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val aboutSections by remember {
        mutableStateOf(createAboutItems(context))
    }
    LazyColumn(modifier = modifier, contentPadding = paddingValues) {
        for (index in aboutSections.indices) {
            val aboutSection = aboutSections[index]
            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            }
            items(aboutSection) { item ->
                when (item) {
                    is ActionItem -> {
                        TextRow(primaryText = item.title, primaryTextColor = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable {
                            urlHandler(item.url)
                        })
                    }
                    is VersionItem -> {
                        TextRow(primaryText = CelestiaString("Version", ""), secondaryText = item.versionName)
                    }
                    is DetailItem -> {
                        MultiLineTextRow(text = item.detail)
                    }
                    is TitleItem -> {
                        TextRow(primaryText = item.title)
                    }
                }
            }
            item {
                if (index != aboutSections.size - 1) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                    Separator()
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
        }
    }
}

private fun createAboutItems(context: Context): List<List<AboutItem>> {
    val array = ArrayList<List<AboutItem>>()

    // Version
    val versionName = "${context.versionName}(${context.versionCode})"

    array.add(listOf(
        VersionItem(versionName)
    ))

    // Authors
    getInfo("CelestiaResources/AUTHORS", CelestiaString("Authors", ""), context)?.let {
        array.add(it)
    }

    // Translators
    getInfo("CelestiaResources/TRANSLATORS", CelestiaString("Translators", ""), context)?.let {
        array.add(it)
    }

    // Links
    array.add(
        listOf(
            ActionItem(CelestiaString("Development", ""),"https://celestia.mobi/help/development"),
            ActionItem(CelestiaString("Third Party Dependencies", ""), "https://celestia.mobi/help/dependencies"),
            ActionItem(CelestiaString("Privacy Policy and Service Agreement", ""), "https://celestia.mobi/privacy")
        )
    )

    array.add(
        listOf(
            ActionItem(CelestiaString("Official Website", ""), "https://celestia.mobi"),
            ActionItem(CelestiaString("About Celestia", ""), "https://celestia.mobi/about")
        )
    )

    return array
}

private fun getInfo(assetPath: String, title: String, context: Context): List<AboutItem>? {
    try {
        val info = AssetUtils.readFileToText(context, assetPath)
        return listOf(
            TitleItem(title),
            DetailItem(info)
        )
    } catch (_: Throwable) {}
    return null
}
