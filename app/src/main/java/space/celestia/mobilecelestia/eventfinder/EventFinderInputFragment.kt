/*
 * EventFinderInputFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.eventfinder

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.utils.*
import java.util.*

class EventFinderInputFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null

    private var adapter: EventFinderInputRecyclerViewAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general_grouped_list, container, false)

        val recView = view.findViewById<RecyclerView>(R.id.list)
        recView.layoutManager = LinearLayoutManager(context)
        adapter = EventFinderInputRecyclerViewAdapter({ isStartTime ->
            val ac = context as? Activity ?: return@EventFinderInputRecyclerViewAdapter
            val format = DateFormat.getBestDateTimePattern(Locale.getDefault(),"yyyyMMddHHmmss")
            ac.showDateInput(CelestiaString("Please enter the time in \"%s\" format.", "").format(format), format) { date ->
                if (date == null) {
                    ac.showAlert(CelestiaString("Unrecognized time string.", ""))
                    return@showDateInput
                }
                if (isStartTime) adapter?.startDate = date else adapter?.endDate = date
                adapter?.reload()
                adapter?.notifyDataSetChanged()
            }
        }, { current ->
            val ac = context as? Activity ?: return@EventFinderInputRecyclerViewAdapter
            val objects = listOf("Earth", "Jupiter")
            val other = CelestiaString("Other", "")
            val currentIndex = 0.coerceAtLeast(objects.indexOf(current))
            ac.showSingleSelection(CelestiaString("Please choose an object.", ""), objects.map { CelestiaAppCore.getLocalizedString(it, "celestia") } + other, currentIndex) { index ->
                if (index >= objects.size) {
                    // User choose other, show text input for the object name
                    ac.showTextInput(CelestiaString("Please enter an object name.", "")) { objectName ->
                        adapter?.objectName = objectName
                        adapter?.reload()
                        adapter?.notifyDataSetChanged()
                    }
                    return@showSingleSelection
                }
                adapter?.objectName = objects[index]
                adapter?.reload()
                adapter?.notifyDataSetChanged()
            }
        }, {
            val startDate = adapter?.startDate ?: return@EventFinderInputRecyclerViewAdapter
            val endDate = adapter?.endDate ?: return@EventFinderInputRecyclerViewAdapter
            val objectName = adapter?.objectName ?: return@EventFinderInputRecyclerViewAdapter
            listener?.onSearchForEvent(objectName, startDate, endDate)
        })
        recView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Eclipse Finder", "")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement EventFinderInputFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        adapter = null
    }

    interface Listener {
        fun onSearchForEvent(objectName: String, startDate: Date, endDate: Date)
    }

    companion object {
        private const val TAG = "EventFinderInput"

        @JvmStatic
        fun newInstance() =
            EventFinderInputFragment()
    }
}
