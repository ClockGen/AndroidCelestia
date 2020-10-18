/*
 * ResourceItemFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.disposables.CompositeDisposable
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.ProgressButton
import space.celestia.mobilecelestia.common.TitledFragment
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.resource.model.*
import space.celestia.mobilecelestia.utils.commonHandler
import space.celestia.mobilecelestia.utils.showAlert
import java.io.File

class ResourceItemFragment : TitledFragment(), ResourceManager.Listener {
    private var item: ResourceItem? = null
    private var progressButton: ProgressButton? = null
    private var currentState: ResourceItemState = ResourceItemState.None

    private var imageView: ImageView? = null
    private var titleLabel: TextView? = null
    private var descriptionLabel: TextView? = null

    private val compositeDisposable = CompositeDisposable()

    class GlideUrlCustomCacheKey(url: String, val key: String) : GlideUrl(url) {
        override fun getCacheKey(): String {
            return key
        }
    }

    enum class ResourceItemState {
        None, Downloading, Installed
    }

    override val title: String
        get() = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ResourceManager.shared.addListener(this)

        arguments?.let {
            item = it.getSerializable(ARG_ITEM) as? ResourceItem
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resource_item, container, false)
        val title = view.findViewById<TextView>(R.id.title)
        val content = view.findViewById<TextView>(R.id.content)
        val footnote = view.findViewById<TextView>(R.id.footnote)
        val image = view.findViewById<ImageView>(R.id.image)
        // TODO: Localization
        footnote.text = "Note: restarting Celestia is needed to use any new installed plugin."

        val progressButton = view.findViewById<ProgressButton>(R.id.progress_button)
        progressButton.setOnClickListener {
            onProgressButtonClick()
        }
        this.progressButton = progressButton
        this.titleLabel = title
        this.descriptionLabel = content
        this.imageView = image
        updateContents()
        updateUI()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item = this.item ?: return

        // Fetch the latest data from server since user might have come from `Installed`
        val lang = CelestiaAppCore.getLocalizedString("LANGUAGE", "celestia")
        val service = ResourceAPI.shared.create(ResourceAPIService::class.java)
        val disposable = service.item(lang, item.id).commonHandler(ResourceItem::class.java, {
            this.item = it
            updateContents()
        })
        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        ResourceManager.shared.removeListener(this)

        super.onDestroy()
    }

    private fun onProgressButtonClick() {
        val item = this.item ?: return
        val activity = this.activity ?: return

        val dm = ResourceManager.shared

        // Already installed, offer an option for uninstalling
        if (dm.isInstalled(item.id)) {
            // TODO: Localization
            activity.showAlert("Do you want to uninstall this plugin?") {
                var success = false
                try {
                    success = dm.uninstall(item.id)
                } catch (e: Exception) {}
                if (success) {
                    currentState = ResourceItemState.None
                } else {
                    activity.showAlert("Unable to uninstall plugin.")
                }
                updateUI()
            }
            return
        }

        // Already downloading, allow user to cancel
        if (dm.isDownloading(item.id)) {
            // TODO: Localization
            activity.showAlert("Do you want to cancel this task?") {
                dm.cancel(item.id)
                currentState = ResourceItemState.None
                updateUI()
            }
            return
        }

        // Start downloading
        dm.download(item, File(activity.cacheDir, item.id))
        currentState = ResourceItemState.Downloading
        updateUI()
    }

    override fun onResourceFetchError(identifier: String) {
        if (identifier != item?.id) return
        currentState = ResourceItemState.None
        updateUI()
        // TODO: Localization
        activity?.showAlert("Failed to download or install this plugin.")
    }

    override fun onFileDownloaded(identifier: String) {
        if (identifier != item?.id) return
        updateUI()
    }

    override fun onFileUnzipped(identifier: String) {
        if (identifier != item?.id) return
        currentState = ResourceItemState.Installed
        updateUI()
    }

    override fun onProgressUpdate(identifier: String, progress: Float) {
        val id = item?.id ?: return
        if (identifier != id) { return }

        currentState = ResourceItemState.Downloading
        progressButton?.setProgress(progress * 100f)
        updateUI()
    }

    private fun updateContents() {
        titleLabel?.text = item?.name
        descriptionLabel?.text = item?.description

        val imageURL = item?.image
        val itemID = item?.id
        val imageView = this.imageView
        if (imageURL != null && itemID != null && imageView != null) {
            imageView.visibility = View.VISIBLE
            Glide.with(this).load(GlideUrlCustomCacheKey(imageURL, itemID)).into(imageView)
        } else {
            imageView?.visibility = View.GONE
        }
    }

    private fun updateUI() {
        val button = progressButton ?: return
        val id = item?.id ?: return

        // Ensure we are up to date with these cases
        val dm = ResourceManager.shared
        if (dm.isInstalled(id))
            currentState = ResourceItemState.Installed
        if (dm.isDownloading(id))
            currentState = ResourceItemState.Downloading

        // TODO: Localization
        when (currentState) {
            ResourceItemState.None -> {
                button.reset()
                button.setText("DOWNLOAD")
            }
            ResourceItemState.Downloading -> {
                button.setText("DOWNLOADING")
            }
            ResourceItemState.Installed -> {
                button.setProgress(100f)
                button.setText("INSTALLED")
            }
        }
    }

    companion object {
        private const val ARG_ITEM = "item"
        @JvmStatic
        fun newInstance(item: ResourceItem) =
            ResourceItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}