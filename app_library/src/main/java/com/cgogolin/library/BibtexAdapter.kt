package com.cgogolin.library

import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Comparator

import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.OutputStream

import android.content.Context
import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.pm.ResolveInfo
import android.content.pm.PackageManager

import android.net.Uri

import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.view.View.MeasureSpec

import android.view.View.OnClickListener

import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Toast

//import android.animation.LayoutTransition;
//import android.animation.ObjectAnimator;
//import android.animation.AnimatorSet;

import android.os.AsyncTask

import android.webkit.MimeTypeMap

import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.Transformation
import java.util.HashMap

import java.util.Arrays.fill

open class BibtexAdapter @Throws(java.io.IOException::class)
constructor(inputStream: InputStream) : BaseAdapter() {

    private val bibtexEntryList: ArrayList<BibtexEntry>?
    private var displayedBibtexEntryList: ArrayList<BibtexEntry>? = null
    private var bibtexGroupEntryList: ArrayList<BibtexEntry>? = null
    private val groupMap: HashMap<String, ArrayList<BibtexEntry>>
    private val filter: String? = null

    internal var sortedAccordingTo = SortMode.None
    internal var filteredAccodingTo = ""
    internal var sortingAccordingTo: SortMode? = null
    internal var filteringAccodingTo: String? = null
    internal var selectedGroup = ""
    internal var selectingGroup: String? = null
    // Cache row states based on positions
    private val mRowStates: IntArray
    private var separatorComparator: Comparator<BibtexEntry>? = null

    internal var applyFilterTask: AsyncTask<Any, Void, Void>? = null
    internal var sortTask: AsyncTask<BibtexAdapter.SortMode, Void, Void>? = null
    val groups: Set<String>
        get() = groupMap.keys

    enum class SortMode {
        None, Date, Author, Journal, Title
    }

    init {
        bibtexEntryList = BibtexParser.parse(inputStream)
        bibtexGroupEntryList = bibtexEntryList
        //Copy all entries to the filtered list
        displayedBibtexEntryList = ArrayList()
        displayedBibtexEntryList!!.addAll(bibtexEntryList)
        groupMap = HashMap()
        for (entry in bibtexEntryList) { //populate groups hashmap
            val entryGroupList = entry.groups
            if (entryGroupList != null) {
                for (groupName in entryGroupList) {
                    addToGroup(groupName, entry)
                }
            }
        }
        mRowStates = IntArray(count)
    }

    open fun onPreBackgroundOperation() {}
    open fun onPostBackgroundOperation() {}
    fun onBackgroundOperationCanceled() {}
    open fun onEntryClick(v: View) {}
    fun addToGroup(groupName: String, entry: BibtexEntry) {
        if (!groupMap.containsKey(groupName)) {
            groupMap[groupName] = ArrayList()
        }
        groupMap[groupName]!!.add(entry)
    }

    @Synchronized
    fun filterAndSortInBackground(filter: String?, sortMode: SortMode?, group: String?) {

        if (filter == null || filteringAccodingTo != null && filteringAccodingTo == filter && sortingAccordingTo != null && sortMode != null && sortingAccordingTo == sortMode && selectingGroup != null && group != null && selectingGroup == group)
            return

        if (applyFilterTask != null) {
            applyFilterTask!!.cancel(true)
        }

        applyFilterTask = object : AsyncTask<Any, Void, Void>() {
            override fun onPreExecute() {
                onPreBackgroundOperation()
            }

            override fun doInBackground(vararg params: Any): Void? {
                selectingGroup = params[2] as String
                if (selectedGroup != selectingGroup) {
                    selectGroup(selectingGroup!!)
                }
                selectingGroup = null
                //must set filteringAccordingTo after selecting group
                filteringAccodingTo = params[0] as String
                sortingAccordingTo = params[1] as SortMode
                if (filteredAccodingTo != filteringAccodingTo) {
                    filter(filteringAccodingTo!!)
                }
                filteringAccodingTo = null
                if (sortedAccordingTo != sortingAccordingTo) {
                    sort(sortingAccordingTo)
                }
                sortingAccordingTo = null
                return null
            }

            override fun onPostExecute(v: Void?) {
                notifyDataSetChanged()
                onPostBackgroundOperation()
            }
        }
        applyFilterTask!!.execute(filter as Any?, sortMode as Any?, group as Any?)
    }

    @Synchronized
    protected fun selectGroup(groupName: String) {
        if (groupName == "" || !groupMap.containsKey(groupName)) {
            bibtexGroupEntryList = bibtexEntryList
            selectedGroup = ""
        } else {
            bibtexGroupEntryList = groupMap[groupName]
            selectedGroup = groupName
        }
        selectingGroup = null
        filter("") //put all group entries in the displayed list, whether or not additional filtering needed
        filteredAccodingTo = ""
        sortedAccordingTo = SortMode.None
    }

    @Synchronized
    protected fun filter(vararg filter: String) {
        val filteredTmpBibtexEntryList = ArrayList<BibtexEntry>()
        if (filter[0].trim { it <= ' ' } == "") {
            filteredTmpBibtexEntryList.addAll(bibtexGroupEntryList!!)
        } else {
            for (entry in bibtexGroupEntryList!!) {
                val blob = entry.stringBlob!!.toLowerCase()
                val substrings = filter[0].toLowerCase().split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var matches = true
                for (substring in substrings) {
                    if (!blob.contains(substring)) {
                        matches = false
                        break
                    }
                }
                if (matches)
                    filteredTmpBibtexEntryList.add(entry)
            }
        }
        displayedBibtexEntryList = filteredTmpBibtexEntryList
        filteringAccodingTo = null
        filteredAccodingTo = filter[0]
        sortedAccordingTo = SortMode.None
    }

    @Synchronized
    fun sortInBackground(sortMode: SortMode?) {
        if (sortMode == null)
            return

        if (sortTask != null) {
            sortTask!!.cancel(true)
        }

        sortTask = object : AsyncTask<BibtexAdapter.SortMode, Void, Void>() {
            override fun onPreExecute() {
                onPreBackgroundOperation()
            }

            override fun doInBackground(vararg sortMode: BibtexAdapter.SortMode): Void? {
                filterAndSortInBackground(null, null, null)//Does nothing if filtering is already done, else waits until filtering is finished
                sortingAccordingTo = sortMode[0]
                if (sortedAccordingTo != sortingAccordingTo) {
                    sort(sortingAccordingTo)
                }
                sortingAccordingTo = null
                return null
            }

            override fun onPostExecute(v: Void) {
                notifyDataSetChanged()
                onPostBackgroundOperation()
            }
        }
        sortTask!!.execute(sortMode)
    }

    @Synchronized
    protected fun sort(sortMode: SortMode?) {
        if (sortMode == null) return

        when (sortMode) {
            BibtexAdapter.SortMode.None -> {
                Collections.sort(displayedBibtexEntryList!!) { entry1, entry2 -> entry1.numberInFile.compareTo(entry2.numberInFile) }
                separatorComparator = null
            }
            SortMode.Date -> {
                Collections.sort(displayedBibtexEntryList!!) { entry1, entry2 -> (entry2.dateFormated + entry2.numberInFile).compareTo(entry1.dateFormated + entry1.numberInFile) }
                separatorComparator = Comparator { entry1, entry2 -> entry2.year!!.compareTo(entry1.year!!) }
            }
            SortMode.Author -> {
                displayedBibtexEntryList!!.sortWith(Comparator { entry1, entry2 -> (entry1.authorSortKey + entry1.numberInFile).compareTo(entry2.authorSortKey + entry2.numberInFile) })
                separatorComparator = Comparator { entry1, entry2 ->
                    if (entry1.authorSortKey!!.length == 0 && entry1.authorSortKey!!.isEmpty())
                        0
                    else if (entry1.authorSortKey!!.isEmpty())
                        -1
                    else if (entry2.authorSortKey!!.isEmpty())
                        1
                    else
                        entry1.authorSortKey!!.substring(0, 1).compareTo(entry2.authorSortKey!!.substring(0, 1))
                }
            }
            SortMode.Journal -> {
                displayedBibtexEntryList!!.sortWith(Comparator { entry1, entry2 -> (entry1.journal + entry1.numberInFile).compareTo(entry2.journal + entry2.numberInFile) })
                separatorComparator = Comparator { entry1, entry2 -> entry1.journal!!.toLowerCase().compareTo(entry2.journal!!.toLowerCase()) }
            }
            SortMode.Title -> {
                displayedBibtexEntryList!!.sortWith(Comparator { entry1, entry2 -> (entry1.title + entry1.numberInFile).compareTo(entry2.title + entry2.numberInFile) })
                separatorComparator = Comparator { entry1, entry2 ->
                    if (entry1.title!!.isEmpty() && entry1.title!!.isEmpty())
                        0
                    else if (entry1.title!!.isEmpty())
                        -1
                    else if (entry2.title!!.isEmpty())
                        1
                    else
                        entry1.title!!.substring(0, 1).compareTo(entry2.title!!.substring(0, 1))
                }
            }
        }
        sortingAccordingTo = null
        sortedAccordingTo = sortMode
        fill(mRowStates, 0)
    }

    @Synchronized
    fun prepareForFiltering() {
        val PrepareBibtexAdapterForFilteringTask = object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg v: Void): Void? {
                if (bibtexEntryList != null)
                    for (entry in bibtexEntryList) {
                        entry.stringBlob
                    }
                return null
            }
        }
        PrepareBibtexAdapterForFilteringTask.execute()
    }

    private fun setTextViewAppearance(textView: TextView, text: String?) {
        if (text == null || text == "")
            textView.visibility = View.GONE
        else {
            textView.text = text
            textView.visibility = View.VISIBLE
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val context = parent.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var showSeparator = false
        var separatorText = ""

        val entry = getItem(position)


        if (convertView == null) {
            convertView = inflater.inflate(R.layout.bibtexentry, null)
        }

        if (displayedBibtexEntryList == null || displayedBibtexEntryList!!.size == 0) {
            setTextViewAppearance(convertView!!.findViewById<View>(R.id.separator) as TextView, "")
            setTextViewAppearance(convertView.findViewById<View>(R.id.bibtex_info) as TextView, context.getString(R.string.no_matches))
            setTextViewAppearance(convertView.findViewById<View>(R.id.bibtex_title) as TextView, "")
            setTextViewAppearance(convertView.findViewById<View>(R.id.bibtex_authors) as TextView, "")
            setTextViewAppearance(convertView.findViewById<View>(R.id.bibtex_journal) as TextView, "")
        } else {
            if (separatorComparator != null) {
                when (mRowStates[position]) {
                    SECTIONED_STATE -> showSeparator = true

                    REGULAR_STATE -> showSeparator = false

                    else -> {
                        if (position == 0) {
                            showSeparator = true
                        } else {
                            val prevEntry = getItem(position - 1)
                            if (separatorComparator!!.compare(entry, prevEntry) != 0) {
                                showSeparator = true
                            }
                        }
                        mRowStates[position] = if (showSeparator) SECTIONED_STATE else REGULAR_STATE
                    }
                }
                if (showSeparator) {
                    separatorText = when (sortedAccordingTo) {
                        SortMode.Date -> entry!!.year!!
                        SortMode.Author -> if (entry!!.authorSortKey!!.isNotEmpty())
                            entry.authorSortKey!!.substring(0, 1)
                        else ""
                        SortMode.Journal -> entry!!.journal!!
                        SortMode.Title -> if (entry!!.title!!.isNotEmpty())
                            entry.title!!.substring(0, 1)
                        else ""
                        else -> error("there wasn't an else here before matt's fork")
                    }
                }
            }
            setTextViewAppearance(convertView!!.findViewById<View>(R.id.separator) as TextView, separatorText)
            setTextViewAppearance(convertView.findViewById<View>(R.id.bibtex_info) as TextView, "")
            setTextViewAppearance(convertView.findViewById<View>(R.id.bibtex_title) as TextView, entry!!.title)
            setTextViewAppearance(convertView.findViewById<View>(R.id.bibtex_authors) as TextView, entry.getAuthorsFormated(context))
            setTextViewAppearance(convertView.findViewById<View>(R.id.bibtex_journal) as TextView, entry.getJournalFormated(context))

            if (entry.extraInfoVisible())
                makeExtraInfoVisible(position, convertView, context, false)
            else
                makeExtraInfoInvisible(position, convertView, false)

            convertView.setOnClickListener { v ->
                onEntryClick(v)
                val extraInfo = v.findViewById<View>(R.id.LinearLayout02) as LinearLayout
                if (extraInfo.visibility != View.VISIBLE) {
                    makeExtraInfoVisible(position, v, context, true)
                } else {
                    makeExtraInfoInvisible(position, v, true)
                }
            }
        }
        return convertView
    }

    override fun getCount(): Int {
        return if (displayedBibtexEntryList == null || displayedBibtexEntryList!!.size == 0)
            1
        else
            displayedBibtexEntryList!!.size
    }

    override fun getItem(position: Int): BibtexEntry? {
        return if (displayedBibtexEntryList == null || displayedBibtexEntryList!!.size == 0)
            null
        else
            displayedBibtexEntryList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //Must be overwritten to create a Uri suitable for the respective Android version
    internal open fun getUriForActionViewIntent(path: String?): Uri? {
        return Uri.parse("file://" + path!!)
    }

    //Can be overriden to modify the path for opening files
    internal open fun getModifiedPath(path: String): String {
        return path
    }

    private fun makeExtraInfoVisible(position: Int, v: View, context: Context, animate: Boolean) {
        val extraInfo = v.findViewById<View>(R.id.LinearLayout02) as LinearLayout
        extraInfo.removeAllViews()

        val entry = getItem(position)
        entry!!.setExtraInfoVisible(true)

        val buttonLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val doiTV = TextView(context)
        setTextViewAppearance(doiTV, entry.getDoiFormated(context))
        extraInfo.addView(doiTV)
        val arxivTV = TextView(context)
        setTextViewAppearance(arxivTV, entry.eprintFormated)
        extraInfo.addView(arxivTV)

        //Read the Files list from the BibtexEntry
        val associatedFilesList = entry.files
        if (associatedFilesList != null) {
            for (file in associatedFilesList) {
                val path = getModifiedPath(file)//Path replacement can be done by overriding getModifiedPath()

                if (path == null || path == "") continue

                val button = Button(context)
                button.layoutParams = buttonLayoutParams
                button.text = context.getString(R.string.file) + ": " + path
                button.setOnClickListener(OnClickListener {
                    val uri = getUriForActionViewIntent(path) ?: return@OnClickListener

                    checkCanWriteToUri(context, uri)

                    openExternally(context, uri)
                })
                extraInfo.addView(button)
            }
        }

        //Read the URLs list from the BibtexEntry
        val associatedUrlList = entry.getUrls(context)
        if (associatedUrlList != null) {
            for (url in associatedUrlList) {
                if (url == null || url == "") continue

                val button = Button(context)
                button.layoutParams = buttonLayoutParams
                button.text = context.getString(R.string.url) + ": " + url
                button.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, context.getString(R.string.error_opening_webbrowser), Toast.LENGTH_SHORT).show()
                    }
                }
                extraInfo.addView(button)
            }
        }

        //Read from the DOIs list from the BibtexEntry
        val associatedDoiList = entry.getDoiLinks(context)
        if (associatedDoiList != null) {
            for (doi in associatedDoiList) {
                if (doi == null || doi == "") continue

                val button = Button(context)
                button.layoutParams = buttonLayoutParams
                button.text = context.getString(R.string.doi) + ": " + doi
                button.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(doi)
                    //                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, context.getString(R.string.error_opening_webbrowser), Toast.LENGTH_SHORT).show()
                    }
                }
                extraInfo.addView(button)
            }
        }

        //Add a share button
        val entryString = entry.entryAsString
        val button = Button(context)
        button.layoutParams = buttonLayoutParams
        button.text = context.getString(R.string.share)
        button.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "plain/text"
            shareIntent.type = "*/*"
            shareIntent.putExtra(Intent.EXTRA_TEXT, entryString)
            try {
                context.startActivity(shareIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, context.getString(R.string.error_starting_share_intent), Toast.LENGTH_SHORT).show()
            }
        }
        extraInfo.addView(button)
        extraInfo.visibility = View.VISIBLE

        if (animate) {
            val layoutParams = LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT)
            extraInfo.measure(View.MeasureSpec.makeMeasureSpec((extraInfo.parent as LinearLayout).width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))//Need to call this once so that extraInfo knows how large it wants to be
            val bottomMargin = -extraInfo.measuredHeight
            layoutParams.setMargins(0, 0, 0, bottomMargin)
            extraInfo.layoutParams = layoutParams
            val marginAnimation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    val layoutParams = extraInfo.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.setMargins(0, 0, 0, ((1.0 - interpolatedTime) * bottomMargin).toInt())
                    extraInfo.layoutParams = layoutParams
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }
            marginAnimation.duration = 200
            extraInfo.startAnimation(marginAnimation)
        }
    }

    private fun makeExtraInfoInvisible(position: Int, v: View, animate: Boolean) {
        val extraInfo = v.findViewById<View>(R.id.LinearLayout02) as LinearLayout

        val entry = getItem(position)
        entry!!.setExtraInfoVisible(false)

        if (animate) {
            val layoutParams = LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT)
            val bottomMargin = -extraInfo.height
            extraInfo.layoutParams = layoutParams
            val marginAnimation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    val layoutParams = extraInfo.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.setMargins(0, 0, 0, (interpolatedTime * bottomMargin).toInt())
                    extraInfo.layoutParams = layoutParams
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }
            marginAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(animation: Animation) {
                    extraInfo.visibility = View.GONE
                }

                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
            })
            marginAnimation.duration = 200
            extraInfo.startAnimation(marginAnimation)
        } else {
            extraInfo.visibility = View.GONE
        }
    }

    fun checkCanWriteToUri(context: Context, uri: Uri?) {
        var os: OutputStream? = null
        Log.i(context.getString(R.string.app_name), "checking if we can somehow open an output stream to uri")
        try {
            os = context.contentResolver.openOutputStream(uri!!, "wa")
            if (os != null) {
                Log.i(context.getString(R.string.app_name), "opened os succesfully")
                os.close()
                Log.i(context.getString(R.string.app_name), "output stream successfully opened and closed")
            }
        } catch (e: Exception) {
            Log.i(context.getString(R.string.app_name), "exception while opening os: $e")
            if (os != null)
                try {
                    os.close()
                } catch (e2: Exception) {
                    os = null
                }
        }
    }

    fun openExternally(context: Context?, uri: Uri?) {
        if ((uri == null) or (context == null))
            return

        //Determine mime type
        val map = MimeTypeMap.getSingleton()
        var extension = ""
        val uriString = uri!!.toString()
        if (uriString.lastIndexOf(".") != -1) extension = uriString.substring(uriString.lastIndexOf(".") + 1, uriString.length)

        val type = map.getMimeTypeFromExtension(extension)

        //Start application to open the file and grant permissions
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        intent.setDataAndType(uri, type)
        try {
            context!!.startActivity(intent)
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                //Taken from http://stackoverflow.com/questions/18249007/how-to-use-support-fileprovider-for-sharing-content-to-other-apps
                try {
                    val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                    for (resolveInfo in resInfoList) {
                        val packageName = resolveInfo.activityInfo.packageName
                        context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    }
                } catch (e: Exception) {
                }
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context!!.getString(R.string.no_application_to_view_files_of_type) + " " + type + ".", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        // State of the row that needs to show separator
        private val SECTIONED_STATE = 1
        // State of the row that need not show separator
        private val REGULAR_STATE = 2
    }
}

