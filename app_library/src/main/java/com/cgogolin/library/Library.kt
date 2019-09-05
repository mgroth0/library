package com.cgogolin.library

import java.io.File
import java.io.InputStream
import java.io.FileInputStream

import java.util.ArrayList
import java.util.regex.Pattern
import android.util.Log

import android.app.Activity

import android.os.Bundle

import android.content.Context
import android.content.SharedPreferences
import android.content.DialogInterface
import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager

import android.Manifest.permission
import android.app.ActionBar
import android.app.AlertDialog
import android.app.DownloadManager
import android.app.SearchManager

import android.support.v4.content.FileProvider
import android.support.v4.provider.DocumentFile
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView

import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.view.Menu
import android.view.MenuItem
import android.view.MenuInflater
import android.view.MotionEvent
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.MenuItem.OnMenuItemClickListener
import android.view.inputmethod.InputMethodManager

import android.text.InputType

import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.PopupMenu
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.webkit.MimeTypeMap

import android.net.Uri

import android.os.AsyncTask

class Library : AppCompatActivity(), SearchView.OnQueryTextListener {

    private var context: Context? = null

    private var libraryWasPreviouslyInitializedCorrectly = false
    private var libraryPathString: String? = "/mnt/sdcard/"
    private var pathTargetString: String? = "home/username"
    private var pathReplacementString: String? = "/mnt/sdcard"
    private var pathPrefixString: String? = ""
    private var libraryFolderRootUri: Uri? = null
    private var uriTargetString: String? = null
    private var uriReplacementString: String? = null
    private var uriPrefixString: String? = null

    private var prepareBibtexAdapterTask: AsyncTask<String, Void, Void>? = null
    private var analyseLibraryFolderRootTask: AsyncTask<String, Void, Boolean>? = null

    internal var sortMode: BibtexAdapter.SortMode = BibtexAdapter.SortMode.None
    internal var filter = ""
    internal var group = ""
    private var menu: Menu? = null

    private var oldQueryText = ""
    private var savedQueryText: String? = null
    private var bibtexListView: ListView? = null
    private var bibtexAdapter: LibraryBibtexAdapter? = null
    private var progressBar: ProgressBar? = null
    private var searchView: android.support.v7.widget.SearchView? = null
    private var setLibraryPathDialog: AlertDialog? = null
    private var setTargetAndReplacementStringsDialog: AlertDialog? = null
    private var setLibraryFolderRootUriDialog: AlertDialog? = null
    private var analysingLibraryFolderRootDialog: AlertDialog? = null
    private var pathOfFileTrigeredSetLibraryFolderRootDialog: String? = null

    internal inner class LibraryBibtexAdapter @Throws(java.io.IOException::class)
    constructor(private val context: Context, inputStream: InputStream) : BibtexAdapter(inputStream) {
        internal override fun getUriForActionViewIntent(path: String?): Uri? {

            if (android.os.Build.VERSION.SDK_INT < 21) {
                val uri = Uri.parse("file://" + path!!)
                var file: File? = null
                if (path != null && uri != null) {
                    file = File(uri.path!!)
                }
                if (uri == null || !file!!.isFile) {
                    Toast.makeText(context, context.getString(R.string.couldnt_find_file) + " " + path + ".\n\n" + context.getString(R.string.path_conversion_hint), Toast.LENGTH_LONG).show()
                    return null
                } else
                    return uri
            } else {
                //New versions of Android want files to be shared through a content:// Uri and not via a file:// Uri
                //First we convert backslashes to slashes and remove Windows style drive letters and then try to idenitfy the uri corresponding to the path in the bibtex file
                val uri = getUriInLibraryFolder(path)
                if (uri != null) {
                    Log.i(getString(R.string.app_name), "got the following uri for this path:$uri and libraryFolderRootUri=$libraryFolderRootUri")
                    val file = DocumentFile.fromSingleUri(context, uri)
                } else {
                    libraryFolderRootUri = null
                }
                if (uri == null || libraryFolderRootUri == null) {
                    showSetLibraryFolderRootDialog(path)
                    return null
                } else
                    return uri
            }
        }

        internal override fun getModifiedPath(path: String): String {
            return if (android.os.Build.VERSION.SDK_INT < 21)
            //Some versions of Android suffer from this very stupid bug:
            //http://stackoverflow.com/questions/16475317/android-bug-string-substring5-replace-empty-string
                pathPrefixString!! + (if (pathTargetString == "") path else path.replace(pathTargetString!!, pathReplacementString!!))
            else
            //On newer versions of Android we return the unmodified path as finding and opening files is handled in a completely different way...
                path
        }

        override fun onPreBackgroundOperation() {
            bibtexListView!!.visibility = View.GONE
            progressBar!!.visibility = View.VISIBLE
        }

        override fun onPostBackgroundOperation() {
            progressBar!!.visibility = View.GONE
            bibtexListView!!.visibility = View.VISIBLE
        }

        override fun onEntryClick(v: View) {
            hideKeyboard()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu) //Inflates the options menu
            : Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.menu_search)
        searchView = MenuItemCompat.getActionView(searchMenuItem) as android.support.v7.widget.SearchView
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView!!.isIconified = false

