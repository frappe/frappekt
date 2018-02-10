package io.frappe.android.Controllers

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.frappe.android.BroadcastReceivers.ConnectivityReceiver
import io.frappe.android.Frappe.FrappeClient
import io.frappe.android.R
import io.frappe.android.Utils.StringUtil
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject

open class BaseCompatActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    val connectivityReceiver = ConnectivityReceiver()
    val frappeClient = FrappeClient(this)

    var docMeta: JSONObject? = null
    var doctype: String? = null
    var filters: JSONArray? = null

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        checkNetworkState()
    }

    private fun checkNetworkState() {
        if (!frappeClient.checkNetworkConnection()) {
            showAlert()
        } else if (frappeClient.checkNetworkConnection()){
            val asyncTask = object : AsyncTask<Void, Void, Boolean>() {
                override fun doInBackground(vararg params: Void?): Boolean{
                    return frappeClient.checkConnection()
                }

                override fun onPostExecute(result: Boolean) {
                    if (!result){
                        showAlert()
                    }
                }
            }
            asyncTask.execute()
        }
    }

    private fun showAlert() {
        alert(getString(R.string.click_ok_when_enabled)) {
            title = getString(R.string.please_enable_net_connection)
            positiveButton(getString(R.string.ok)){
                checkNetworkState()
            }
        }.show().setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        registerReceiver(connectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        ApplicationController.instance?.setConnectivityListener(this)
    }

    override fun onPause(){
        super.onPause()
        unregisterReceiver(connectivityReceiver)
        ApplicationController.instance?.activityPaused()
    }

    override fun onResume() {
        super.onResume()
        ApplicationController.instance?.setConnectivityListener(this);
        ApplicationController.instance?.activityResumed()
        registerReceiver(connectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    open fun setupDocType(doctype:String) {
        Log.d("DOC", doctype)
        this.doctype = doctype
        val keyDocTypeMeta = StringUtil.slugify(this.doctype) + "_meta"
        var pref = getSharedPreferences(ListingFragment.DOCTYPE_META, 0)
        val editor = pref.edit()
        val doctypeMetaString = pref.getString(keyDocTypeMeta, null)
        if (doctypeMetaString != null){
            this.docMeta = JSONObject(doctypeMetaString)
        } else {
            doAsync {
                FrappeClient(applicationContext).retrieveDocTypeMeta(editor, keyDocTypeMeta, doctype)
                uiThread {
                    setupDocType(doctype)
                }
            }
        }
    }

    open fun setupFilters(filters:JSONArray) {
        Log.d("Filters", filters.toString())
        this.filters = filters
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                SHOW_REQUEST -> {
                    setupDocType(data?.extras?.getString(DOCTYPE)!!)
                    setupFilters(JSONArray(data?.extras?.getString(FILTER)!!))
                }
            }
        }
    }

    companion object {
        val SHOW_REQUEST = 500
        val DOCTYPE = "doctype"
        val FILTER = "filter"
    }
}
