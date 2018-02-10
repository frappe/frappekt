package io.frappe.android.UI

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.json.JSONArray
import org.json.JSONObject
import android.widget.Spinner
import io.frappe.android.R
import io.frappe.android.Utils.FieldUtils
import kotlin.collections.ArrayList

class FilterViewAdapter(filterList:JSONArray): RecyclerView.Adapter<FilterViewAdapter.ViewHolder>() {
    var immutableFilterlist = JSONArray()
    var filterList = JSONArray()
    var docMeta = JSONObject()
    init {
        immutableFilterlist = filterList
        this.filterList = filterList
    }

    constructor(filterList: JSONArray, docMeta:JSONObject) : this(filterList) {
        immutableFilterlist = filterList
        this.filterList = filterList
        this.docMeta = docMeta
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val context = itemView.context
        // Get list_item (and other fields) from ListItemUI
        val spinnerField: Spinner = itemView.find<Spinner>(FiltersItemUI.Companion.Ids.docFieldSpinner)
        // Bind values to name and other fields above
        val spinnerExpression: Spinner = itemView.find(FiltersItemUI.Companion.Ids.expressionSpinner)

        val fieldsValue: EditText = itemView.find(FiltersItemUI.Companion.Ids.filterValue)

        val bRemoveFilter = itemView.find<Button>(FiltersItemUI.Companion.Ids.removeFilter)

        fun bind(filtersArray: JSONArray?, fieldsArray: JSONArray) {
            var list = ArrayList<String>().apply {
                add(context.resources.getString(R.string.sort_name))
                add(context.resources.getString(R.string.last_modified_on))
                add(context.resources.getString(R.string.created_on))
                add(context.resources.getString(R.string.most_used))
                add(context.resources.getString(R.string.created_by))
                add(context.resources.getString(R.string.modified_by))
            }

            val listFields = ArrayList<String>().apply {
                add("name")
                add("modified")
                add("creation")
                add("idx")
                add("owner")
                add("modified_by")
            }

            var fieldName = ""
            var expression = ""
            var value = ""

            for(i in 0 until fieldsArray?.length()!! - 1){
                list.add(fieldsArray.getJSONArray(i).getString(1))
                for (j in 0 until filtersArray?.length()!! - 1){
                    if (fieldsArray.getJSONArray(i).getString(0) == filtersArray.getString(0)) {
                        fieldName = fieldsArray.getJSONArray(i).getString(1)
                    } else if (listFields.contains(filtersArray.getString(0))) {
                        when(filtersArray.getString(0)){
                            "name" -> fieldName = context.resources.getString(R.string.sort_name)
                            "modified" -> fieldName = context.resources.getString(R.string.last_modified_on)
                            "creation" -> fieldName = context.resources.getString(R.string.created_on)
                            "idx" -> fieldName = context.resources.getString(R.string.most_used)
                            "owner" -> fieldName = context.resources.getString(R.string.created_by)
                            "modified_by" -> fieldName = context.resources.getString(R.string.modified_by)
                        }
                    }
                    expression = filtersArray.getString(1)
                    value = filtersArray.getString(2)
                }
            }

            val spinnerAdapter = ArrayAdapter<String>(itemView.context, android.R.layout.simple_list_item_1, list)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerField.adapter = spinnerAdapter

            // Set Filter FieldName
            for (i in 0 until spinnerField.count) {
                if (spinnerField.getItemAtPosition(i).equals(fieldName)) {
                    spinnerField.setSelection(i)
                    break
                }
            }

            // Set Filter Operator / Expression
            for (i in 0 until spinnerExpression.count) {
                if (spinnerExpression.getItemAtPosition(i).equals(FieldUtils().getLabelFromExpression(context, expression))) {
                    spinnerExpression.setSelection(i)
                    break
                }
            }

            // Set Filter value / Expression
            fieldsValue.setText(value)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(FiltersItemUI().createView(AnkoContext.create(parent!!.context, parent)))
    }

    override fun getItemCount(): Int {
        // return listing size
        return filterList.length()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var p = position
        while(filterList.length() < p){
            p -= filterList.length()
        }

        val fieldsArray = JSONArray()
        val fields = docMeta?.getJSONArray("fields")!!

        for (i in 0 until fields.length() - 1){
            if(fields.getJSONObject(i).has("label")){
                fieldsArray.put(
                        JSONArray()
                                .put(fields.getJSONObject(i).getString("fieldname"))
                                .put(fields.getJSONObject(i).getString("label"))
                )
            }
        }
        val filtersArray = filterList.getJSONArray(p)

        holder!!.bRemoveFilter.onClick {
            filterList.remove(p)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position,filterList.length())
        }

        holder.spinnerField.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                val fieldname = FieldUtils().getFieldnameFromLabel(holder.context, docMeta, holder.spinnerField.selectedItem.toString())
                filterList.getJSONArray(p).put(0,fieldname)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Nothing selected
            }

        }
        holder.spinnerExpression.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                val expression = FieldUtils().getExpressionFromLabel(holder.spinnerExpression.selectedItem.toString())
                filterList.getJSONArray(p).put(1,expression)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Nothing selected
            }

        }
        holder.fieldsValue.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterList.getJSONArray(p).put(2,s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        holder!!.bind(filtersArray, fieldsArray)
    }
}