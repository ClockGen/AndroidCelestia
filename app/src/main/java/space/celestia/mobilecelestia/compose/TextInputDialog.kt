/*
 * TextInputDialog.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
fun TextInputDialog(onDismissRequest: () -> Unit, confirmHandler: () -> Unit, title: String, text: String?, placeholder: String? = null, textChange: (String) -> Unit) {
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        TextButton(onClick = confirmHandler) {
            Text(text = CelestiaString("OK", ""))
        }
    }, title = {
        Text(text = title)
    }, text = {
        TextField(value = text ?: "", placeholder = if (placeholder != null) { { Text(text = placeholder) } } else null, onValueChange = textChange)
    })
}