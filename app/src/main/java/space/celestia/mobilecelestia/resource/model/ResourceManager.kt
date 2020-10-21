/*
 * ResourceManager.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource.model

import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.lingala.zip4j.ZipFile
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.lang.ref.WeakReference

class DownloadTask(
    val item: ResourceItem,
    val destination: File,
    val unzipDestination: File,
    val listener: WeakReference<Listener>
): AsyncTask<Void, Long, Boolean>() {
    enum class State {
        Downloading, Unzipping
    }

    var state: State = State.Downloading

    interface Listener {
        fun onProgressUpdate(identifier: String, bytesWritten: Long, totalBytes: Long)
        fun onFileDownloaded(identifier: String)
        fun onUnzipFinished(identifier: String)
        fun onResourceFetchingFailed(identifier: String)
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        val client = OkHttpClient()
        val call = client.newCall(Request.Builder().url(item.item).get().build())
        try {
            val response = call.execute()
            val body = response.body()
            if (!response.isSuccessful || body == null) {
                return false
            }

            val totalLength = body.contentLength()
            var writtenLength = 0L
            body.byteStream().use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        writtenLength += bytes
                        publishProgress(writtenLength, totalLength)

                        // Check if user has canceled
                        if (isCancelled)
                            return false
                        bytes = input.read(buffer)
                    }
                }
            }
            unzip(destination, unzipDestination)
            // We also save a `description.json` just in case for future use
            try {
                val writer = FileWriter(File(unzipDestination, "description.json"))
                writer.use {
                    val gson = GsonBuilder().create()
                    gson.toJson(item, it)
                }
            } catch (ignored: Exception) {
                print(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    override fun onProgressUpdate(vararg values: Long?) {
        if (values[1] == values[0]) {
            listener.get()?.onFileDownloaded(item.id)
            state = State.Unzipping
        } else {
            listener.get()?.onProgressUpdate(item.id, values[0]!!, values[1]!!)
        }
    }

    override fun onPostExecute(result: Boolean?) {
        if (result!!)
            listener.get()?.onUnzipFinished(item.id)
        else
            listener.get()?.onResourceFetchingFailed(item.id)
    }

    private fun unzip(zipFile: File, destination: File) {
        ZipFile(zipFile).extractAll(destination.absolutePath)
    }
}

class ResourceManager: DownloadTask.Listener {
    private var listeners = arrayListOf<Listener>()
    private var tasks = hashMapOf<String, DownloadTask>()

    var addonDirectory: String? = null

    interface Listener {
        fun onProgressUpdate(identifier: String, progress: Float)
        fun onFileDownloaded(identifier: String)
        fun onFileUnzipped(identifier: String)
        fun onResourceFetchError(identifier: String)
    }

    fun addListener(listener: Listener) {
        if (!listeners.contains(listener))
            listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun isDownloading(identifier: String): Boolean {
        return tasks[identifier] != null
    }

    fun isInstalled(identifier: String): Boolean {
        return File(addonDirectory, identifier).exists()
    }

    fun installedResources(): List<ResourceItem> {
        val parentDirPath = addonDirectory ?: return listOf()
        val parentDir = File(parentDirPath)
        if (!parentDir.exists() || !parentDir.isDirectory) return listOf()
        val items = arrayListOf<ResourceItem>()

        for (folder in parentDir.listFiles() ?: arrayOf()) {
            if (!folder.isDirectory) {
                continue
            }
            val jsonDescriptionFile = File(folder, "description.json")
            if (!jsonDescriptionFile.exists() || jsonDescriptionFile.isDirectory)
                continue
            try {
                val reader = FileReader(jsonDescriptionFile)
                reader.use {
                    val gson = GsonBuilder().create()
                    val item = gson.fromJson<ResourceItem>(it, ResourceItem::class.java)
                    if (item.id == folder.name)
                        items.add(item)
                }
            } catch (ignored: Exception) {}
        }
        return items
    }

    fun uninstall(identifier: String): Boolean {
        return File(addonDirectory, identifier).deleteRecursively()
    }

    fun cancel(identifier: String) {
        tasks.remove(identifier)?.cancel(true)
    }

    fun download(item: ResourceItem, destination: File) {
        val unzipDestination = File(addonDirectory, item.id)
        val task = DownloadTask(item, destination, unzipDestination, WeakReference(this))
        task.execute()
        tasks[item.id] = task
    }

    fun callListenerProgressCallback(identifier: String, progress: Float) {
        for (listener in listeners)
            listener.onProgressUpdate(identifier, progress)
    }

    fun callListenerErrorCallback(identifier: String) {
        for (listener in listeners)
            listener.onResourceFetchError(identifier)
    }

    fun callListenerDownloadSuccessCallback(identifier: String) {
        for (listener in listeners)
            listener.onFileDownloaded(identifier)
    }

    fun callListenerUnzipSuccessCallback(identifier: String) {
        for (listener in listeners)
            listener.onFileUnzipped(identifier)
    }

    override fun onFileDownloaded(identifier: String) {
        Log.d(TAG, "File download success")
        callListenerDownloadSuccessCallback(identifier)
    }

    override fun onProgressUpdate(identifier: String, bytesWritten: Long, totalBytes: Long) {
        Log.d(TAG, "Progress update")
        callListenerProgressCallback(identifier, bytesWritten.toFloat() / totalBytes.toFloat())
    }

    override fun onResourceFetchingFailed(identifier: String) {
        Log.d(TAG, "Resource fetch failed")
        tasks.remove(identifier)
        callListenerErrorCallback(identifier)
    }

    override fun onUnzipFinished(identifier: String) {
        Log.d(TAG, "Unzip finished")
        tasks.remove(identifier)
        callListenerUnzipSuccessCallback(identifier)
    }

    companion object {
        var shared = ResourceManager()
        private const val TAG = "ResourceManager"
    }
}