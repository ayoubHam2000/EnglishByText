package com.example.englishbytext.Fragments

import android.os.Handler
import android.view.View
import android.widget.TextView
import com.example.englishbytext.*
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Utilites.OpenStatisticFrag
import java.lang.StringBuilder
import kotlin.concurrent.thread

class F_Statistic : MyFragment() {

    //region Init

    //====================================
    //++++++++++++++++++++++  Views
    //====================================
    lateinit var statisticTextView : TextView


    override fun getMainLayout(): Int {
        return R.layout.f_statistic
    }

    override fun getNotifyListenerId(): Int {
        return OpenStatisticFrag
    }

    override fun initVar(view: View) {
        statisticTextView = view.findViewById(R.id.statisticTextView)
    }

    override fun initFun() {
        initStaticView()
    }

    //endregion

    private fun initStaticView(){
        thread {
            val theValues = HashMap<String, Int>()

            val totalWords = DataBaseServices.tableCountByQuery("SELECT $A_word FROM $T_words")

            val tagsNbr = DataBaseServices.tableCountByQuery("SELECT $A_tag FROM $T_tags")
            val wordsInTags = DataBaseServices.tableCountByQuery("SELECT distinct $A_word FROM $T_wordTags")

            val foldersNbr = DataBaseServices.tableCountByQuery("SELECT $A_path FROM $T_folders")
            val wordsInFolder = DataBaseServices.tableCountByQuery("SELECT distinct $A_word FROM $T_words_Folder")

            val textNbr = DataBaseServices.tableCountByQuery("SELECT $A_textID FROM $T_texts")
            val textWordsNbr = DataBaseServices.tableCountByQuery("SELECT distinct $A_word FROM $T_wordsText")

            val notListedWordsNbr = DataBaseServices.tableCountByQuery("select $A_word from $T_words where $A_word not in (" +
                    "select $A_word from $T_relatedWord union" +
                    " select $A_related from $T_relatedWord union" +
                    " select $A_word from $T_words_Folder union" +
                    " select $A_word from $T_wordTags union" +
                    " select $A_word from $T_wordsText);")

            val imageWordsNbr = DataBaseServices.tableCountByQuery("SELECT distinct $A_word FROM $T_images")
            val audioWordsNbr = DataBaseServices.tableCountByQuery("SELECT distinct $A_word FROM $T_audios")
            val relatedWordsNbr = DataBaseServices.tableCountByQuery("select $A_word from $T_relatedWord union select $A_related from $T_relatedWord")

            theValues["Total Words"] = totalWords
            theValues["Total Tags"] = tagsNbr
            theValues["Total Tags Words"] = wordsInTags
            theValues["Total Folders"] = foldersNbr
            theValues["Total Folders Words"] = wordsInFolder
            theValues["Total Texts"] = textNbr
            theValues["Total Texts Words"] = textWordsNbr
            theValues["Total Unlisted Words"] = notListedWordsNbr
            theValues["Total Words With Images"] = imageWordsNbr
            theValues["Total Words With Audios"] = audioWordsNbr
            theValues["Total Related Word"] = relatedWordsNbr


            Handler(gContext.mainLooper).post{
                val res = StringBuilder()
                for(item in theValues.keys){
                    res.append(item)
                    res.append(" : ")
                    res.append(theValues[item])
                    res.append("\n")
                }
                statisticTextView.text = res.toString()
            }

        }
    }

}