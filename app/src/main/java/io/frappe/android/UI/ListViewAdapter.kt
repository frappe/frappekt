package io.frappe.android.UI

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find
import org.json.JSONArray
import org.json.JSONObject

class ListViewAdapter(var doc_list:JSONArray): RecyclerView.Adapter<ListViewAdapter.ViewHolder>(), Filterable {
    fun setLoadDataCallback() {

    }

    private var mCLickListener: View.OnClickListener? = null
    fun setOnClickListener(listener: View.OnClickListener) {
        mCLickListener = listener
    }

    val immutable_doc_list = doc_list
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                doc_list = results?.values as JSONArray
                Log.d("results", doc_list.toString())
                notifyDataSetChanged()
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                Log.d("constraint", constraint.toString())
                val charString = constraint.toString();
                if (charString.isNullOrEmpty()) {
                    filterResults.values = immutable_doc_list
                } else {
                    var filteredList = JSONArray()
                    for (i in 0..doc_list.length() - 1) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if(doc_list.getJSONObject(i).getString("name").toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.put(doc_list.getJSONObject(i))
                        }
                    }
                    filterResults.values = filteredList
                }

                return filterResults
            }
        }
    }

    fun clear() {
        val size = this.doc_list.length()
        if (size > 0) {
            for (i in 0 until size) {
                this.doc_list.remove(0)
            }
            this.notifyItemRangeRemoved(0, size)

        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Get list_item (and other fields) from ListItemUI
        val name: TextView = itemView.find(ListItemUI.Companion.Ids.listItem)
        // Bind values to name and other fields above
        fun bind(jsonObject: JSONObject?) {
            name.text = jsonObject?.getString("name")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(ListItemUI().createView(AnkoContext.create(parent!!.context, parent)))
    }

    override fun getItemCount(): Int {
        // return listing size
        return doc_list.length()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var p = position
        while(doc_list.length() < p){
            p -= doc_list.length()
        }
        val jsonObject = doc_list.getJSONObject(p)
        holder!!.bind(jsonObject)

        holder.itemView.setOnClickListener(mCLickListener)
    }
}