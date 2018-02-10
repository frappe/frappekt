package io.frappe.android.UI

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import io.frappe.android.R
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView
import java.util.ArrayList

class FiltersItemUI: AnkoComponent<ViewGroup> {

    override fun createView(ui: AnkoContext<ViewGroup>): View {
        // UI list item
        val out = with(ui){
            linearLayout {
                lparams(width = matchParent, height = dip(200))
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                cardView {
                    verticalLayout {
                        spinner {
                            id = Ids.docFieldSpinner
                        }

                        spinner {
                            var list = ArrayList<String>().apply {
                                add("Equal")
                                add("Like")
                                add("In")
                                add("Not In")
                                add("Not Equal")
                                add("Not Like")
                                add("Between")
                                add(">")
                                add("<")
                                add(">=")
                                add("<=")
                            }
                            val spinnerAdapter = ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, list)
                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            adapter = spinnerAdapter
                            id = Ids.expressionSpinner
                        }

                        editText {
                            id = Ids.filterValue
                        }

                        linearLayout {
                            orientation = LinearLayout.HORIZONTAL
                            weightSum = 10F
                            space {

                            }.lparams(
                                weight = 9.5F
                            )

                            button {
                                id = Ids.removeFilter
                                backgroundResource = android.R.color.transparent
                            }.lparams(weight = 0.5F, width = matchParent, height = wrapContent)
                                    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_close, 0, 0, 0)
                        }
                    }.lparams(width = matchParent, height = matchParent)
                }.lparams(height = dip(190), width = matchParent)
            }
        }
        return out
    }

    companion object {
        object Ids {
            val fieldName = 0
            val listItem = 1
            val docFieldSpinner = 2
            val saveFilter = 3
            val removeFilter = 4
            val expressionSpinner = 5
            val filterValue = 6
        }
    }
}