        searchView!!.setOnCloseListener {
            searchView!!.isIconified = false//prevent collapsing
            true
        }
        searchView!!.setOnQueryTextListener(this) //Implemented in: public boolean onQueryTextChange(String query) and public boolean onQueryTextSubmit(String query)
        //        searchView.setMaxWidth(Integer.MAX_VALUE);//Makes the overflow menu button disappear on API 23
        if (savedQueryText != null) {
            searchView!!.setQuery(savedQueryText, true)
            savedQueryText = null
        }

        var SelectedSortMenuItem: MenuItem? = null
        when (sortMode) {
            BibtexAdapter.SortMode.None -> SelectedSortMenuItem = menu.findItem(R.id.menu_sort_by_none)
            BibtexAdapter.SortMode.Date -> SelectedSortMenuItem = menu.findItem(R.id.menu_sort_by_date)
            BibtexAdapter.SortMode.Author -> SelectedSortMenuItem = menu.findItem(R.id.menu_sort_by_author)
            BibtexAdapter.SortMode.Journal -> SelectedSortMenuItem = menu.findItem(R.id.menu_sort_by_journal)
            BibtexAdapter.SortMode.Title -> SelectedSortMenuItem = menu.findItem(R.id.menu_sort_by_title)
        }
        if (SelectedSortMenuItem != null)
            SelectedSortMenuItem.isChecked = true

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            val pathConversionMenuItem = menu.findItem(R.id.menu_set_path_conversion)
            if (pathConversionMenuItem != null)
                pathConversionMenuItem.isVisible = false
        }

        this.menu = menu

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) //Handel clicks in the options menu
            : Boolean {
        when (item.itemId) {
            R.id.menu_set_library_path -> {
                showSetLibraryPathDialog()
                return true
            }
            R.id.menu_set_path_conversion -> {
                showSetTargetAndReplacementStringsDialog()
                return true
            }
            R.id.menu_sort_by_none -> {
                sortMode = BibtexAdapter.SortMode.None
                sortInBackground(sortMode)
            }
            R.id.menu_sort_by_date -> {
                sortMode = BibtexAdapter.SortMode.Date
                sortInBackground(sortMode)
            }
            R.id.menu_sort_by_author -> {
                sortMode = BibtexAdapter.SortMode.Author
                sortInBackground(sortMode)
            }
            R.id.menu_sort_by_journal -> {
                sortMode = BibtexAdapter.SortMode.Journal
                sortInBackground(sortMode)
            }
            R.id.menu_sort_by_title -> {
                sortMode = BibtexAdapter.SortMode.Title
                sortInBackground(sortMode)
            }
            R.id.menu_groups -> {
                val intent = Intent(this, GroupsActivity::class.java)
                intent.putExtra("group list", ArrayList(bibtexAdapter!!.groups))
                startActivityForResult(intent, SELECT_GROUP_REQUEST)
                return super.onOptionsItemSelected(item)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        invalidateOptionsMenu()
        return true
    }

    override fun onQueryTextChange(query: String): Boolean { //This is a hacky way to determine when the user has reset the text field with the X button
        if (query.length == 0 && oldQueryText.length > 1) {
            resetFilter()
        }
        oldQueryText = query
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    // @Override
    // public void onBackPressed() //Handles clicks on the back button
    // {
    //     if (!searchView.getQuery().toString().equals(""))
    //         searchView.setQuery("", true);
    //     else
    //         super.onBackPressed();
    // }

    public override fun onNewIntent(intent: Intent) { //Is called when a search is performed
        if (Intent.ACTION_SEARCH == intent.action) {
            filter = intent.getStringExtra(SearchManager.QUERY)
            filterAndSortInBackground(filter, sortMode, group)
        }
        //Unocus the searchView and close the keyboard
        if (searchView != null)
            searchView!!.clearFocus()
        hideKeyboard()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionBar = supportActionBar
        actionBar!!.title = ""
        actionBar.setIcon(null)
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayShowHomeEnabled(false)

        setContentView(R.layout.bibtexlist)
        context = this
        loadGlobalSettings() //Load seetings (uses default if not set)
        //        bibtexAdapter = (LibraryBibtexAdapter) getLastNonConfigurationInstance(); //retreving doesn't work as the on...BackgroundOpertaion() methods lose their references to the Views

        progressBar = findViewById<View>(R.id.progress_bar) as ProgressBar

        if (savedInstanceState != null) {
            savedQueryText = savedInstanceState.getString("SearchQueryText", savedQueryText)
        }
    }

    override fun onResume() {
        super.onResume()

        prepareBibtexListView()
        prepareBibtexAdapter()
    }

    override fun onStop() {
        super.onStop()

        //Write settings
        val globalSettings = getSharedPreferences(GLOBAL_SETTINGS, Context.MODE_PRIVATE)
        val globalSettingsEditor = globalSettings.edit()
        globalSettingsEditor.putBoolean("libraryPreviouslyInitialized", libraryWasPreviouslyInitializedCorrectly)
        globalSettingsEditor.putString("bibtexUrlString", libraryPathString)
        globalSettingsEditor.putString("pathTargetString", pathTargetString)
        globalSettingsEditor.putString("pathReplacementString", pathReplacementString)
        globalSettingsEditor.putString("pathPrefixString", pathPrefixString)
        globalSettingsEditor.putString("uriTargetString", uriTargetString)
        globalSettingsEditor.putString("uriReplacementString", uriReplacementString)
        globalSettingsEditor.putString("uriPrefixString", uriPrefixString)
        globalSettingsEditor.putString("sortMode", sortMode.toString())
        globalSettingsEditor.putString("bibtexFolderRootUri", if (libraryFolderRootUri != null) libraryFolderRootUri!!.toString() else "null")
        globalSettingsEditor.commit()
    }

    // @Override
    // public Object onRetainNonConfigurationInstance() //retainig doesn't work as the on...BackgroundOpertaion() methods lose their references to the Views
    // {
    //     return bibtexAdapter;
    // }

    override fun onSaveInstanceState(outState: Bundle) { //Called when the app is destroyed by the system and in various other cases
        super.onSaveInstanceState(outState)

        var searchQueryText = ""
        if (searchView != null) {
            searchQueryText = searchView!!.query.toString()
            outState.putString("SearchQueryText", searchQueryText)
        }
    }

    fun showSetLibraryPathDialog() //Open a dialoge to set the bibtex library path from user input
    {
        if (setLibraryPathDialog != null && setLibraryPathDialog!!.isShowing)
            return

        val editTextLayout = LinearLayout(context)
        editTextLayout.layoutParams = LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)
        editTextLayout.orientation = 1
        editTextLayout.setPadding(16, 0, 16, 0)
        val input = EditText(this)
        input.setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        input.setText(libraryPathString)
        editTextLayout.addView(input)
        var message = getString(R.string.please_enter_path_of_bibtex_library)
        if (bibtexAdapter == null && libraryWasPreviouslyInitializedCorrectly)
            message = getString(R.string.adapter_failed_to_intialized) + "\n\n" + message
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            /*On newer versions of Android offer to use the file system picker to chose the bibtex library file*/
            val button = Button(context)
            button.text = getString(R.string.pick_bibtex_library)
            button.setOnClickListener {
                if (android.os.Build.VERSION.SDK_INT >= 21)
                    setLibraryFolderRootUri(null)

                val openDocumentIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                openDocumentIntent.addCategory(Intent.CATEGORY_OPENABLE)
                openDocumentIntent.type = "*/*"
                openDocumentIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                startActivityForResult(openDocumentIntent, LIBRARY_FILE_PICK_REQUEST)
            }
            editTextLayout.addView(button)
        }
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder
                .setTitle(getString(R.string.menu_set_library_path))
                .setMessage(message)
                .setView(editTextLayout)
                .setPositiveButton(getString(R.string.save)) { dialog, whichButton ->
                    if (android.os.Build.VERSION.SDK_INT >= 21)
                        setLibraryFolderRootUri(null)
                    setLibraryPathDialog = null
                    setLibraryPath(input.text.toString().trim { it <= ' ' })
                    bibtexAdapter = null
                    prepareBibtexAdapter()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, whichButton ->
                    setLibraryPathDialog = null
                    if (bibtexAdapter == null && prepareBibtexAdapterTask == null)
                        finish()
                }
                .setOnCancelListener {
                    if (bibtexAdapter == null && prepareBibtexAdapterTask == null)
                        finish()
                }
        setLibraryPathDialog = alertDialogBuilder.show()
    }

    fun showSetTargetAndReplacementStringsDialog() //Open a dialoge to set the target and repacement strings from user input
    {
        if (setTargetAndReplacementStringsDialog != null && setTargetAndReplacementStringsDialog!!.isShowing)
            return

        val editTextLayout = LinearLayout(context)
        editTextLayout.layoutParams = LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)
        editTextLayout.orientation = 1
        editTextLayout.setPadding(16, 0, 16, 0)
        val input1 = EditText(this)
        val input2 = EditText(this)
        val input3 = EditText(this)
        input1.setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        input2.setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        input3.setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        input1.setSingleLine()
        input2.setSingleLine()
        input3.setSingleLine()
        input1.setText(pathTargetString)
        input2.setText(pathReplacementString)
        input3.setText(pathPrefixString)
        val view1 = TextView(this)
        view1.text = getString(R.string.target) + ":"
        val view2 = TextView(this)
        view2.text = getString(R.string.replacement) + ":"
        val view3 = TextView(this)
        view3.text = getString(R.string.prefix) + ":"
        editTextLayout.addView(view1)
        editTextLayout.addView(input1)
        editTextLayout.addView(view2)
        editTextLayout.addView(input2)
        editTextLayout.addView(view3)
        editTextLayout.addView(input3)
        val alertDialogBuilder = AlertDialog.Builder(this)
                .setTitle(getString(R.string.menu_set_path_conversion))
                .setMessage(getString(R.string.menu_set_path_conversion_help))
                .setView(editTextLayout)
                .setPositiveButton(getString(R.string.save)) { dialog, whichButton ->
                    val newPathTargetString = input1.text.toString().trim { it <= ' ' }
                    val newPathReplacementString = input2.text.toString().trim { it <= ' ' }
                    val newpathPrefixString = input3.text.toString().trim { it <= ' ' }
                    val globalSettings = getSharedPreferences(GLOBAL_SETTINGS, Context.MODE_PRIVATE)
                    val globalSettingsEditor = globalSettings.edit()
                    globalSettingsEditor.putString("pathTargetString", newPathTargetString)
                    globalSettingsEditor.putString("pathReplacementString", newPathReplacementString)
                    globalSettingsEditor.putString("pathPrefixString", newpathPrefixString)
                    globalSettingsEditor.commit()
                    pathTargetString = newPathTargetString
                    pathReplacementString = newPathReplacementString
                    pathPrefixString = newpathPrefixString

                    setTargetAndReplacementStringsDialog = null
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, whichButton -> setTargetAndReplacementStringsDialog = null }

        setTargetAndReplacementStringsDialog = alertDialogBuilder.show()
    }

    fun showSetLibraryFolderRootDialog(path: String?) {
        if (setLibraryFolderRootUriDialog != null && setLibraryFolderRootUriDialog!!.isShowing)
            return

        var message = getString(R.string.dialog_set_library_root_message)
        if (pathTargetString != "obsolete due to update") {
            message = getString(R.string.dialog_set_library_root_message_addition_on_upgrade) + "\n\n" + message
            pathTargetString = "obsolete due to update"
        }
        val alertDialogBuilder = AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_set_library_root_title))
                .setMessage(message)
                .setPositiveButton(getString(R.string.dialog_set_library_root_select)) { dialog, whichButton ->
                    pathOfFileTrigeredSetLibraryFolderRootDialog = path
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    startActivityForResult(intent, SET_LIBRARY_FOLDER_ROOT_REQUEST)
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, whichButto -> }
        setLibraryFolderRootUriDialog = alertDialogBuilder.show()
    }

    // private void rememberPathOfFileTrigeredSetLibraryFolderRootDialog(String path)
    // {
    //     pathOfFileTrigeredSetLibraryFolderRootDialog = path;
    // }

    private fun analyseLibraryFolderRoot(treeUri: Uri) {
        if (pathOfFileTrigeredSetLibraryFolderRootDialog == null)
            throw RuntimeException("pathOfFileTrigeredSetLibraryFolderRootDialog was null, this should not have happened")

        if (analysingLibraryFolderRootDialog != null && analysingLibraryFolderRootDialog!!.isShowing)
            return

        analyseLibraryFolderRootTask = object : AsyncTask<String, Void, Boolean>() {
            override fun onPreExecute() {
            }

            override fun doInBackground(vararg path0: String): Boolean? {
                val path = convertToLinuxLikePath(path0[0])
                val libraryFolderRootDir = DocumentFile.fromTreeUri(context!!, treeUri)
                var currentDir = libraryFolderRootDir
                var file: DocumentFile? = null
                var relativePath = ""
                for (pathSegment in path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {

                    file = currentDir!!.findFile(pathSegment)
                    if (file != null) {
                        //Log.i(getString(R.string.app_name), "found "+pathSegment);
                        currentDir = file
                        if (relativePath != "")
                            relativePath += "/"
                        relativePath += pathSegment
                    } else {
                        //Log.i(getString(R.string.app_name), "couldn't find "+pathSegment+" in "+currentDir.getUri().toString());
                        relativePath = ""
                    }
                }
                if (file != null) {
                    val fileUriString = file.uri.toString()
                    uriTargetString = path.substring(0, path.lastIndexOf(relativePath))
                    uriReplacementString = ""
                    //                        uriPrefixString = (relativePath.equals("") ? fileUriString : fileUriString.replaceLast(Uri.encode(relativePath), ""));
                    uriPrefixString = fileUriString.substring(0, fileUriString.lastIndexOf(Uri.encode(relativePath)))
                    return true
                } else
                    return false
            }

            override fun onPostExecute(succees: Boolean?) {
                if (succees!!) {
                    val path = pathOfFileTrigeredSetLibraryFolderRootDialog
                    setLibraryFolderRootUri(treeUri)
                    val globalSettings = getSharedPreferences(GLOBAL_SETTINGS, Context.MODE_PRIVATE)
                    val globalSettingsEditor = globalSettings.edit()
                    globalSettingsEditor.putString("uriTargetString", uriTargetString)
                    globalSettingsEditor.putString("uriReplacementString", uriReplacementString)
                    globalSettingsEditor.putString("uriPrefixString", uriPrefixString)
                    if (analysingLibraryFolderRootDialog != null) {
                        //analysingLibraryFolderRootDialog.cancel();
                        analysingLibraryFolderRootDialog!!.setMessage(String.format(getString(R.string.dialog_analyse_library_root_message_success), pathOfFileTrigeredSetLibraryFolderRootDialog, libraryFolderRootUri!!.toString(), getUriInLibraryFolder(pathOfFileTrigeredSetLibraryFolderRootDialog)!!.toString()))
                        analysingLibraryFolderRootDialog!!.setTitle(getString(R.string.dialog_analyse_library_root_title_success))
                        analysingLibraryFolderRootDialog!!.getButton(android.content.DialogInterface.BUTTON_NEGATIVE).setText(R.string.open)
                        analysingLibraryFolderRootDialog!!.setButton(android.content.DialogInterface.BUTTON_NEGATIVE, getString(R.string.open)) { dialog, whichButton ->
                            if (bibtexAdapter != null)
                                bibtexAdapter!!.openExternally(context, getUriInLibraryFolder(path))
                        }
                        analysingLibraryFolderRootDialog = null
                    }
                } else {
                    setLibraryFolderRootUri(null)
                    if (analysingLibraryFolderRootDialog != null) {
                        //analysingLibraryFolderRootDialog.cancel();
                        analysingLibraryFolderRootDialog!!.setMessage(String.format(getString(R.string.dialog_analyse_library_root_message_failed), pathOfFileTrigeredSetLibraryFolderRootDialog, treeUri.toString()))
                        analysingLibraryFolderRootDialog!!.setTitle(getString(R.string.dialog_analyse_library_root_title_failed))
                        analysingLibraryFolderRootDialog = null
                    }
                }
                pathOfFileTrigeredSetLibraryFolderRootDialog = null
            }
            // @Override
            // protected void onPostExecute(Boolean succees) {

            // }
        }

        val alertDialogBuilder = AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_analyse_library_root_title))
                .setMessage(getString(R.string.dialog_analyse_library_root_message))
                .setNegativeButton(getString(R.string.cancel)) { dialog, whichButton ->
                    if (analyseLibraryFolderRootTask != null) {
                        analyseLibraryFolderRootTask!!.cancel(false)
                        analyseLibraryFolderRootTask = null
                    }
                }
                .setOnCancelListener {
                    if (analyseLibraryFolderRootTask != null) {
                        analyseLibraryFolderRootTask!!.cancel(false)
                        analyseLibraryFolderRootTask = null
                    }
                }
        analysingLibraryFolderRootDialog = alertDialogBuilder.show()
        analyseLibraryFolderRootTask!!.execute(pathOfFileTrigeredSetLibraryFolderRootDialog)
    }

    internal fun getUriInLibraryFolder(path: String?): Uri? {
        var path = path
        path = convertToLinuxLikePath(path)
        //Some versions of Android suffer from this very stupid bug:
        //http://stackoverflow.com/questions/16475317/android-bug-string-substring5-replace-empty-string
        return if (uriPrefixString == null || path == null)
            null
        else
            Uri.parse(uriPrefixString!! + Uri.encode(if (uriTargetString == null || uriTargetString == "") path else path.replaceFirst(Pattern.quote(uriTargetString).toRegex(), uriReplacementString!!)))
    }

    private fun loadGlobalSettings() {
        val globalSettings = getSharedPreferences(GLOBAL_SETTINGS, Context.MODE_PRIVATE)
        libraryWasPreviouslyInitializedCorrectly = globalSettings.getBoolean("libraryPreviouslyInitialized", libraryWasPreviouslyInitializedCorrectly)
        libraryPathString = globalSettings.getString("bibtexUrlString", libraryPathString)
        pathTargetString = globalSettings.getString("pathTargetString", pathTargetString)
        pathReplacementString = globalSettings.getString("pathReplacementString", pathReplacementString)
        pathPrefixString = globalSettings.getString("pathPrefixString", pathPrefixString)
        uriTargetString = globalSettings.getString("uriTargetString", uriTargetString)
        uriReplacementString = globalSettings.getString("uriReplacementString", uriReplacementString)
        uriPrefixString = globalSettings.getString("uriPrefixString", uriPrefixString)
        sortMode = BibtexAdapter.SortMode.valueOf(globalSettings.getString("sortMode", "None"))
        val libraryFolderRootUriString = globalSettings.getString("bibtexFolderRootUri", if (libraryFolderRootUri != null) libraryFolderRootUri!!.toString() else "")
        if (libraryFolderRootUriString == null || libraryFolderRootUriString == "null")
            libraryFolderRootUri = null
        else
            libraryFolderRootUri = Uri.parse(libraryFolderRootUriString)
    }

    private fun prepareBibtexListView() {
        bibtexListView = findViewById<View>(R.id.bibtex_list_view) as ListView
    }

    private fun prepareBibtexAdapter() {
        //If we already have an adapter, throw it away if if the file has changed since we last opend it
        if (bibtexAdapter != null) {
            val globalSettings = getSharedPreferences(GLOBAL_SETTINGS, Context.MODE_PRIVATE)
            val lastModifyDate = globalSettings.getLong("libraryFileLastModifyDate", 0)
            if (libraryPathString != null) {
                val libraryUri = Uri.parse(libraryPathString)
                if (libraryUri != null) {
                    val libraryFile = File(Uri.decode(libraryUri.encodedPath))
                    if (libraryFile != null) {
                        val globalSettingsEditor = globalSettings.edit()
                        globalSettingsEditor.putLong("libraryFileLastModifyDate", libraryFile.lastModified())
                        globalSettingsEditor.apply()
                        if (libraryFile.lastModified() != lastModifyDate)
                            bibtexAdapter = null
                    }
                }
            }
        }

        if (prepareBibtexAdapterTask != null)
            prepareBibtexAdapterTask!!.cancel(true)

        prepareBibtexAdapterTask = object : AsyncTask<String, Void, Void>() {
            override fun onPreExecute() {
                if (bibtexListView != null)
                    bibtexListView!!.visibility = View.GONE
                if (progressBar != null)
                    progressBar!!.visibility = View.VISIBLE
            }

            override fun doInBackground(vararg libraryPathString: String): Void? {
                if (bibtexAdapter == null) {
                    var inputStream: InputStream? = null
                    try {
                        val libraryUri = Uri.parse(libraryPathString[0])
                        val libraryFile = File(Uri.decode(libraryUri.encodedPath))

                        if (libraryFile != null && libraryFile.isFile) {
                            inputStream = FileInputStream(libraryFile)
                        }
                        if (inputStream == null && libraryUri.toString().startsWith("content://")) {
                            inputStream = context!!.contentResolver.openInputStream(libraryUri)
                        }

                        bibtexAdapter = LibraryBibtexAdapter(context!!, inputStream!!)
                    } catch (e: Exception) {
                        Log.e(getString(R.string.app_name), getString(R.string.exception_while_loading_library) + e.message, e)
                        bibtexAdapter = null
                    } finally {
                        prepareBibtexAdapterTask = null
                        if (inputStream != null) {
                            try {
                                inputStream.close()
                            } catch (e: java.io.IOException) {
                                //Nothing we can do
                            }
                        }
                    }
                }
                return null
            }

            override fun onPostExecute(v: Void?) {
                if (bibtexAdapter != null) {
                    libraryWasPreviouslyInitializedCorrectly = true
                    //Bind the Adapter to the UI and update
                    bibtexListView!!.adapter = bibtexAdapter
                    bibtexAdapter!!.notifyDataSetChanged()
                    bibtexAdapter!!.onPostBackgroundOperation()

                    filterAndSortInBackground(filter, sortMode, group)
                    bibtexAdapter!!.prepareForFiltering()

                    if (bibtexAdapter!!.groups.isEmpty() && menu != null) {
                        menu!!.findItem(R.id.menu_groups).isVisible = false
                    }
                    if (!bibtexAdapter!!.groups.isEmpty() && menu != null) {
                        menu!!.findItem(R.id.menu_groups).isVisible = true
                    }
                } else {
                    showSetLibraryPathDialog()
                }
            }
        }
        prepareBibtexAdapterTask!!.execute(libraryPathString)
    }

    private fun resetFilter() {
        if (bibtexAdapter != null)
            bibtexAdapter!!.filterAndSortInBackground("", sortMode, group)
        filter = ""
    }

    private fun sortInBackground(sortMode: BibtexAdapter.SortMode) {
        if (bibtexAdapter != null)
            bibtexAdapter!!.sortInBackground(sortMode)
    }

    private fun filterAndSortInBackground(filter: String, sortMode: BibtexAdapter.SortMode, group: String) {
        if (bibtexAdapter != null) {
            bibtexAdapter!!.filterAndSortInBackground(filter, sortMode, group)
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager != null && currentFocus != null)
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            LIBRARY_FILE_PICK_REQUEST -> if (resultCode == Activity.RESULT_OK) {
                if (intent != null && intent.data != null) {
                    if (setLibraryPathDialog != null) {
                        setLibraryPathDialog!!.dismiss()
                        setLibraryPathDialog = null
                    }
                    setLibraryPath(intent.data!!.toString())

                    if (android.os.Build.VERSION.SDK_INT >= 19) {
                        try {
                            contentResolver.takePersistableUriPermission(intent.data!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        } catch (e: Exception) {
                            //Nothing we can do if we don't get the permission
                        }
                    }

                    bibtexAdapter = null
                    prepareBibtexAdapter()
                }
                if (android.os.Build.VERSION.SDK_INT >= 21 && (android.support.v4.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || android.support.v4.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_PERMISSION_REQUEST)
                }
            }
            SET_LIBRARY_FOLDER_ROOT_REQUEST -> if (resultCode == Activity.RESULT_OK) {
                val treeUri = intent!!.data
                grantUriPermission(packageName, treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)//Not sure this is necessary
                contentResolver.takePersistableUriPermission(treeUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                analyseLibraryFolderRoot(treeUri)
            }
            SELECT_GROUP_REQUEST -> if (resultCode == Activity.RESULT_OK) {
                group = intent!!.getStringExtra("group")
                filterAndSortInBackground(filter, sortMode, group)
                val group_titlebar = findViewById<View>(R.id.group_titlebar) as TextView

                if (bibtexAdapter!!.groups.contains(group)) {
                    group_titlebar.text = group
                    group_titlebar.visibility = View.VISIBLE
                } else {
                    group_titlebar.text = ""
                    group_titlebar.visibility = View.GONE
                }
            }
        }
    }

    internal fun setLibraryPath(newLibraryPathString: String) {
        val globalSettings = getSharedPreferences(GLOBAL_SETTINGS, Context.MODE_PRIVATE)
        val globalSettingsEditor = globalSettings.edit()
        globalSettingsEditor.putString("bibtexUrlString", newLibraryPathString)
        globalSettingsEditor.commit()
        libraryPathString = newLibraryPathString
    }

    internal fun setLibraryFolderRootUri(newLibraryFolderRootUri: Uri?) {
        val globalSettings = getSharedPreferences(GLOBAL_SETTINGS, Context.MODE_PRIVATE)
        val globalSettingsEditor = globalSettings.edit()
        globalSettingsEditor.putString("bibtexFolderRootUri", newLibraryFolderRootUri?.toString() ?: "null")
        globalSettingsEditor.commit()
        libraryFolderRootUri = newLibraryFolderRootUri
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            /*We just resume irrespective of whether the permission was
                 * granted and then handle cases where we can not access a
                 * file on a per case basis.
                 * Addendum: We should be able to simply resume here, but
                 * due to a bug in Android we have to kill the current process
                 * because we only actually get the permission after the app
                 * is restarted from scratch.
                 * */
            //onResume();
            var anyResultPositive: Boolean? = false
            for (result in grantResults)
                if (result == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    anyResultPositive = true
                    break
                }

            if (anyResultPositive!!) {
                val alertDialogBuilder = AlertDialog.Builder(this)
                val alert = alertDialogBuilder.create()
                alert.setTitle(R.string.dialog_newpermissions_title)
                alert.setMessage(resources.getString(R.string.dialog_newpermissions_message))
                alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_newpermissions_ok)
                ) { dialog, which -> }
                alert.setOnDismissListener { android.os.Process.killProcess(android.os.Process.myPid()) }
                alert.show()
            }
        }
    }

    internal fun convertToLinuxLikePath(path: String?): String {
        var path = path
        path = path!!.replace("\\", "/")
        if (path.indexOf(":") >= 0)
            path = path.substring(path.indexOf(":") + 1)
        return path
    }

    companion object {

        val GLOBAL_SETTINGS = "global settings"
        val LIBRARY_FILE_PICK_REQUEST = 0
        val WRITE_PERMISSION_REQUEST = 1
        val SET_LIBRARY_FOLDER_ROOT_REQUEST = 2
        val SELECT_GROUP_REQUEST = 3
    }
}
