package space.celestia.mobilecelestia.common

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.common_text_list_item.view.*
import space.celestia.mobilecelestia.R

interface BaseTextItemHolder {
    val title: TextView
    val accessory: ImageView
}

class CommonTextViewHolder(parent: ViewGroup):
    RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_text_list_item, parent, false)), BaseTextItemHolder {
    override val title = itemView.title
    override var accessory = itemView.accessory
    val detail = itemView.detail
}