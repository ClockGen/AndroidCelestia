/*
 * EventFinderContainerFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.eventfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.TitledFragment
import space.celestia.mobilecelestia.common.pop
import space.celestia.mobilecelestia.common.push
import space.celestia.mobilecelestia.common.replace
import space.celestia.mobilecelestia.utils.CelestiaString

class EventFinderContainerFragment : Fragment() {
    private val toolbar by lazy { requireView().findViewById<Toolbar>(R.id.toolbar) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_general_container_with_toolbar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.title = CelestiaString("Eclipse Finder", "")
        toolbar.setNavigationOnClickListener {
            popItem()
        }
        replace(EventFinderInputFragment.newInstance(), R.id.fragment_container)
    }

    fun showResult() {
        push(EventFinderResultFragment.newInstance(), R.id.fragment_container)
        toolbar.navigationIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_action_arrow_back, null)
    }

    private fun popItem() {
        pop()
        val index = childFragmentManager.backStackEntryCount - 1
        if (index == 0) {
            // no more return
            toolbar.navigationIcon = null
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            EventFinderContainerFragment()
    }
}
