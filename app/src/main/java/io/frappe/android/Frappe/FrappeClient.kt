package io.frappe.android.Frappe

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.github.scribejava.core.model.OAuthRequest
import com.github.scribejava.core.model.Verb
import io.frappe.android.CallbackAsync.AuthReqCallback
import io.frappe.android.CallbackAsync.AuthRequest
import io.frappe.android.CallbackAsync.RetrieveAuthTokenTask
import io.frappe.android.R
import io.frappe.android.Utils.NetworkUtils
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.json.JSONObject
import java.io.IOException
import java.net.URL

class FrappeClient(ctx: Context){
    val ctx = ctx
    fun getAuthRequest() : AuthRequest {
        val oauth2Scope = ctx.getString(R.string.oauth2Scope)
        val clientId = ctx.getString(R.string.clientId)
        val clientSecret = ctx.getString(R.string.clientSecret)
        val serverURL = ctx.getString(R.string.serverURL)
        val redirectURI = ctx.getString(R.string.redirectURI)
        val authRequest = AuthRequest(oauth2Scope, clientId, clientSecret, serverURL, redirectURI)
        return authRequest
    }

    fun getServerURL () : String {
        return ctx.getString(R.string.serverURL)
    }

    fun getAuthReqCallback(request: OAuthRequest, responseCallback: AuthReqCallback) : AuthReqCallback {
        val authRequest = getAuthRequest()
        val authReqCallback = object : AuthReqCallback {
            override fun onSuccessResponse(s: String) {
                var bearerToken = JSONObject(s)
                if (bearerToken.length() > 0) {
                    authRequest.makeRequest(bearerToken.getString("access_token"), request, responseCallback)
                }
            }
            override fun onErrorResponse(s: String) {
                Log.d("ReqCallbackError", s)
            }
        }
        return authReqCallback
    }

    fun get_all(doctype: String,
                filters: String? = null,
                fields: String? = null,
                limit_page_length: String? = null,
                limit_start: String? = null,
                order_by: String? = null) : OAuthRequest {
        val encoded_doctype = doctype.replace(" ", "%20")
        var requestURL = getServerURL() + "/api/resource/$encoded_doctype?"

        if(!filters.isNullOrEmpty()) {
            requestURL += "filters=$filters&"
        }

        if(!fields.isNullOrEmpty()){
            requestURL += "fields=$fields&"
        }

        if(!limit_page_length.isNullOrEmpty()){
            requestURL += "limit_page_length=$limit_page_length&"
        }

        if(!limit_start.isNullOrEmpty()){
            requestURL += "limit_start=$limit_start&"
        }

        if(!order_by.isNullOrEmpty()) {
            requestURL += "order_by=$order_by&"
        }

        Log.d("requestURL", requestURL)

        val frappeRequest = OAuthRequest(Verb.GET, requestURL)

        return frappeRequest
    }

    fun get_doc(doctype: String, docname:String) : OAuthRequest {
        val encoded_doctype = doctype.replace(" ", "%20")
        val encoded_docname = docname.replace(" ", "%20")
        var requestURL = getServerURL() + "/api/resource/$encoded_doctype/$encoded_docname"
        Log.d("requestURL", requestURL)
        val frappeRequest = OAuthRequest(Verb.GET, requestURL)
        return frappeRequest
    }

    fun checkNetworkConnection(): Boolean {

        return NetworkUtils.isWifiConnected(ctx) ||
                NetworkUtils.isMobileConnected(ctx)||
                NetworkUtils.isConnected(ctx)
    }

    fun checkConnection(): Boolean {
        val connectUrl = URL(getServerURL())
        val connection = connectUrl.openConnection()
        connection.connectTimeout = 3000
        try {
            connection.connect()
            return true
        } catch (e: IOException){
            return false
        }
    }

    fun executeRequest(request: OAuthRequest, callback: AuthReqCallback) {
        RetrieveAuthTokenTask(
                context = ctx,
                callback = getAuthReqCallback(request, callback)
        ).execute()
    }

    suspend fun executeSuspendedRequest(request: OAuthRequest, callback: AuthReqCallback) = async(UI) {
        try {
            async(CommonPool){
                RetrieveAuthTokenTask(
                        context = ctx,
                        callback = getAuthReqCallback(request, callback)
                ).execute()
            }.await()
        } catch (e:Exception) {
            Log.e("suspendedRequest", "Error", e)
        } finally {
            Log.d("suspendedRequest", "finally")
        }
    }

    fun retrieveDocTypeMeta(editor: SharedPreferences.Editor, key: String, doctype: String?) {

        val request = OAuthRequest(Verb.GET, "${getServerURL()}/api/method/frappe.client.get_meta?doctype=${doctype}")
        val callback = object: AuthReqCallback {
            override fun onSuccessResponse(result: String) {
                val doctypeMetaJson = JSONObject(result).getJSONObject("message")
                editor.putString(key, doctypeMetaJson.toString())
                editor.commit()
            }

            override fun onErrorResponse(error: String) {
                Log.d("DocTypeMeta", "ResponseError")
            }

        }
        executeRequest(request, callback)

    }

}
