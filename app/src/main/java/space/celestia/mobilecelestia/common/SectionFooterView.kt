package space.celestia.mobilecelestia.common

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import space.celestia.mobilecelestia.R

class SectionFooterView(context: Context): FrameLayout(context) {
    val textView: TextView = TextView(context)

    init {
        val density = resources.displayMetrics.density

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val paddingH = (footerHorizontalPadding * density).toInt()
        val paddingV = (footerVerticalPadding * density).toInt()
        setPadding(paddingH, paddingV, paddingH, paddingV)

        val view = View(context)
        view.setBackgroundColor(resources.getColor(R.color.colorSeparator))

        textView.setTextColor(resources.getColor(R.color.colorSecondaryLabel))
        textView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(textView)
    }

    private companion object {
        const val footerHorizontalPadding: Float = 16F
        const val footerVerticalPadding: Float = 8F
    }
}