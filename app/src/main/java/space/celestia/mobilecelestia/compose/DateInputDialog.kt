/*
 * DateInputDialog.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.compose

import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun DateInputDialog(onDismissRequest: () -> Unit, confirmHandler: () -> Unit, title: String, text: String?, formatter: SimpleDateFormat, dateChange: (Date?, String) -> Unit) {
    TextInputDialog(
        onDismissRequest = onDismissRequest,
        confirmHandler = confirmHandler,
        title = title,
        text = text,
        placeholder = formatter.format(Date()),
        textChange = {
            try {
                val date = formatter.parse(it)
                dateChange(date, it)
            } catch (ignored: Throwable) {
                dateChange(null, it)
            }
        }
    )
}