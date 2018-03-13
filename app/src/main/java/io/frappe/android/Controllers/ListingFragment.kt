package io.frappe.android.Controllers

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import org.json.JSONArray
import org.json.JSONObject
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.*
import android.widget.*
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.ArrayList
import android.widget.AdapterView.OnItemSelectedListener
import android.view.MenuInflater
import io.frappe.android.CallbackAsync.AuthReqCallback
import io.frappe.android.Controllers.BaseCompatActivity.Companion.DOCTYPE
import io.frappe.android.Frappe.FrappeClient
import io.frappe.android.R
import io.frappe.android.UI.EndlessRecyclerViewScrollListener
import io.frappe.android.UI.ListViewAdapter
import io.frappe.android.Utils.StringUtil
import kotlinx.android.synthetic.main.activity_listing.*
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast
import org.json.JSONException

open class ListingFragment : Fragment(), View.OnClickListener {

    internal lateinit var mRecyclerView: RecyclerView
    var recyclerAdapter: ListViewAdapter? = null
    var recyclerModels = JSONArray()
    var searchView : SearchView? = null
    var progressBar: ProgressBar? = null
    var filters: JSONArray = JSONArray()
    var loadServerData = false
    var order_by:String? = "modified+desc"
    var sortOrder: String? = "desc"
    var doctype: String? = null
    var doctypeMetaJson = JSONObject()
    var swipeRefresh: SwipeRefreshLayout? = null
    var form: Class<Any>? = null

    companion object {
        val DOCTYPE_META = "DOCTYPE_META"
        val KEY_DOCTYPE = "doctype"
        val KEY_FILTERS = "filters"
        val SET_DOCTYPE = 400
        val SET_DOCTYPE_FILTERS = 401
    }

    override fun onClick(view: View?) {
        var itemPosition = mRecyclerView.getChildLayoutPosition(view)
        var value = JSONObject(recyclerModels.get(itemPosition).toString()).get("name")
        if (form == null) {
            form = FormGeneratorActivity.javaClass
        }
        var intent = Intent(activity, form)
        intent.putExtra("DocType", this.doctype)
        intent.putExtra("DocName", value.toString())
        startActivity(intent)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.activity_listing, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDocType()

        setupFilters()

        setupView()

        setupSortOrder()

        setRecycleViewScrollListener()

        setupSwipeRefresh()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_view, menu)
        // Associate searchable configuration with the SearchView
        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu?.findItem(R.id.action_search)?.actionView as SearchView
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
        searchView?.setMaxWidth(Integer.MAX_VALUE)
        // listening to search query text change
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // filter recycler view when query submitted
                recyclerModels = JSONArray()
                mRecyclerView.adapter = null
                // name like query filter
                setupFilters()
                filters.put(JSONArray().put("name").put("like").put("%$query%"))

