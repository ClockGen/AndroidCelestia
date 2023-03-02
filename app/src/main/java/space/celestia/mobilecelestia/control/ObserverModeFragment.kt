package space.celestia.mobilecelestia.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.celestia.Observer
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.utils.CelestiaString
import javax.inject.Inject

@AndroidEntryPoint
class ObserverModeFragment: NavigationFragment.SubFragment() {
    @Inject
    lateinit var appCore: AppCore

    @Inject
    lateinit var executor: CelestiaExecutor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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

    @Composable
    fun MainScreen() {
        val coordinateSystems = listOf(
            Pair(Observer.COORDINATE_SYSTEM_UNIVERSAL, CelestiaString("Universal", "")),
            Pair(Observer.COORDINATE_SYSTEM_ECLIPTICAL, CelestiaString("Ecliptical", "")),
            Pair(Observer.COORDINATE_SYSTEM_EQUATORIAL, CelestiaString("Equatorial", "")),
            Pair(Observer.COORDINATE_SYSTEM_BODY_FIXED, CelestiaString("Body Fixed", "")),
            Pair(Observer.COORDINATE_SYSTEM_PHASE_LOCK, CelestiaString("Phase Lock", "")),
            Pair(Observer.COORDINATE_SYSTEM_CHASE, CelestiaString("Chase", "")),
        )
        var referenceObjectName by remember {
            mutableStateOf("")
        }
        var targetObjectName by remember {
            mutableStateOf("")
        }
        var selectedCoordinate by remember {
            mutableStateOf(0)
        }
        val textViewModifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = space.celestia.mobilecelestia.R.dimen.list_item_medium_margin_horizontal),
                vertical = dimensionResource(id = space.celestia.mobilecelestia.R.dimen.list_item_small_margin_vertical),
            )
        Column(modifier = Modifier.verticalScroll(state = rememberScrollState(), enabled = true).systemBarsPadding()) {
            Header(text = CelestiaString("Coordinate System", ""))
            OptionSelect(options = coordinateSystems.map { it.second }, selectedIndex = selectedCoordinate, modifier = textViewModifier, selectionChange = {
                selectedCoordinate = it
            })
            if (coordinateSystems[selectedCoordinate].first != Observer.COORDINATE_SYSTEM_UNIVERSAL) {
                Header(text = CelestiaString("Reference Object Name", ""))
                ObjectNameAutoComplete(appCore = appCore, name = referenceObjectName, modifier = textViewModifier) {
                    referenceObjectName = it
                }
            }
            if (coordinateSystems[selectedCoordinate].first == Observer.COORDINATE_SYSTEM_PHASE_LOCK || coordinateSystems[selectedCoordinate].first == Observer.COORDINATE_SYSTEM_CHASE) {
                Header(text = CelestiaString("Target Object Name", ""))
                ObjectNameAutoComplete(appCore = appCore, name = targetObjectName, modifier = textViewModifier) {
                    targetObjectName = it
                }
            }
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth().padding(
                    start = dimensionResource(id = space.celestia.mobilecelestia.R.dimen.common_page_medium_margin_horizontal),
                    top = dimensionResource(id = space.celestia.mobilecelestia.R.dimen.common_page_medium_gap_vertical),
                    end = dimensionResource(id = space.celestia.mobilecelestia.R.dimen.common_page_medium_margin_horizontal),
                    bottom = dimensionResource(id = space.celestia.mobilecelestia.R.dimen.common_page_medium_margin_vertical)
                ),
                onClick = {
                    applyObserverMode(referenceObjectName, targetObjectName, coordinateSystems[selectedCoordinate].first)
                }
            ) {
                Text(text = CelestiaString("OK", ""))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Observer Mode", "")
    }

    private fun applyObserverMode(referenceObjectName: String, targetObjectName: String, coordinateSystem: Int) {
        val refName = referenceObjectName
        val targetName = targetObjectName
        val system = coordinateSystem
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            val ref = if (refName.isEmpty()) Selection() else appCore.simulation.findObject(refName)
            val target = if (targetName.isEmpty()) Selection() else appCore.simulation.findObject(targetName)
            appCore.simulation.activeObserver.setFrame(system, ref, target)
        }
    }

    companion object {
        fun newInstance(): ObserverModeFragment {
            return ObserverModeFragment()
        }
    }
}