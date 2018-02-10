package io.frappe.android.CallbackAsync

interface AuthReqCallback {
    fun onSuccessResponse(result: String)
    fun onErrorResponse(error: String)
}
