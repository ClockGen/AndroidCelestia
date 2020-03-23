package space.celestia.MobileCelestia.Browser

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import space.celestia.MobileCelestia.Common.*
import space.celestia.MobileCelestia.Core.CelestiaAppCore
import space.celestia.MobileCelestia.Core.CelestiaBrowserItem

import space.celestia.MobileCelestia.R

class BrowserFragment : Fragment(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val browserItemMenu by lazy {
        val sim = CelestiaAppCore.shared().simulation
        listOf(
            BrowserItemMenu(sim.universe.solBrowserRoot(), R.drawable.browser_tab_sso),
            BrowserItemMenu(sim.starBrowserRoot(), R.drawable.browser_tab_star),
            BrowserItemMenu(sim.universe.dsoBrowserRoot(), R.drawable.browser_tab_dso)
        )
    }

    private val toolbar by lazy { view!!.findViewById<Toolbar>(R.id.toolbar) }
    private var currentPath = ""

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null) {
            val p = savedInstanceState.getString("path")
            if (p != null) {
                currentPath = p
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("path", currentPath)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_browser, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val nav = view.findViewById<BottomNavigationView>(R.id.navigation)
        for (i in 0 until browserItemMenu.count()) {
            val item = browserItemMenu[i]
            nav.menu.add(Menu.NONE, i, Menu.NONE, item.item.name).setIcon(item.icon)
        }
        toolbar.setNavigationOnClickListener {
            popItem()
        }
        nav.setOnNavigationItemSelectedListener(this)
        replaceItem(browserItemMenu[0].item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        replaceItem(browserItemMenu[item.itemId].item)
        return true
    }

    private fun replaceItem(browserItem: CelestiaBrowserItem) {
        toolbar.navigationIcon = null
        toolbar.title = browserItem.name
        currentPath = browserItem.name
        browserMap[currentPath] = browserItem
        replace(BrowserCommonFragment.newInstance(currentPath), R.id.browser_container)
    }

    public fun pushItem(browserItem: CelestiaBrowserItem) {
        toolbar.title = browserItem.name
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_action_arrow_back)
        currentPath = "$currentPath/${browserItem.name}"
        browserMap[currentPath] = browserItem
        val frag = BrowserCommonFragment.newInstance(currentPath)
        push(frag, R.id.browser_container)
    }

    fun popItem() {
        pop()
        val index = childFragmentManager.backStackEntryCount - 1
        if (index == 0) {
            // no more return
            toolbar.navigationIcon = null
        }
        toolbar.title = (childFragmentManager.fragments[index] as TitledFragment).title
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            BrowserFragment()

        val browserMap = HashMap<String, CelestiaBrowserItem>()
    }
}
