package com.example.englishbytext.Objects

import com.example.englishbytext.*
import com.example.englishbytext.Classes.schemas.Word
import com.example.englishbytext.Objects.DataBaseServices.toBase64
import com.example.englishbytext.Utilites.SORT_CREATED_TIME_ASC
import com.example.englishbytext.Utilites.SORT_CREATED_TIME_DESC
import com.example.englishbytext.Utilites.SORT_DEFAULT_ASC
import com.example.englishbytext.Utilites.SORT_DEFAULT_DESC


object WordsManagement {

    val wordList = ArrayList<Word>()
    var selectedWordName : String = ""

    object Setting{
        var tableType = -1
    }

    fun updateWordList(fgType : String, passedData : String){
        wordList.clear()

        val query = getWordQuery(fgType, passedData)
        println("---> $query")
        wordList.addAll(DataBaseServices.getWords(query))

    }

    private fun getWordQuery(fgType : String, passedData : String) : String{
        val q1 = "SELECT $A_word, $A_favorite FROM $T_words ?1"
        val q2 = " WHERE $A_word IN (?1) ?2"

        val p = passedData.toBase64()
        val tag = "SELECT $A_word FROM $T_wordTags WHERE $A_tag = '$p'"
        val folder = "Select $A_word From $T_words_Folder Where $A_path = '$p'"
        val text = "SELECT $A_word FROM $T_wordsText WHERE $A_textID = ${TextManagement.selectedItem}"

        val sortType = getSortType()

        return when(fgType){
            "Main" -> q1.replace("?1", sortType)
            "Tags" -> q1.replace("?1", q2).replace("?1", tag).replace("?2", sortType)
            "Folders" -> q1.replace("?1", q2).replace("?1", folder).replace("?2", sortType)
            "Text" -> q1.replace("?1", q2).replace("?1", text).replace("?2", sortType)
            else -> q1.replace("?1", sortType)
        }
    }

    private fun getSortType() : String{
        return "Order by " + when(com.example.englishbytext.Objects.Setting.sortTypeWordList){
            SORT_DEFAULT_ASC -> A_level_order
            SORT_DEFAULT_DESC -> "$A_level_order DESC"
            SORT_CREATED_TIME_ASC -> A_created_time
            SORT_CREATED_TIME_DESC -> "$A_created_time DESC"
            else -> "$A_level_order DESC"
        }
    }

}