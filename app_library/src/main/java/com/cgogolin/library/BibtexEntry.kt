package com.cgogolin.library

import android.content.Context
import java.util.*
import java.util.regex.Pattern

class BibtexEntry : BaseBibtexEntry() {

    private var extraInfoVisible = false
    val eprintFormated: String
        get() = if (arxivId != "")
            if (archivePrefix != "" && !arxivId!!.startsWith(archivePrefix!!))
                "$archivePrefix:$arxivId"
            else
                arxivId!!
        else if (eprint != "")
            if (archivePrefix != "" && !eprint!!.startsWith(archivePrefix!!))
                "$archivePrefix:$eprint"
            else
                eprint!!
        else
            ""
    val dateFormated: String
        get() {
            val year = year
            val month = monthNumeric
            val day = day
            return if (month == "")
                year!!
            else if (day == "")
                "$year-$month"
            else
                "$year-$month-$day"
        }
    val numberInFile: String
        get() = saveGet("numberInFile")!!

    fun getFilesFormated(context: Context): String {
        val associatedFilesList = files
        var filesString = ""
        if (associatedFilesList != null) {
            filesString = context.getString(R.string.files) + ": "
            for (file in associatedFilesList) {
                filesString = "$filesString$file "
            }
        }
        return filesString.trim { it <= ' ' }
    }

    fun getUrls(context: Context): List<String>? {
        var url = url
        if (url == "")
            url = howpublished
        var eprint = if (arxivId == "") eprint else arxivId
        if (url == "" && eprint == "") return null
        val urls = ArrayList<String>()
        if (url != "") urls.add(url!!)
        if (eprint != "") {
            eprint = context.getString(R.string.arxiv_url_prefix) + eprint
            if (eprint != url)
                urls.add(eprint)
        }
        return urls
    }

    fun getDoiLinks(context: Context): List<String>? {
        val doi = doi
        if (doi == "") return null
        val dois = ArrayList<String>()
        dois.add(context.getString(R.string.doi_url_prefix) + doi)
        return dois
    }

    fun getDoiFormated(context: Context): String {
        return if (doi != "")
            context.getString(R.string.doi) + ": " + doi
        else
            ""
    }

    fun getAuthorsFormated(context: Context): String {
        //use precompiled regex
        val authors = authorAndPattern.split(author)
        val editors = authorAndPattern.split(editor)
        var authorsString = ""
        var firstAuthor = true
        for (author in authors) {
            if (author.trim { it <= ' ' } == "") break
            if (!firstAuthor)
                authorsString += ", "
            else
                firstAuthor = false
            //apply regex twice to take care of "Last, Jr ,First" cases
            //use precompiled regex
            authorsString += authorCommaPattern.matcher(authorCommaPattern.matcher(author.trim { it <= ' ' }).replaceAll("$2 $1").trim { it <= ' ' }).replaceAll("$2 $1").trim { it <= ' ' }
        }
        var firstEditor = true
        for (author in editors) {
            if (author.trim { it <= ' ' } == "") break
            if (!firstEditor)
                authorsString += ", "
            else {
                authorsString += " " + context.getString(R.string.edited_by) + " "
                firstEditor = false
            }
            //apply regex twice to take care of "Last, Jr ,First" cases
            //use precompiled regex
            authorsString += authorCommaPattern.matcher(authorCommaPattern.matcher(author.trim { it <= ' ' }).replaceAll("$2 $1").trim { it <= ' ' }).replaceAll("$2 $1").trim { it <= ' ' }
        }
        return authorsString.trim { it <= ' ' }
    }

    fun getJournalFormated(context: Context): String {
        var jounnal = journal
        if (volume != "")
            jounnal += " " + context.getString(R.string.vol) + " " + volume
        if (number != "")
            jounnal += " " + context.getString(R.string.num) + " " + number
        if (pages != "")
            jounnal += " " + context.getString(R.string.page) + " " + pages
        if (year != "" || month != "") {
            jounnal += " ("
            if (month != "")
                jounnal += "$month "
            jounnal += "$year)"
        }
        if (jounnal != "")
            jounnal += "."
        return jounnal!!
    }

    fun extraInfoVisible(): Boolean {
        return extraInfoVisible
    }

    fun setExtraInfoVisible(extraInfoVisible: Boolean) {
        this.extraInfoVisible = extraInfoVisible
    }

    companion object {
        //For performance reasons, compile regexps once:
        private val authorCommaPattern = Pattern.compile(" *([^,]*) *,? *(.*) *")
        private val authorAndPattern = Pattern.compile(" and ")
    }
}
