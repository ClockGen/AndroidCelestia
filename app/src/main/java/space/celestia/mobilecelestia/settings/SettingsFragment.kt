/*
 * SettingsFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import RenderInfoScreen
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.purchase.FontSettingsScreen
import space.celestia.mobilecelestia.purchase.SubscriptionBackingScreen
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

class SettingsFragment : Fragment() {
    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen()
                }
            }
        }
    }

    private fun getCurrentTopAppBarTitle(controller: NavController, sections: List<CommonSectionV2>): String {
        val backStackEntry = controller.currentBackStackEntry ?: return ""
        return when (backStackEntry.destination.route) {
            ROUTE_SETTINGS_LIST -> CelestiaString("Settings", "")
            ROUTE_SETTINGS_LANGUAGE -> CelestiaString("Language", "")
            ROUTE_SETTINGS_REFRESH_RATE -> CelestiaString("Frame Rate", "")
            ROUTE_SETTINGS_ABOUT -> CelestiaString("About", "")
            ROUTE_SETTINGS_CURRENT_TIME -> CelestiaString("Current Time", "")
            ROUTE_SETTINGS_DATA_LOCATION -> CelestiaString("Data Location", "")
            ROUTE_SETTINGS_COMMON -> {
                val route = backStackEntry.arguments?.getString(
                    ROUTE_SETTINGS_COMMON_ARGUMENT_ITEM_KEY, "") ?: return ""
                return findItem(route, sections)?.name ?: ""
            }
            ROUTE_SETTINGS_RENDER_INFO -> CelestiaString("Render Info", "")
            ROUTE_SETTINGS_FONT -> CelestiaString("Font", "")
            else -> ""
        }
    }

    private fun navigateToItem(item: SettingsItem, navController: NavController) {
        when (item) {
            is SettingsLanguageItem -> {
                navController.navigate(ROUTE_SETTINGS_LANGUAGE)
            }
            is SettingsRefreshRateItem -> {
                navController.navigate(ROUTE_SETTINGS_REFRESH_RATE)
            }
            is SettingsAboutItem -> {
                navController.navigate(ROUTE_SETTINGS_ABOUT)
            }
            is SettingsCurrentTimeItem -> {
                navController.navigate(ROUTE_SETTINGS_CURRENT_TIME)
            }
            is SettingsDataLocationItem -> {
                navController.navigate(ROUTE_SETTINGS_DATA_LOCATION)
            }
            is SettingsCommonItem -> {
                navController.navigate("settings/common/${item.route}")
            }
            is SettingsRenderInfoItem -> {
                navController.navigate(ROUTE_SETTINGS_RENDER_INFO)
            }
            is SettingsFontItem -> {
                navController.navigate(ROUTE_SETTINGS_FONT)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        val viewModel: SettingsViewModel = hiltViewModel()
        val sections = if (viewModel.purchaseManager.canUseInAppPurchase()) mainSettingSectionsBeforePlus + celestiaPlusSettingSection + mainSettingSectionsAfterPlus else mainSettingSectionsBeforePlus + mainSettingSectionsAfterPlus
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val navController = rememberNavController()
        var title by remember {
            mutableStateOf("")
        }
        var canPop by remember { mutableStateOf(false) }
        DisposableEffect(navController) {
            val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
                canPop = controller.previousBackStackEntry != null
                title = getCurrentTopAppBarTitle(controller, sections)
            }
            navController.addOnDestinationChangedListener(listener)
            onDispose {
                navController.removeOnDestinationChangedListener(listener)
            }
        }

        Scaffold(topBar = {
            TopAppBar(title = {
                Text(text = title)
            }, navigationIcon = {
                if (canPop) {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "")
                    }
                }
            }, scrollBehavior = scrollBehavior)
        }) { paddingValues ->
            NavHost(navController = navController, startDestination = ROUTE_SETTINGS_LIST) {
                composable(ROUTE_SETTINGS_LIST) {
                    SettingsListScreen(paddingValues = paddingValues, itemHandler = {
                        navigateToItem(it, navController)
                    }, sections = sections,  modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable(ROUTE_SETTINGS_LANGUAGE) {
                    LanguageSettingsScreen(paddingValues = paddingValues, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable(ROUTE_SETTINGS_REFRESH_RATE) {
                    RefreshRateSettingsScreen(paddingValues = paddingValues, changeHandler = {
                        listener?.onRefreshRateChanged(it)
                    }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable(ROUTE_SETTINGS_ABOUT) {
                    AboutScreen(paddingValues = paddingValues, urlHandler = {
                        listener?.onAboutURLSelected(it)
                    }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable(ROUTE_SETTINGS_CURRENT_TIME) {
                    TimeSettingsScreen(paddingValues = paddingValues, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable(ROUTE_SETTINGS_DATA_LOCATION) {
                    DataLocationSettingsScreen(paddingValues = paddingValues, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable(ROUTE_SETTINGS_COMMON, arguments = listOf(navArgument(
                    ROUTE_SETTINGS_COMMON_ARGUMENT_ITEM_KEY) { type = NavType.StringType })) { backStackEntry ->
                    val itemRoute = backStackEntry.arguments?.getString(ROUTE_SETTINGS_COMMON_ARGUMENT_ITEM_KEY, "") ?: return@composable
                    val item = findItem(itemRoute, sections) ?: return@composable
                    CommonSettingsScreen(paddingValues = paddingValues, item = item, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable(ROUTE_SETTINGS_RENDER_INFO) {
                    RenderInfoScreen(paddingValues = paddingValues, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    composable(ROUTE_SETTINGS_FONT) {
                        SubscriptionBackingScreen(paddingValues = paddingValues, content = {
                            FontSettingsScreen(paddingValues = paddingValues, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                        }) {
                            listener?.requestOpenSubscriptionManagement()
                        }
                    }
                }
            }
        }
    }

    private fun findItem(route: String, sections: List<CommonSectionV2>): SettingsCommonItem? {
        for (section in sections) {
            for (item in section.items) {
                if (item is SettingsCommonItem && item.route == route) {
                    return item
                }
            }
        }
        return null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SettingsFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onRefreshRateChanged(frameRateOption: Int)
        fun onAboutURLSelected(url: String)
        fun requestOpenSubscriptionManagement()
    }

    companion object {
        fun newInstance() = SettingsFragment()

        private const val ROUTE_SETTINGS_LIST = "settings/home"
        private const val ROUTE_SETTINGS_LANGUAGE = "settings/language"
        private const val ROUTE_SETTINGS_REFRESH_RATE = "settings/refresh_rate"
        private const val ROUTE_SETTINGS_ABOUT = "settings/about"
        private const val ROUTE_SETTINGS_CURRENT_TIME = "settings/current_time"
        private const val ROUTE_SETTINGS_DATA_LOCATION = "settings/data_location"
        private const val ROUTE_SETTINGS_COMMON_ARGUMENT_ITEM_KEY = "item"
        private const val ROUTE_SETTINGS_COMMON = "settings/common/{item}"
        private const val ROUTE_SETTINGS_RENDER_INFO = "settings/render_info"
        private const val ROUTE_SETTINGS_FONT = "settings/font"
    }
}
