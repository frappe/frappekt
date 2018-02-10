package io.frappe.android.UI

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.frappe.android.Utils.DocField
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find
import org.json.JSONObject
import kotlin.collections.ArrayList

class FormViewAdapter(metaList: ArrayList<DocField>) : RecyclerView.Adapter<FormViewAdapter.ViewHolder>() {
    var metaList = metaList
    var docMeta = JSONObject()
    var position: Int = -1 // help pass position for which view is being pushed into adapter

    companion object {
        var holderArray = ArrayList<FormViewAdapter.ViewHolder>() // hold each row's binder
    }

    constructor(metaList: ArrayList<DocField>, docMeta: JSONObject) : this(metaList) {
        this.metaList = metaList
        this.docMeta = docMeta
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val label: TextView = itemView.find(FormGeneraterUI.Companion.Ids.fieldName)
        var value: View = itemView.find<View>(FormGeneraterUI.Companion.Ids.fieldValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FormViewAdapter.ViewHolder {
        return FormViewAdapter.ViewHolder(FormGeneraterUI(metaList[getCurrentPosition()]).createView(AnkoContext.create(parent!!.context, parent)))
    }

    override fun getItemCount(): Int {
        // return listing size
        return metaList.size
    }

    fun getCurrentPosition(): Int {
        return this.position + 1
    }

    override fun onBindViewHolder(holder: FormViewAdapter.ViewHolder, position: Int) {
        var p = position
        while(metaList.size < p){
            p -= metaList.size
        }

        this.position = p
        holder.label.text = metaList[p].label + " : "
        holderArray!!.add(holder) // populate controller for each row generated
    }
}