package io.frappe.android.Utils

import org.json.JSONException
import org.json.JSONObject

class DocField(fieldMeta:JSONObject, value:Any? = null) {

    var reqd: Int? = null
    var read_only: Int? = null
    var length: Int? = 0
    var label: String? = String()
    var hidden: Int = 0
    var fieldname: String? = null
    var fieldtype: String? = null
    var options: String? = null
    var allowedFieldTypes = arrayListOf<String>()
    var allFieldTypes = arrayListOf<String>()
    var value = value

    init {
        allFieldTypes = ArrayList<String>().apply {
            add("Attach")
            add("Attach Image")
            add("Barcode")
            add("Button")
            add("Check")
            add("Code")
            add("Color")
            add("Column Break")
            add("Currency")
            add("Data")
            add("Date")
            add("Datetime")
            add("Dynamic Link")
            add("Float")
            add("Fold")
            add("Geolocation")
            add("Heading")
            add("HTML")
            add("Image")
            add("Int")
            add("Link")
            add("Long Text")
            add("Password")
            add("Percent")
            add("Read Only")
            add("Section Break")
            add("Select")
            add("Small Text")
            add("Table")
            add("Text")
            add("Text Editor")
            add("Time")
            add("Signature")
        }

        allowedFieldTypes = ArrayList<String>().apply {
            add("Attach")
            add("Attach Image")
            add("Barcode")
            add("Check")
            add("Code")
            add("Color")
            add("Currency")
            add("Data")
            add("Date")
            add("Datetime")
            add("Float")
            add("Image")
            add("Int")
            add("Link")
            add("Long Text")
            add("Percent")
            add("Select")
            add("Small Text")
            add("Text")
            add("Time")
        }

        fieldtype = fieldMeta.getString("fieldtype")
        if (allowedFieldTypes.contains(fieldtype!!)){
            reqd = fieldMeta.getInt("reqd")
            read_only = fieldMeta.getInt("read_only")
            length = fieldMeta.getInt("length")
            hidden = fieldMeta.getInt("hidden")
            fieldname = fieldMeta.getString("fieldname")
            try {options = fieldMeta.getString("options") }
            catch (e: JSONException) { options = null }

            try {label = fieldMeta.getString("label") }
            catch (e: JSONException) { label = null }
        }
    }

}