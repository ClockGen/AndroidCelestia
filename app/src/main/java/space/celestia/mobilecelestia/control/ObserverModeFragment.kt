package space.celestia.mobilecelestia.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.celestia.Observer
import space.celestia.celestia.Renderer
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.travel.GoToInputFragment
import space.celestia.mobilecelestia.travel.GoToSuggestionAdapter
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.getSerializableValue
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class ObserverModeFragment: NavigationFragment.SubFragment() {
    private lateinit var coordinateSystemEditText: AutoCompleteTextView
    private lateinit var referenceObjectNameEditText: AutoCompleteTextView
    private lateinit var targetObjectNameEditText: AutoCompleteTextView
    private lateinit var referenceObjectNameContainer: View
    private lateinit var targetObjectNameContainer: View

    private var referenceObjectName = ""
    private var targetObjectName = ""
    private var coordinateSystem = Observer.COORDINATE_SYSTEM_UNIVERSAL

    @Inject
    lateinit var appCore: AppCore

    @Inject
    lateinit var executor: CelestiaExecutor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            referenceObjectName = savedInstanceState.getString(ARG_REFERENCE_OBJECT, referenceObjectName)
            targetObjectName = savedInstanceState.getString(ARG_TARGET_OBJECT, targetObjectName)
            coordinateSystem = savedInstanceState.getInt(ARG_COORDINATE_SYSTEM, coordinateSystem)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_observer_mode, container, false)

        val coordinateSystemHeaderContainer = view.findViewById<View>(R.id.coordinate_system_section_header)
        coordinateSystemHeaderContainer.findViewById<TextView>(R.id.text).text = CelestiaString("Coordinate System", "")

        referenceObjectNameContainer = view.findViewById(R.id.reference_object_container)
        targetObjectNameContainer = view.findViewById(R.id.target_object_container)

        referenceObjectNameEditText = view.findViewById(R.id.reference_object_text_view)
        referenceObjectNameEditText.threshold = 1
        referenceObjectNameEditText.setAdapter(GoToSuggestionAdapter(requireActivity(), android.R.layout.simple_dropdown_item_1line, appCore))
        referenceObjectNameEditText.hint = CelestiaString("Reference Object Name", "")

        targetObjectNameEditText = view.findViewById(R.id.target_object_text_view)
        targetObjectNameEditText.threshold = 1
        targetObjectNameEditText.setAdapter(GoToSuggestionAdapter(requireActivity(), android.R.layout.simple_dropdown_item_1line, appCore))
        targetObjectNameEditText.hint = CelestiaString("Target Object Name", "")

        coordinateSystemEditText = view.findViewById(R.id.coordinate_system_text_view)
        val coordinateSystemAdapter = object: ArrayAdapter<String>(requireActivity(), android.R.layout.simple_dropdown_item_1line) {
            override fun getCount(): Int {
                return coordinateSystems.size
            }

            override fun getItem(position: Int): String {
                return coordinateSystems[position].second
            }
        }
        coordinateSystemEditText.setAdapter(coordinateSystemAdapter)

        val okButton = view.findViewById<Button>(R.id.ok_button)
        okButton.text = CelestiaString("OK", "")

        val weakSelf = WeakReference(this)
        okButton.setOnClickListener {
            val self = weakSelf.get() ?: return@setOnClickListener
            self.applyObserverMode()
        }

        referenceObjectNameEditText.addTextChangedListener { newText ->
            val self = weakSelf.get() ?: return@addTextChangedListener
            val result = newText?.toString() ?: return@addTextChangedListener
            self.referenceObjectName = result
        }

        targetObjectNameEditText.addTextChangedListener { newText ->
            val self = weakSelf.get() ?: return@addTextChangedListener
            val result = newText?.toString() ?: return@addTextChangedListener
            self.targetObjectName = result
        }

        coordinateSystemEditText.setOnItemClickListener { _, _, position, _ ->
            val self = weakSelf.get() ?: return@setOnItemClickListener
            self.coordinateSystem = coordinateSystems[position].first
            self.reloadVisibility()
        }
        reload()
        return view
    }

    private fun reload() {
        referenceObjectNameEditText.setText(referenceObjectName)
        targetObjectNameEditText.setText(targetObjectName)
        coordinateSystemEditText.setText(coordinateSystems.firstOrNull { it.first == coordinateSystem }?.second ?: coordinateSystems[0].second)
        reloadVisibility()
    }

    private fun reloadVisibility() {
        referenceObjectNameContainer.visibility = if (coordinateSystem == Observer.COORDINATE_SYSTEM_UNIVERSAL) View.GONE else View.VISIBLE
        targetObjectNameContainer.visibility = if (coordinateSystem == Observer.COORDINATE_SYSTEM_PHASE_LOCK || coordinateSystem == Observer.COORDINATE_SYSTEM_CHASE) View.VISIBLE else View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Observer Mode", "")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ARG_COORDINATE_SYSTEM, coordinateSystem)
        outState.putString(ARG_REFERENCE_OBJECT, referenceObjectName)
        outState.putString(ARG_TARGET_OBJECT, targetObjectName)
        super.onSaveInstanceState(outState)
    }

    private fun applyObserverMode() {
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
        private const val ARG_COORDINATE_SYSTEM = "coordinate_system"
        private const val ARG_REFERENCE_OBJECT = "reference_object"
        private const val ARG_TARGET_OBJECT = "target_object"

        private val coordinateSystems = listOf(
            Pair(Observer.COORDINATE_SYSTEM_UNIVERSAL, CelestiaString("Universal", "")),
            Pair(Observer.COORDINATE_SYSTEM_ECLIPTICAL, CelestiaString("Ecliptical", "")),
            Pair(Observer.COORDINATE_SYSTEM_EQUATORIAL, CelestiaString("Equatorial", "")),
            Pair(Observer.COORDINATE_SYSTEM_BODY_FIXED, CelestiaString("Body Fixed", "")),
            Pair(Observer.COORDINATE_SYSTEM_PHASE_LOCK, CelestiaString("Phase Lock", "")),
            Pair(Observer.COORDINATE_SYSTEM_CHASE, CelestiaString("Chase", "")),
        )

        fun newInstance(): ObserverModeFragment {
            return ObserverModeFragment()
        }
    }
}