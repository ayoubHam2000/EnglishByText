package com.example.englishbytext.Objects

import android.content.Context
import android.net.Uri
import com.example.englishbytext.Classes.schemas.WordFile
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

object FileManagement {

    var fgType = ""
    var passedData = ""


    fun startWorking(context : Context, fileUri: Uri, complete: () -> Unit){
        Lib.showMessage(context, "Start Working")
        thread {
            val data = getText(context, fileUri)
            val words = getListOfWords(data)
            DataBaseServices.insertWordsFromPdf(words)
            complete()
        }
    }

    private fun getText(context : Context, fileUri: Uri) : String{
        when(findFileExtension(fileUri)){
            "txt" ->{
                return getTextFromText(context, fileUri)
            }
            "pdf" ->{
                return getTextFromPdf(context, fileUri)
            }
        }
        return ""
    }

    private fun getTextFromText(context : Context, filePath : Uri) : String{
        println(">>> get from text ...")
        val inputStream =  context.contentResolver.openInputStream(filePath)
        if(inputStream != null){
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            return String(buffer, StandardCharsets.UTF_8)
        }else{
            println("> error : file not found ${filePath.path}")
        }
        return ""
    }

    private fun getTextFromPdf(context : Context, fileUri : Uri) : String{
        println(">>> get from Pdf ...")
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

    private fun getListOfWords(text : String) : ArrayList<WordFile>{
        val data = text.replace("\r\n", "||")
        print("-->${data}")
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

    //---------------------------------------------------------
    //---------------------------------------------------------

    private fun findFileExtension(fileUri : Uri) : String{
        val arr = fileUri.path!!.split(".")
        return arr[arr.count() - 1].toLowerCase(Locale.ROOT)
    }

}