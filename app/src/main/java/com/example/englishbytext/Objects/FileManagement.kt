package com.example.englishbytext.Objects

import android.content.Context
import android.net.Uri
import com.example.englishbytext.Adapters.A_WordList
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

/*
 the pattern of the defined text
 word_name
 -definition_1
 -definition_2
 ....
 --example_1
 --example_2
 ....
*/

    fun startWorking(context : Context, fileUri: Uri, complete: () -> Unit){
        Lib.showMessage(context, "Start Working")
        thread {
            val data = getText(context, fileUri)
            val words = getListOfWords(data)
            DataBaseServices.insertWordsFromDefinedText(words)
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
        val unitRegex = Regex("^[+][\\s\\S]*?^[+]", RegexOption.MULTILINE)
        val data = unitRegex.findAll(text)
        val words = ArrayList<WordFile>()
        println(">>> matches ${data.count()}")
        data.forEach {unit->
            val defAndExpRegex = Regex("^--?[\\S\\s]*?(?=^--?|^\\+)", RegexOption.MULTILINE)
            val wordRegex = Regex("^[+][\\s]*\\w+", RegexOption.MULTILINE)
            val wordName = wordRegex.find(unit.value)?.value?.replace("^[+][\\s]*".toRegex(), "")
            if (wordName != null && wordName.length > 1){
                val word = WordFile()
                word.word = wordName
                val defAndExp = defAndExpRegex.findAll(unit.value)
                defAndExp.forEach { unitDefExp ->
                    val formatUnitDefExp = unitDefExp.value.replace("\\s".toRegex(), " ")
                    if (formatUnitDefExp.matches("--.*".toRegex())) {
                        val expUnit = formatUnitDefExp.replace("^--".toRegex(), "")
                        word.examples.add(expUnit)
                    }
                    else {
                        val defUnit = formatUnitDefExp.replace("^-".toRegex(), "")
                        word.definitions.add(defUnit)
                    }
                }
                words.add(word)
            }
        }
        println(">>> inserted ${words.count()}")
        return words
    }

    //---------------------------------------------------------
    //---------------------------------------------------------

    private fun findFileExtension(fileUri : Uri) : String{
        val arr = fileUri.path!!.split(".")
        return arr[arr.count() - 1].lowercase()
    }

}