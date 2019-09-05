package com.cgogolin.library

import java.util.ArrayList
import java.util.Arrays

import android.util.Log

import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader

object BibtexParser {
    @Throws(java.io.IOException::class)
    fun parse(inputStream: InputStream): ArrayList<BibtexEntry> {
        val BibtexEntryList = ArrayList<BibtexEntry>()

        //Set up buffered input
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        var line: String? = null
        var i = 1
        val eofReached = false
        var buffer = ""

        //Go through the file
        SEARCH_FOR_ENTRY@ while (true) {
            //If we have not yet found an '@' continue reading
            if (buffer.indexOf('@') == -1) {
                //Break if we reach the end of the file, otherwise add the line to the buffer
                val line = bufferedReader.readLine() ?: break@SEARCH_FOR_ENTRY
                buffer += line.trim { it <= ' ' }
            }
            if (buffer.indexOf('@') == -1) continue@SEARCH_FOR_ENTRY

            //Now we have an '@', so start processing the bibtex entry by
            //throwing away everyting before and including the first '@' and triming whitespaces
            buffer = buffer.substring(buffer.indexOf('@') + 1).trim { it <= ' ' }

            //Continue reading lines until we have a '{' and a ',' in the buffer so that we know
            //we have the documenttype of the entry and the label in the buffer or we reach the end of the file
            while (buffer.indexOf('{') == -1 || buffer.indexOf(',') == -1) {
                val line = bufferedReader.readLine() ?: break@SEARCH_FOR_ENTRY
                buffer += line.trim { it <= ' ' }
            }

            val documentTyp = buffer.substring(0, buffer.indexOf('{')).trim { it <= ' ' }.toLowerCase()
            val label = buffer.substring(buffer.indexOf('{') + 1, buffer.indexOf(',', buffer.indexOf('{'))).trim { it <= ' ' }

            //Create a new BibtexEntry
            val entry = BibtexEntry()
            entry.put("numberInFile", Integer.toString(i))
            i++
            entry.put("documenttyp", documentTyp)
            entry.put("label", label)

            //Discard the type and the label
            buffer = buffer.substring(buffer.indexOf(',') + 1).trim { it <= ' ' }

            //int j = 0;
            SEARCH_FOR_TAG@ while (true) {
                //If we have not yet found an '=' or a '}' continue reading
                if (buffer.indexOf('=') == -1 && buffer.indexOf('}') == -1) {
                    //Break if we reach the end of the file, otherwise add the line to the buffer
                    val line = bufferedReader.readLine() ?: break@SEARCH_FOR_ENTRY
                    buffer += line.trim { it <= ' ' }
                }
                if (buffer.indexOf('=') == -1 && buffer.indexOf('}') == -1) continue@SEARCH_FOR_TAG

                //Break if we have rached the end of the entry, i.e. there is a '}' before the next '='
                if (buffer.indexOf('}') != -1 && (buffer.indexOf('=') == -1 || buffer.indexOf('}') < buffer.indexOf('='))) {

                    //Throw away the rest of this entry and break
                    if (buffer.indexOf('}') + 1 < buffer.length) {
                        buffer = buffer.substring(buffer.indexOf('}') + 1)
                    } else {
                        buffer = ""
                    }
                    //We have found all tags of this entry so stop searching for more
                    break@SEARCH_FOR_TAG
                }

                //Now we have an '=', so start processing the bibtex tag

                //Everything before the first '=' is the name of the tag
                val name = buffer.substring(0, buffer.indexOf('=')).trim { it <= ' ' }.toLowerCase()

                //If name contains whitespaces we have a malformed tag name and thus break and serch for a new entry
                if (name !== name.replace(" ", "")) break@SEARCH_FOR_TAG

                //Now we extract the value
                var value = ""

                //Discard the name and the '=' from the buffer
                buffer = buffer.substring(buffer.indexOf('=') + 1).trim { it <= ' ' }

                //Treat the value of the tag differentyl depending on the delimiter
                val Delimiter1 = buffer[0]

                when (Delimiter1) {
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' //It is a number which has no delimiter
                    -> {
                        //Continue reading lines until we find the first possible delimiter character
                        while (buffer.indexOf(' ') == -1 && buffer.indexOf('}') == -1 && buffer.indexOf(',') == -1 && buffer.indexOf('\"') == -1) {
                            val line = bufferedReader.readLine() ?: break@SEARCH_FOR_ENTRY
                            buffer += line.trim { it <= ' ' }
                        }
                        //Now that we have at least one non digit character in the buffer we intepret the first non interrupted sequence of numbers as the value of the tag
                        var lengthOfNumber = 0
                        while (Character.isDigit(buffer[lengthOfNumber])) lengthOfNumber++
                        //Copy the value and remove it from the buffer
                        value = buffer.substring(0, lengthOfNumber)
                        buffer = buffer.substring(lengthOfNumber).trim { it <= ' ' }
                    }
                    '{', '\"' -> {
                        var lengthOfValue = 0
                        //Determine what the closing delimiter is
                        val Delimiter2 = if (Delimiter1 == '{') '}' else '\"'
                        //Discard the opening delimiter and put it into value
                        buffer = buffer.substring(1)
                        value = Character.toString(Delimiter1)
                        //Find the closing delimiter of the value
                        do {
                            //Continue reading until we find the first unescaped closing delemiter
                            while (buffer.replace("\\\\", "").replace("\\" + Delimiter2, "").indexOf(Delimiter2.toInt().toChar()) == -1) {
                                val line = bufferedReader.readLine()
                                if ((line) == null) break@SEARCH_FOR_ENTRY
                                buffer += line.trim { it <= ' ' }
                            }
                            //Find the position of the first unescaped closing delemiter
                            lengthOfValue = buffer.replace("\\\\", "__").replace("\\" + Delimiter2, "__").indexOf(Delimiter2.toInt().toChar())

                            //Copy the everything before and including the closing delimiter into value and remove it from the buffer
                            value = value + buffer.substring(0, lengthOfValue + 1) //Closing delimiter is put into value
                            buffer = buffer.substring(lengthOfValue + 1) //Closing delimiter not left in buffer
                        } while (value.replace("\\\\", "__").replace("\\" + Delimiter1, "__").replace("\\" + Delimiter2, "__").replace(Character.toString(Delimiter1), "").length != value.replace("\\\\", "__").replace("\\" + Delimiter1, "__").replace("\\" + Delimiter2, "__").replace(Character.toString(Delimiter2), "").length) //While value is not "balanced"
                    }
                    else //Try to be nice and also read non bibtex conform values assuming that the value consist of exactly one word
                    -> {
                        //Continue reading lines until we find the first possible delimiter character
                        while (buffer.indexOf(' ') == -1 && buffer.indexOf('}') == -1 && buffer.indexOf(',') == -1 && buffer.indexOf('\"') == -1) {
                            val line = bufferedReader.readLine()
                            if ((line) == null) break@SEARCH_FOR_ENTRY
                            buffer += line!!.trim { it <= ' ' }
                        }
                        //Now that we have at least one non letter character in the buffer we intepret the first non interrupted sequence of letters as the value of the tag
                        var lengthOfWord = 0
                        while (Character.isLetter(buffer[lengthOfWord])) lengthOfWord++
                        //Copy the value and remove it from the buffer
                        value = buffer.substring(0, lengthOfWord)
                        buffer = buffer.substring(lengthOfWord).trim { it <= ' ' }

                        //Remove everything until the next ',', '}' or '@' in an attempt to read the rest of the entry and file
                        while (buffer.indexOf(',') == -1 && buffer.indexOf('}') == -1 && buffer.indexOf('@') == -1) {
                            val line = bufferedReader.readLine()
                            if ((line) == null) break@SEARCH_FOR_ENTRY
                            buffer = buffer + line!!.trim { it <= ' ' }
                        }
                        var cutoff = if (buffer.indexOf(',') == -1) 0 else buffer.indexOf(',')
                        cutoff = if (buffer.indexOf('}') == -1 || cutoff < buffer.indexOf('}')) cutoff else buffer.indexOf('}')
                        cutoff = if (buffer.indexOf('@') == -1 || cutoff < buffer.indexOf('@')) cutoff else buffer.indexOf('@')
                        buffer = buffer.substring(cutoff)
                    }
                }

                //Discard a trailing ',' that might be left in the buffer
                if (buffer.length > 0 && buffer[0] == ',')
                    if (buffer.length > 1)
                        buffer = buffer.substring(1)
                    else
                        buffer = ""

                //Trim left over delimiters from the value
                value = trimDelimiters(value)
                //Add the bibtex tag to the entry
                entry.put(name, value)
            }
            BibtexEntryList.add(entry)
        }
        bufferedReader.close()

        return BibtexEntryList
    }

    private fun trimDelimiters(string: String): String {
        var s = string
        var t = s.replace("\\\\", "").replace("\\\"", "").replace("\\{", "").replace("\\}", "")
        if (t.length >= 2 && (t.startsWith("{") && t.endsWith("}") && s.startsWith("{") && s.endsWith("}") || t.startsWith("\"") && t.endsWith("\"") && s.startsWith("\"") && s.endsWith("\""))) {
            s = s.substring(1, s.length - 1)
            t = t.substring(1, t.length - 1)
        }
        return s
    }
}
