package io.frappe.android.UI

import android.annotation.TargetApi
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import io.frappe.android.R
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView

/**
 * Created by revant on 28/12/17.
 */
class ListItemUI : AnkoComponent<ViewGroup> {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun createView(ui: AnkoContext<ViewGroup>): View {
        // UI list item
        val out = with(ui){
            linearLayout {
                lparams(width = matchParent, height = dip(100))
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                cardView {
                    verticalLayout {
                        padding = dip(10)
                        textView {
                            gravity = Gravity.LEFT
                            id = Ids.itemType
                            text = "Name"
                        }.lparams(width = matchParent, height = wrapContent)

                        textView {
                            gravity = Gravity.LEFT
                            id = Ids.listItem
                            textAppearance = R.style.Base_TextAppearance_AppCompat_Body1
                        }.lparams(width = matchParent, height = wrapContent)
                    }.lparams(width = matchParent, height = matchParent)
                }.lparams(height = dip(90), width = matchParent)
            }
        }
        return out
    }

    companion object {
        object Ids {
            val itemType = 0
            val listItem = 1
        }
    }
}
