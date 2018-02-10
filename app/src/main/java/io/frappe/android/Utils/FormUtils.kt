package io.frappe.android.Utils

import android.accounts.AccountManager
import android.app.DatePickerDialog
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import io.frappe.android.CallbackAsync.AuthReqCallback
import io.frappe.android.Controllers.FormGeneratorActivity
import io.frappe.android.Frappe.FrappeClient
import io.frappe.android.R
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class FormUtils(ctx: FormGeneratorActivity) {
    val context = ctx

    // Check if the current user is the owner of the document being viewed
    fun isOwner(owner: String): Boolean {
        val mAccountManager = AccountManager.get(context)
        val accounts = mAccountManager?.getAccountsByType(context.getString(R.string.package_name))
        val user = accounts?.get(0)?.name

        if(owner==user){
            return true
        }
        return false
    }

    // fetch data for a document and return
    fun fetchDoc(doctype: String,
                 docname: String,
                 fields: String,
                 setupCallback: (JSONObject) -> Unit) {
        val filters = JSONArray().put(JSONArray().put("name").put("=").put(docname))
        val request = FrappeClient(context).get_all(
                doctype = doctype,
                filters = filters.toString(),
                fields = fields
        )

        val responseCallback = object : AuthReqCallback {
            override fun onSuccessResponse(result: String) {
                var data = JSONObject(JSONObject(result).getJSONArray("data").get(0).toString())
                setupCallback(data)
            }

            override fun onErrorResponse(error: String) {
                Toast.makeText(context, R.string.somethingWrong, Toast.LENGTH_SHORT).show()
            }
        }
        doAsync {
            FrappeClient(context).executeRequest(request, responseCallback)
        }
    }

    // Opens up a Calendar dialog box if the fieldtype is DateTime or Date
    fun displayCalender(value: EditText) {
        var cal = Calendar.getInstance()

        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "yyyy-MM-dd" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)
                value.setText(sdf.format(cal.time))
            }
        }

        var dateDialog = DatePickerDialog(context,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH))

        value.setOnClickListener(View.OnClickListener {
            dateDialog.show()
        })

        value.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                dateDialog.show()
            }
        })
    }

}