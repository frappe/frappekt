package io.frappe.android.Utils

import android.content.Context
import io.frappe.android.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class FieldUtils {
    fun getFieldnameFromLabel(context:Context, doctypeMetaJson:JSONObject, label: String) : String {

        var fields = JSONArray()
        try {
            fields = doctypeMetaJson.getJSONArray("fields")
        } catch (e: JSONException) {
            fields = JSONArray()
        }

        var spinnerField = ""

        for (i in 0 until fields.length() - 1){
            if(fields.getJSONObject(i).has("label") && fields.getJSONObject(i).getString("label") == label){
                spinnerField = fields.getJSONObject(i).getString("fieldname")
                break
            }
        }

        if (spinnerField.isNullOrEmpty()){
            when(label){
                context.resources.getString(R.string.sort_name) -> spinnerField = "name"
                context.resources.getString(R.string.last_modified_on) -> spinnerField = "modified"
                context.resources.getString(R.string.created_on) -> spinnerField = "creation"
                context.resources.getString(R.string.most_used) -> spinnerField = "idx"
                context.resources.getString(R.string.created_by) -> spinnerField = "owner"
                context.resources.getString(R.string.modified_by) -> spinnerField = "modified_by"
            }
        }

        return spinnerField
    }
    fun getExpressionFromLabel(label: String) : String {
        var expressionValue = ""
        when(label){
            "Equal" -> expressionValue = "="
            "Like" -> expressionValue = "like"
            "In" -> expressionValue = "in"
            "Not In" -> expressionValue = "not in"
            "Not Equal" -> expressionValue = "!="
            "Not Like" -> expressionValue = "not like"
            "Between" -> expressionValue = "between"
            ">" -> expressionValue = ">"
            "<" -> expressionValue = "<"
            ">=" -> expressionValue = ">="
            "<=" -> expressionValue = "<="
        }

        return expressionValue
    }
    fun getLabelFromExpression(context: Context,expression: String) : String {
        var label = ""
        when(expression){
            "=" -> label = context.getString(R.string.labelEqual)
            "like" -> label = context.getString(R.string.labelLike)
            "in" -> label = context.getString(R.string.labelIn)
            "not in" -> label = context.getString(R.string.labelNotIn)
            "!=" -> label = context.getString(R.string.labelNotEqual)
            "not like" -> label = context.getString(R.string.labelNotLike)
            "between" -> label = context.getString(R.string.labelBetween)
            ">" -> label = ">"
            "<" -> label = "<"
            ">=" -> label = ">="
            "<=" -> label = "<="
        }

        return label
    }
}