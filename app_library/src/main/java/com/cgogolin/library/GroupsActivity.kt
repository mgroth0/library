package com.cgogolin.library

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

import java.util.ArrayList

class GroupsActivity : AppCompatActivity() {

    private var groupList: ArrayList<String>? = null
    private var groupsArrayAdapter: ArrayAdapter<String>? = null
    private var lv: ListView? = null
    private var context: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.groupslist)
        groupList = intent.getSerializableExtra("group list") as ArrayList<String>
        groupList!!.add(0, "All")
        groupsArrayAdapter = ArrayAdapter(this, R.layout.grouplistentry, R.id.group_name, groupList!!)
        lv = findViewById<View>(R.id.bibtex_grouplist_view) as ListView
        lv!!.adapter = groupsArrayAdapter
        lv!!.onItemClickListener = AdapterView.OnItemClickListener { adapter, v, position, id ->
            var selectedGroup = ""
            if (position != 0) {
                selectedGroup = adapter.getItemAtPosition(position) as String
            }
            val resultIntent = Intent()
            resultIntent.putExtra("group", selectedGroup)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
