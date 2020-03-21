package space.celestia.MobileCelestia.Settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import space.celestia.MobileCelestia.Common.TitledFragment

import space.celestia.MobileCelestia.R

class SimpleTextFragment : TitledFragment() {

    override val title: String
        get() = textTitle!!

    private var textTitle: String? = null
    private var textDetail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            textTitle = it.getString(ARG_TITLE, null)
            textDetail = it.getString(ARG_DETAIL, null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_simple_text, container, false)
        view.findViewById<TextView>(R.id.text).text = textDetail
        return view
    }

    companion object {

        const val ARG_TITLE = "title"
        const val ARG_DETAIL = "detail"

        @JvmStatic
        fun newInstance(title: String, detail: String) =
            SimpleTextFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_DETAIL, detail)
                }
            }
    }
}
