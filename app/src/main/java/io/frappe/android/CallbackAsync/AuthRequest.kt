package io.frappe.android.CallbackAsync

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import com.github.scribejava.apis.FrappeApi

import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.model.OAuthRequest
import com.github.scribejava.core.oauth.OAuth20Service

class AuthRequest(oauth2Scope: String,
                  clientId: String,
                  clientSecret: String,
                  serverURL: String,
                  redirectURI: String) {

    internal var oauth20Service: OAuth20Service = ServiceBuilder(clientId)
            .apiSecret(clientSecret)
            .scope(oauth2Scope)
            .callback(redirectURI)
            .build(FrappeApi.instance(serverURL))

    fun makeRequest(accessToken: String,
            request: OAuthRequest,
            callback: AuthReqCallback) {

        val oAuth2AccessToken = OAuth2AccessToken(accessToken)
        val mHandler = Handler(Looper.getMainLooper())
        mHandler.post {
            object : AsyncTask<Void, Void, String>() {
                override fun doInBackground(vararg params: Void): String {
                    oauth20Service.signRequest(oAuth2AccessToken, request)
                    val response = oauth20Service.execute(request)
                    return response!!.body
                }

                override fun onPostExecute(result: String) {
                    callback.onSuccessResponse(result)
                }
            }.execute()
        }
    }
}
