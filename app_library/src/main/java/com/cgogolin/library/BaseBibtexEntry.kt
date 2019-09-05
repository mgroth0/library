package com.cgogolin.library

import java.util.HashMap
import java.util.ArrayList
import java.util.Arrays

open class BaseBibtexEntry {

    val entryMap: HashMap<String, String> = HashMap()
    private val latexPrettyPrinterEntryMap: HashMap<String, String> = HashMap()

    val label: String?
        get() = saveGet("label")
    val documentType: String?
        get() = saveGet("documenttyp")
    val group: String?
        get() = saveGet("groups")
    //We assume the following format:
    //{group1, group2...}
    val groups: List<String>?
        get() {
            if (group == "") return null
            val rawGroupString = group!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in rawGroupString.indices) {
                rawGroupString[i] = rawGroupString[i].trim { it <= ' ' }
            }
            return ArrayList(Arrays.asList(*rawGroupString))
        }
    val file: String?
        get() = saveGet("file")
    //We assume the either of the following formats:
    //{:path1/file1.end1:end1;:path2/file2.end1:end2;...}
    //{path1/file1.end1:end1;path2/file2.end1:end2;...}
    //{path1/file1.end1;path2/file2.end1;...}
    //{:path1\file1.end1:end1;:path2\file2.end1:end2;...}
    //{path1\file1.end1:end1;path2\file2.end1:end2;...}
    //{path1\file1.end1;path2\file2.end1;...}
    //whereby path can contains Windows drive letters such as 'c:\'.
    //Furthermore we assume that '\_' is an escape sequence for '_'.
    val files: List<String>?
        get() {
            if (file == "") return null
            val rawFileString = file!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in rawFileString.indices) {
                val start = rawFileString[i].indexOf(':') + 1
                val end = if (rawFileString[i].lastIndexOf(':') != rawFileString[i].indexOf(':')) rawFileString[i].lastIndexOf(':') else rawFileString[i].length
                rawFileString[i] = rawFileString[i].substring(start, end).replace("\\_", "_")
            }
            return ArrayList(Arrays.asList(*rawFileString))
        }
    val url: String?
        get() = saveGet("url")
    val doi: String?
        get() = saveGet("doi")
    val archivePrefix: String?
        get() = saveGet("archiveprefix")
    val arxivId: String?
        get() = saveGet("arxivid")
    val entryAsString: String
        get() {
            var output = "@$documentType{$label"
            for (key in entryMap.keys)
                output += ",\n" + key + " = {" + entryMap[key] + "}"
            output += "\n}"
            return output
        }
    val stringBlob: String?
        @Synchronized get() {
            if (!latexPrettyPrinterEntryMap.containsKey("stringblob")) generateStringBlob()
            return latexPrettyPrinterEntryMap["stringblob"]
        }
    val authorSortKey: String?
        @Synchronized get() {
            if (!entryMap.containsKey("authorSortKey")) generateAuthorSortKey()
            return saveGet("authorSortKey")
        }
    // public synchronized String getAuthorLast() {
    //     if (!entryMap.containsKey("authorLast")) generateAuthorSortKey();
    //     return saveGet("authorLast");
    // }
    // public synchronized String getAuthorFirst() {
    //     if (!entryMap.containsKey("authorFirst")) generateAuthorSortKey();
    //     return saveGet("authorFirst");
    // }
    // public synchronized String getAuthorJR() {
    //     if (!entryMap.containsKey("authorJR")) generateAuthorSortKey();
    //     return saveGet("authorJR");
    // }
    val author: String?
        get() = saveGetPretty("author")
    val rawAuthor: String?
        get() = saveGet("author")
    val editor: String?
        get() = saveGetPretty("editor")
    val eprint: String?
        get() = saveGet("eprint")
    val primaryclass: String?
        get() = saveGet("primaryclass")
    val howpublished: String?
        get() = saveGet("howpublished")
    val journal: String?
        get() = saveGetPretty("journal")
    val number: String?
        get() = saveGet("number")
    val pages: String?
        get() = saveGetPretty("pages")
    val title: String?
        get() = saveGetPretty("title")
    val volume: String?
        get() = saveGet("volume")
    val day: String
        get() {
            val day = saveGet("day")
            return if (day!!.length == 2)
                day
            else if (day.length == 1)
                "0$day"
            else
                ""
        }
    val month: String?
        get() = saveGet("month")
    val monthNumeric: String
        get() {
            val month = month!!.trim { it <= ' ' }.replace("[^0-9a-zA-z]".toRegex(), "").toLowerCase()
            if (month == "jan") return "01"
            if (month == "feb") return "02"
            if (month == "mar") return "03"
            if (month == "apr") return "04"
            if (month == "may") return "05"
            if (month == "jun") return "06"
            if (month == "jul") return "07"
            if (month == "aug") return "08"
            if (month == "sep") return "09"
            if (month == "oct") return "10"
            if (month == "nov") return "11"
            return if (month == "dec") "12" else ""
        }
    val year: String?
        get() = saveGet("year")

    fun put(name: String, value: String) {
        entryMap[name] = value
    }

    protected fun saveGet(name: String): String? {
        return if (entryMap.containsKey(name)) entryMap[name] else ""
    }

    private fun saveGetPretty(name: String): String? {
        if (!entryMap.containsKey(name))
            return ""
        if (!latexPrettyPrinterEntryMap.containsKey(name))
            latexPrettyPrinterEntryMap[name] = LatexPrettyPrinter.parse(entryMap[name]!!)
        return latexPrettyPrinterEntryMap[name]
    }

    //Functions above output raw values, functions below use the LaTeX pretty printer
    private fun generateStringBlob() {
        var blob = ""
        for (key in arrayOf("label", "documenttyp", "author", "editor", "eprint", "primaryclass", "doi", "journal", "number", "pages", "title", "volume", "month", "year", "archiveprefix", "arxivid", "keywords", "mendeley-tags", "url")) {
            if (entryMap.containsKey(key))
                blob = blob + "" + key + "=" + entryMap[key] + " "
        }
        blob = blob + " " + LatexPrettyPrinter.parse(blob)
        latexPrettyPrinterEntryMap["stringblob"] = blob
    }

    fun generateAuthorSortKey() {
        val authors = rawAuthor!!.split(" and ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val rawAuthorString = authors[0] //We only sort according to the first authors, which should be enugh for all practical puposes

        // Based on explanations from Tame the BeaST: http://tug.ctan.org/info/bibtex/tamethebeast/ttb_en.pdf
        if (rawAuthorString.isEmpty()) return

        val authorParts = arrayOf("", "", "", "")
        val FIRST = 0 //a few constants
        val VON = 1
        val LAST = 2
        val JR = 3

        var word = false
        var depth = 0 //brace depth
        var specialChar = 0 //brace depth inside special character

        // indices of commas in brace depth 0 (up to two commas)
        val commaInd = ArrayList<Int>()
        // indices of spaces in brace depth 0
        val spaceInd = ArrayList<Int>()
        // indices of the first letter of words
        val newWordInd = ArrayList<Int>()

        val depthArray = IntArray(rawAuthorString.length) // brace depth of each character
        val specialCharArray = IntArray(rawAuthorString.length) // special-charachter brace depth
        MAP_CHARS_TAG@ for (i in 0 until rawAuthorString.length) {
            when (rawAuthorString[i]) {
                '{' // either deeper brace depth of deeper special-char depth
                -> if (specialChar > 0)
                    specialChar++
                else if (depth == 0 && i + 1 < rawAuthorString.length && rawAuthorString[i + 1] == '\\')
                    specialChar = 1
                else
                    depth++
                ',' // add to comma list only if depth 0
                -> if (depth == 0 && specialChar == 0) {
                    commaInd.add(i)
                    if (commaInd.size == 2)
                        break@MAP_CHARS_TAG // don't care what happens after 2 commas
                }
                '}' -> if (specialChar > 0)
                    specialChar--
                else {
                    depth--
                    if (depth < 0) return  // throw an error? log warning?
                }
                ' ' // add to space list only if depth zero, and before first comma
                ->
                    //case '-': // count as space
                    //case '~': // count as space
                    if (depth == 0 && specialChar == 0 && commaInd.size == 0) {
                        spaceInd.add(i)
                        word = false
                    }
                else ->
                    // look for first letter of words
                    if (!word && Character.isLetter(rawAuthorString[i]) && commaInd.size == 0) {
                        newWordInd.add(i)
                        word = true
                    }
            }
            specialCharArray[i] = specialChar
            if (specialChar == 0)
                depthArray[i] = depth
            else
            //inside special character depth always zero
                depthArray[i] = 0
        }

        var part = -1
        var firstStart = 0
        var firstEnd = 0
        var vonStart = 0
        var vonEnd = 0
        var lastStart = 0
        var lastEnd = 0
        var jrStart = 0
        var jrEnd = 0

        when (commaInd.size) {
            2 -> {
                firstStart = commaInd[1] + 1
                firstEnd = rawAuthorString.length
                jrStart = commaInd[0] + 1
                jrEnd = commaInd[1]
                lastEnd = commaInd[0] - 1
                part = LAST
            }
            1 -> {
                firstStart = commaInd[0] + 1
                firstEnd = rawAuthorString.length
                lastEnd = commaInd[0] - 1
                part = LAST
            }
            0 -> lastEnd = rawAuthorString.length
        }
        var thisChar = '\u0000'
        var wordInd = 0
        for (i in 0 until newWordInd.size - 1) {
            wordInd = newWordInd[i]

            thisChar = rawAuthorString[wordInd]
            depth = depthArray[wordInd]
            if (depth == 0) {
                if (Character.isUpperCase(thisChar)) {
                    when (part) {
                        -1, FIRST -> part = FIRST
                        VON -> {
                            vonEnd = spaceInd[if (i > 0) i - 1 else 0]
                            part = LAST
                        }
                        LAST -> part = LAST
                    }
                } else {
                    when (part) {
                        -1 -> {
                            vonEnd = spaceInd[i]
                            part = VON
                        }
                        FIRST -> {
                            firstEnd = spaceInd[i - 1]
                            vonStart = spaceInd[i - 1]
                            vonEnd = spaceInd[i]
                            part = VON
                        }
                        VON -> vonEnd = spaceInd[i]
                        LAST -> {
                            vonEnd = spaceInd[i]
                            part = VON
                        }
                    }
                }
            }
        }
        if (part == FIRST) {
            firstEnd = if (newWordInd.size > 1) spaceInd[newWordInd.size - 2] else 0
            lastStart = firstEnd
        } else
            lastStart = vonEnd

        authorParts[FIRST] = rawAuthorString.substring(firstStart, firstEnd).trim { it <= ' ' }
        authorParts[VON] = rawAuthorString.substring(vonStart, vonEnd).trim { it <= ' ' }
        authorParts[LAST] = rawAuthorString.substring(lastStart, lastEnd).trim { it <= ' ' }
        authorParts[JR] = rawAuthorString.substring(jrStart, jrEnd).trim { it <= ' ' }
        var authorSortKey = authorParts[LAST] + " " + authorParts[FIRST] + " " + authorParts[JR]

        authorSortKey = authorSortKey.toUpperCase()
        put("authorSortKey", LatexPrettyPrinter.parse(authorSortKey))

        //In case these are ever needed individually we can save them here and get them with the methors below (if this is activated this functino should be renamed)
        // put("authorLast", authorParts[LAST]);
        // put("authorFirst", authorParts[FIRST]);
        // put("authorJR", authorParts[JR]);
    }
}
