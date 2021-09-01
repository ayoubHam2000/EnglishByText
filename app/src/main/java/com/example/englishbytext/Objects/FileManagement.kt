package com.example.englishbytext.Objects

import android.content.Context
import android.net.Uri
import com.example.englishbytext.Classes.schemas.WordFile
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.nio.charset.StandardCharsets
import java.util.HashMap
import kotlin.concurrent.thread

object FileManagement {

    var fgType = ""
    var passedData = ""

    fun startWorking(c : Context, fileUri: Uri, complete: () -> Unit){
        Lib.showMessage(c, "Start Working")
        thread {
            val words = getFromText(c, fileUri)
            DataBaseServices.insertWordsFromPdf(words)
            complete()
        }
    }

    private fun getFromText(context : Context, fileUri : Uri) : ArrayList<WordFile>{
        val data = getTextFromPdf(context, fileUri).replace("\n", "||")
        val units = "\\+.*?\\+".toRegex().findAll(data)
        val words = ArrayList<WordFile>()

        for(unit in units){
            //println("-->${unit.value}")
            val items = unit.value.replace("(^[+]|[+]$)".toRegex(), "") .split("||")
            val word = WordFile()
            var case = 0
            for(i in items){
                val item = i.trim()
                if(item.isNotEmpty()){
                    when {
                        case == 0 -> {
                            word.word = item
                            case++
                        }
                        item.matches("^-[^-].*".toRegex()) -> {
                            word.definitions.add(item.replace("^-".toRegex(), ""))
                        }
                        item.matches("^--[^-].*".toRegex()) -> {
                            word.examples.add(item.replace("^--".toRegex(), ""))
                            case++
                        }
                        case == 1 -> {
                            val last = word.definitions.count() - 1
                            word.definitions[last] =  word.definitions[last] + " $item"
                        }
                        case > 1 -> {
                            val last = word.definitions.count() - 1
                            word.examples[last] =  word.examples[last] + " $item"
                        }
                    }
                }
            }
            if(word.word.isNotEmpty()){
                words.add(word)
            }
        }
        return words
    }

    private fun getTextFromPdf(context : Context, fileUri : Uri) : String{
        println(">>| get from Pdf ...")
        try {
            val parsedText = ArrayList<String>()
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val reader = PdfReader(inputStream)
            val max = reader.numberOfPages

            for(item in 1..max){
                parsedText.add(PdfTextExtractor.getTextFromPage(reader, item))
            }

            reader.close()
            return parsedText.joinToString(" ")
        } catch (e: Exception) {
            println(">>| error Pdf")
            println(e.printStackTrace())
        }
        return ""
    }

}