/*
 * FavoriteBaseItem.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.favorite

import space.celestia.celestia.Destination
import space.celestia.celestia.Script
import space.celestia.celestiafoundation.favorite.BookmarkNode
import space.celestia.mobilecelestia.utils.CelestiaString
import java.io.Serializable
import java.util.*

enum class FavoriteItemAction {
    Delete, Rename, Share
}

interface FavoriteBaseItem : Serializable {
    val children: List<FavoriteBaseItem>
    val isLeaf: Boolean
    val title: String

    val supportedItemActions: List<FavoriteItemAction>
        get() = listOf()

    val hasFullPageRepresentation: Boolean
}

interface MutableFavoriteBaseItem : FavoriteBaseItem {
    fun insert(newItem: FavoriteBaseItem, index: Int)

    fun append(newItem: FavoriteBaseItem) {
        insert(newItem, children.size)
    }

    fun remove(index: Int)
    fun rename(newName: String)
    fun move(fromIndex: Int, toIndex: Int)
}

enum class FavoriteType {
    Script,
    Destination
}

class FavoriteRoot : FavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() = listOf(
            FavoriteTypeItem(FavoriteType.Script),
            FavoriteBookmarkRootItem(currentBookmarkRoot),
            FavoriteTypeItem(FavoriteType.Destination)
        )
    override val title: String
        get() = CelestiaString("Favorites", "")
    override val isLeaf: Boolean
        get() = false
    override val hasFullPageRepresentation: Boolean
        get() = false
}

class FavoriteTypeItem(val type: FavoriteType) : FavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() {
            return when (type) {
                FavoriteType.Script -> {
                    currentScripts.map { FavoriteScriptItem(it) }
                }
                FavoriteType.Destination -> {
                    currentDestinations.map { FavoriteDestinationItem(it) }
                }
            }
        }
    override val title: String
    get() = when (type) {
        FavoriteType.Script -> {
            CelestiaString("Scripts", "")
        }
        FavoriteType.Destination -> {
            CelestiaString("Destinations", "")
        }
    }
    override val isLeaf: Boolean
        get() = false
    override val hasFullPageRepresentation: Boolean
        get() = false
}

class FavoriteScriptItem(val script: Script) : FavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() = listOf()
    override val title: String
        get() = script.title
    override val isLeaf: Boolean
        get() = true
    override val hasFullPageRepresentation: Boolean
        get() = false
}

class FavoriteDestinationItem(val destination: Destination): FavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() = listOf()
    override val title: String
        get() = destination.name
    override val isLeaf: Boolean
        get() = true
    override val hasFullPageRepresentation: Boolean
        get() = true
}

open class FavoriteBookmarkItem(val bookmark: BookmarkNode) : MutableFavoriteBaseItem {
    override val children: ArrayList<FavoriteBaseItem> by lazy { if (bookmark.isLeaf) arrayListOf() else ArrayList(bookmark.children!!.map { FavoriteBookmarkItem(it) }) }
    override val title: String
        get() = bookmark.name
    override val isLeaf: Boolean
        get() = bookmark.isLeaf
    override val supportedItemActions: List<FavoriteItemAction>
        get() = listOf(FavoriteItemAction.Delete, FavoriteItemAction.Rename, FavoriteItemAction.Share)
    override val hasFullPageRepresentation: Boolean
        get() = false

    override fun insert(newItem: FavoriteBaseItem, index: Int) {
        if (newItem !is FavoriteBookmarkItem)
            throw RuntimeException("$newItem does not match type FavoriteBookmarkItem")
        bookmark.children!!.add(index, newItem.bookmark)
        children.add(index, newItem)
    }

    override fun remove(index: Int) {
        bookmark.children!!.removeAt(index)
        children.removeAt(index)
    }

    override fun rename(newName: String) {
        bookmark.name = newName
    }

    override fun move(fromIndex: Int, toIndex: Int) {
        bookmark.children!!.add(toIndex, bookmark.children!!.removeAt(fromIndex))
        children.add(toIndex, children.removeAt(fromIndex))
    }
}

class FavoriteBookmarkRootItem(bookmark: BookmarkNode) : FavoriteBookmarkItem(bookmark) {
    // Root item does not support any customization
    override val supportedItemActions: List<FavoriteItemAction>
        get() = listOf()
}

fun updateCurrentScripts(scripts: List<Script>) {
    currentScripts = scripts
}

fun getCurrentBookmarks(): List<BookmarkNode> {
    return currentBookmarkRoot.children ?: return listOf()
}

fun updateCurrentBookmarks(nodes: List<BookmarkNode>) {
    currentBookmarkRoot.children = ArrayList(nodes)
}

fun updateCurrentDestinations(destinations: List<Destination>) {
    currentDestinations = destinations
}

private var currentScripts: List<Script> = listOf()
private var currentBookmarkRoot: BookmarkNode = BookmarkNode(CelestiaString("Bookmarks", "") , "", arrayListOf())
private var currentDestinations: List<Destination> = listOf()