/*
 * InfoFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.info

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.info.model.*

class InfoFragment : NavigationFragment.SubFragment() {

    private var listener: Listener? = null
    private var descriptionItem: InfoDescriptionItem? = null
    private var embeddedInNavigation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            descriptionItem = it.getSerializable(ARG_DESCRIPTION_ITEM) as? InfoDescriptionItem
            embeddedInNavigation = it.getBoolean(ARG_EMBEDDED_IN_NAVIGATION, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info_list, container, false)

        if (embeddedInNavigation)
            view.setBackgroundResource(R.color.colorBackground)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                val manager = GridLayoutManager(context, 2)
                manager.spanSizeLookup = SizeLookup()
                layoutManager = manager
                val actions = ArrayList<InfoItem>()
                actions.add(descriptionItem!!)
                val otherActions = ArrayList(InfoActionItem.infoActions)
                if (descriptionItem!!.hasWebInfo)
                    otherActions.add(InfoWebActionItem())
                if (descriptionItem!!.hasAlternateSurfaces)
                    otherActions.add(AlternateSurfacesItem())
                otherActions.add(SubsystemActionItem())
                otherActions.add(MarkItem())
                actions.addAll(otherActions)
                adapter = InfoRecyclerViewAdapter(actions, listener)
                addItemDecoration(SpaceItemDecoration())
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = ""
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement InfoFragment.nListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onInfoActionSelected(action: InfoActionItem)
    }

    inner class SizeLookup: GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            if (position == 0) { return 2 }
            return 1
        }
    }

    inner class SpaceItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val density = Resources.getSystem().displayMetrics.density
            val spacing = (16 * density).toInt()

            val pos = parent.getChildLayoutPosition(view)
            if (pos == 0) {
                outRect.left = spacing
                outRect.right = spacing
                outRect.top = spacing
                outRect.bottom = spacing
            } else {
                outRect.top = 0
                outRect.bottom = spacing
                if (pos % 2 == 0) {
                    outRect.left = spacing / 2
                    outRect.right = spacing
                } else {
                    outRect.left = spacing
                    outRect.right = spacing / 2
                }
            }
        }
    }

    companion object {
        const val ARG_DESCRIPTION_ITEM = "description-item"
        const val ARG_EMBEDDED_IN_NAVIGATION = "embedded-in-navigation"

        @JvmStatic
        fun newInstance(info: InfoDescriptionItem, embeddedInNavigation: Boolean = false) =
            InfoFragment().apply {
                arguments = Bundle().apply {
                    this.putSerializable(ARG_DESCRIPTION_ITEM, info)
                    this.putBoolean(ARG_EMBEDDED_IN_NAVIGATION, embeddedInNavigation)
                }
            }
    }
}