                loadData(filters=filters)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                // filter recycler view when text is changed
                /*
                recyclerModels = JSONArray()
                filters = "[[\"owner\",\"=\",\"$user\"],[\"name\",\"like\",\"%$query%\"]]"
                loadData(filters=filters!!)
                */
                return false
            }
        })

        searchView?.setOnCloseListener(object :SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                recyclerModels = JSONArray()
                mRecyclerView.adapter = null
                setupFilters()
                loadData(filters=filters)
                setRecycleViewScrollListener()
                return false
            }
        })
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_search -> return true
            R.id.action_sort -> {
                sortLayout.visibility = View.VISIBLE
                return true
            }
            R.id.action_filters -> {
                val mIntent = intentFor<FiltersActivity>()
                mIntent.putExtra(DOCTYPE, this.doctype)
                mIntent.putExtra(KEY_FILTERS, this.filters.toString())
                startActivityForResult(mIntent,SET_DOCTYPE_FILTERS)
                return true
            }
            R.id.action_add -> {
                var intent = Intent(activity, FormGeneratorActivity::class.java)
                intent.putExtra("DocType", this.doctype)
                startActivity(intent)
                return true
            }
            else -> return false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            SET_DOCTYPE -> {
                if (resultCode == Activity.RESULT_OK && data != null)
                    this.doctype = data?.extras?.getString(KEY_DOCTYPE)
            }
            SET_DOCTYPE_FILTERS -> {
                if (resultCode == Activity.RESULT_OK && data != null){
                    this.doctype = data?.extras?.getString(KEY_DOCTYPE)
                    this.filters = JSONArray(data?.extras?.getString(KEY_FILTERS))

                    recyclerModels = JSONArray()
                    mRecyclerView.adapter = null
                    loadData(filters = filters!!)
                    setRecycleViewScrollListener()
                }
            }
        }
    }

    open fun setupDocType() {
        if (doctype == null) {
            this.doctype = "Note"
        }
        val keyDocTypeMeta = StringUtil.slugify(this.doctype) + "_meta"
        var pref = activity.getSharedPreferences(DOCTYPE_META, 0)
        val editor = pref.edit()
        val doctypeMetaString = pref.getString(keyDocTypeMeta, null)
        if (doctypeMetaString != null){
            this.doctypeMetaJson = JSONObject(doctypeMetaString)
            setupSortSpinner()
        } else {
            FrappeClient(activity).retrieveDocTypeMeta(editor!!, keyDocTypeMeta, doctype)
            val recycler_view = find<RecyclerView>(R.id.recycler_view)
            recycler_view.visibility = View.GONE
            toast(getString(R.string.swipe_to_refresh))
        }
    }

    fun setupView() {
        mRecyclerView = activity.findViewById(R.id.recycler_view)
        progressBar = activity.findViewById(R.id.edit_progress_bar)
        progressBar?.visibility = View.VISIBLE
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true)
    }

    open fun setupFilters() {
        this.filters = JSONArray()
    }

    fun setupSortSpinner() {
        var list = ArrayList<String>().apply {
            add(getString(R.string.last_modified_on))
            add(getString(R.string.sort_name))
            add(getString(R.string.created_on))
            add(getString(R.string.most_used))
            add(getString(R.string.created_by))
            add(getString(R.string.modified_by))
        }
        var fields = JSONArray()
        try {
            fields = doctypeMetaJson.getJSONArray("fields")
        }catch (e: JSONException) {
            fields = JSONArray()
        }
        for (i in 0 until fields.length() -1 ){
            val bold = fields.getJSONObject(i).getInt("bold")
            val reqd = fields.getJSONObject(i).getInt("reqd")

            if(reqd != 0 || bold != 0){
                list.add(fields.getJSONObject(i).getString("label"))
            }
        }

        var spinner = activity.findViewById<Spinner>(R.id.spinner)
        val spinnerAdapter = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, list)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                recyclerModels = JSONArray()
                mRecyclerView.adapter = null
                loadData(filters = filters!!)
                setRecycleViewScrollListener()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Nothing selected
            }

        }
    }

    fun setRecycleViewScrollListener() {
        // use a linear layout manager
        val mLayoutManager = LinearLayoutManager(activity)
        mRecyclerView.setLayoutManager(mLayoutManager)

        mRecyclerView.addOnScrollListener(object: EndlessRecyclerViewScrollListener(mLayoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                if(loadServerData){
                    loadData(page, filters!!)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                sortLayout.visibility = View.GONE
            }
        })
    }

    fun loadData(page: Int? = null,
                 filters: JSONArray,
                 limit_page_length:String = "5") {

        if(recyclerModels.length() == 0) loadServerData = true

        // limit_start is page * limit_page_length or 0
        val limit_start = ((page?.times(limit_page_length.toInt()))?:0).toString()

        // make progress bar visible while loading data
        progressBar?.visibility = View.VISIBLE

        // set order
        val sortSpinner = activity.find<Spinner>(R.id.spinner)
        val spinnerLabel = sortSpinner.selectedItem.toString()
        var fields = JSONArray()
        try {
            fields = doctypeMetaJson.getJSONArray("fields")
        } catch (e: JSONException) {
            fields = JSONArray()
        }

        var spinnerField = "modified"

        for (i in 0 until fields.length() - 1){
            if(fields.getJSONObject(i).has("label") && fields.getJSONObject(i).getString("label") == spinnerLabel){
                spinnerField = fields.getJSONObject(i).getString("fieldname")
                break
            }
        }

        when(spinnerLabel){
            getString(R.string.last_modified_on) -> spinnerField="modified"
            getString(R.string.sort_name) -> spinnerField="name"
            getString(R.string.created_on) -> spinnerField="creation"
            getString(R.string.most_used) -> spinnerField="idx"
            getString(R.string.created_by) -> spinnerField="owner"
            getString(R.string.modified_by) -> spinnerField="modified_by"
        }

        order_by = "$spinnerField+$sortOrder"

        val request = FrappeClient(activity).get_all(
                doctype = doctype!!,
                filters = filters.toString(),
                limit_page_length = limit_page_length,
                limit_start = limit_start,
                order_by = order_by
        )

        val responseCallback = object : AuthReqCallback {
            override fun onSuccessResponse(s: String) {
                val response = JSONObject(s)
                // JSON Array from frappe's listing
                for (i in 0 until response.getJSONArray("data").length()) {
                    recyclerModels.put(response.getJSONArray("data").get(i))
                }
                if (page != null){
                    // Notify an adapter
                    recyclerAdapter!!.notifyDataSetChanged()
                } else if (page == null) {
                    // specify and add an adapter
                    recyclerAdapter = ListViewAdapter(recyclerModels)
                    recyclerAdapter!!.setOnClickListener(this@ListingFragment)

                    if (mRecyclerView.adapter == null)
                        mRecyclerView.adapter = recyclerAdapter
                }
                loadServerData = true
                progressBar?.visibility = View.GONE
                if(swipeRefresh!!.isRefreshing)
                    swipeRefresh!!.setRefreshing(false)
            }

            override fun onErrorResponse(s: String) {
                loadServerData =  false
                progressBar?.visibility = View.VISIBLE
                toast(R.string.somethingWrong)
            }
        }
        if(loadServerData) {
            loadServerData = false
            FrappeClient(activity).executeRequest(request, responseCallback)
        }
    }

    fun setupSortOrder() {
        val sortOrderButton = activity.find<Button>(R.id.sortOrderButton)
        sortOrderButton.onClick {
            // change icon
            when(sortOrder) {
                "desc" -> sortOrderButton.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_keyboard_arrow_down,0)
                "asc" -> sortOrderButton.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_keyboard_arrow_up,0)
            }
            // name like query filter
            sortOrder = if (sortOrder == "desc") "asc" else "desc"

            recyclerModels = JSONArray()
            mRecyclerView.adapter = null
            loadData(filters = filters!!)
            setRecycleViewScrollListener()
        }
    }

    fun setupSwipeRefresh() {
        swipeRefresh = activity.find<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh!!.setOnRefreshListener(
                SwipeRefreshLayout.OnRefreshListener {
                    recyclerModels = JSONArray()
                    mRecyclerView.adapter = null
                    val recycler_view = find<RecyclerView>(R.id.recycler_view)
                    recycler_view.visibility = View.VISIBLE
                    loadData(filters = filters!!)
                    setRecycleViewScrollListener()
                }
        )
    }
}