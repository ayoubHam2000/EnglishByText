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
        var data = getTextFromPdf(context, fileUri).replace("Page \\d+?\n".toRegex(), "")
        data = data.replace("Summary[ \n]of[ \n]Annotations".toRegex(), "")
        val units = data.split("#\\d.*?\\n".toRegex())
        val words = ArrayList<WordFile>()

        for(unit in units){
            val lines = unit.split("\n")
            val word = WordFile()
            var case = 0
            for(i in lines){
                val item = i.trim()
                if(item.isEmpty() || item.isBlank()) continue
                if(case == 0){
                    word.word = item
                    case++
                }else if(case == 1){
                    if(item.matches("---ex".toRegex())){
                        case++
                        continue
                    }
                    word.definitions.add(item.replace("^-".toRegex(), ""))
                }else if(case == 2){
                    word.examples.add(item.replace("^-".toRegex(), ""))
                }
            }
            if(case > 0)
            words.add(word)
        }
        println("---> ${units.count()}")
        for((i, item) in words.withIndex()){
            print("--->${i+1}. ")
            item.print()
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