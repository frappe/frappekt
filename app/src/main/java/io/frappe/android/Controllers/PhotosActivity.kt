package io.frappe.android.Controllers

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import io.frappe.android.CallbackAsync.AuthReqCallback
import io.frappe.android.Frappe.FrappeClient
import io.frappe.android.R
import io.frappe.android.UI.PhotoViewAdapter
import kotlinx.android.synthetic.main.activity_photos.*
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject

class PhotosActivity : BaseCompatActivity() {

    internal lateinit var mRecyclerView: RecyclerView
    var recyclerAdapter: PhotoViewAdapter? = null
    var recyclerModels = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photos)

        // set doctype and meta
        if(intent.hasExtra(DOCTYPE)){
            setupDocType(intent.getStringExtra(DOCTYPE))
        }

        // set filters
        if(intent.hasExtra(ListingFragment.KEY_FILTERS)) {
            this.filters = JSONArray(intent.extras.getString(ListingFragment.KEY_FILTERS))
            fetchFiles(filters = filters!!)

        }

        mRecyclerView = findViewById(R.id.imageList)

        val mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
        mRecyclerView.setLayoutManager(mLayoutManager)
        recyclerAdapter = PhotoViewAdapter(recyclerModels, imagePreview)
        // recyclerAdapter!!.setOnClickListener(this@PhotosActivity)
        mRecyclerView.adapter = recyclerAdapter

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true)

    }

    fun fetchFiles(filters: JSONArray) {

        // if(recyclerModels.length() == 0) loadServerData = true

        // make progress bar visible while loading data
        imageProgress?.visibility = View.VISIBLE

        val request = FrappeClient(this).get_all(
                doctype = doctype!!,
                fields = "[\"*\"]",
                filters = filters.toString()
        )

        val responseCallback = object : AuthReqCallback {
            override fun onSuccessResponse(s: String) {
                val response = JSONObject(s)
                // JSON Array from frappe's listing
                for (i in 0 until response.getJSONArray("data").length()) {
                    val jsonObject = response.getJSONArray("data").getJSONObject(i)
                    var imageExtensions = ArrayList<String>().apply {
                        add("jpg")
                        add("jpeg")
                        add("png")
                    }
                    if (imageExtensions.contains(
                                    jsonObject?.getString("file_url")
                                            ?.substring(jsonObject
                                                    ?.getString("file_url").lastIndexOf(".") + 1
                                            )?.toLowerCase()
                            )
                    ) {
                        recyclerModels.put(jsonObject)
                    }
                }
                if (mRecyclerView.adapter != null){
                    // Notify an adapter
                    recyclerAdapter!!.notifyDataSetChanged()
                } else {
                    // specify and add an adapter
                    recyclerAdapter = PhotoViewAdapter(recyclerModels, imagePreview/*, docMeta!!*/)


                    if (mRecyclerView.adapter == null)
                        mRecyclerView.adapter = recyclerAdapter
                }
                // loadServerData = true

                sortLayout.visibility = View.VISIBLE
                imageProgress?.visibility = View.GONE
            }

            override fun onErrorResponse(s: String) {
                // loadServerData =  false
                imageProgress?.visibility = View.VISIBLE
                toast(R.string.somethingWrong)
            }
        }
        FrappeClient(this).executeRequest(request, responseCallback)
        // if(loadServerData) {
        //     loadServerData = false
        // }
    }
}
