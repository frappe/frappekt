package io.frappe.android.Controllers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import io.frappe.android.R
import io.frappe.android.UI.FilterViewAdapter
import kotlinx.android.synthetic.main.activity_filters.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import org.json.JSONArray

class FiltersActivity : BaseCompatActivity() {

    internal lateinit var mRecyclerView: RecyclerView
    var recyclerAdapter: FilterViewAdapter? = null
    var recyclerModels = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)

        // set doctype and meta
        if(intent.hasExtra(DOCTYPE)){
            setupDocType(intent.getStringExtra(DOCTYPE))
        }

        // set filters
        if(intent.hasExtra(ListingFragment.KEY_FILTERS)) {
            this.filters = JSONArray(intent.extras.getString(ListingFragment.KEY_FILTERS))
            recyclerModels = filters as JSONArray
        }

        mRecyclerView = findViewById(R.id.filter_recycler_view)

        val mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.setLayoutManager(mLayoutManager)

        recyclerAdapter = FilterViewAdapter(recyclerModels, docMeta!!)
        mRecyclerView.adapter = recyclerAdapter

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true)

        bAddFilter.onClick {
            recyclerModels.put(JSONArray().put("name").put("=").put(""))
            recyclerAdapter!!.notifyItemInserted(recyclerModels.length() - 1)
        }

        bSetFilter.onClick {
            var setFilter = false
            Log.d("setFilter", recyclerModels.toString())
            for(i in 0 until recyclerModels.length()) {
                Log.d("bSetFilters", recyclerModels.getJSONArray(i).toString())
                if(recyclerModels.getJSONArray(i).length() == 0) {
                    Log.d("bSetFilters", recyclerModels.toString())
                    toast(getString(R.string.please_set_filter))
                    setFilter = false
                } else {
                    setFilter = true
                }
            }
            if(setFilter || recyclerModels.length() == 0){
                val results = Intent()
                results.putExtra(ListingFragment.KEY_FILTERS, recyclerModels.toString())
                results.putExtra(ListingFragment.KEY_DOCTYPE, doctype)
                setResult(Activity.RESULT_OK, results)
                finish()
            }
        }
    }
}
