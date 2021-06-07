/*
 * ViewHolder.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.common

import android.graphics.drawable.Drawable
import android.util.LayoutDirection
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Flow
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R

interface BaseTextItemHolder {
    val title: TextView
    val accessory: ImageView
}

class CommonTextViewHolder(parent: ViewGroup):
    RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_text_list_item, parent, false)), BaseTextItemHolder {
    override val title: TextView
        get() = itemView.findViewById(R.id.title)
    override val accessory: ImageView
        get() = itemView.findViewById(R.id.accessory)

    val detail: TextView
        get() = itemView.findViewById(R.id.detail)

    init {
        itemView.findViewById<Flow>(R.id.flow).setHorizontalBias(if (itemView.resources.configuration.layoutDirection == LayoutDirection.RTL) 1f else 0f)
    }
}