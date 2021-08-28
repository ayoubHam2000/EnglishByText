package com.example.englishbytext.Objects

import com.example.englishbytext.Classes.schemas.Word

object WordsManagement {

    val wordList = ArrayList<Word>()
    var selectedWordName : String = ""

    object Setting{
        var tableType = -1
    }

    fun updateWordList(tag : String){
        wordList.clear()
        wordList.addAll(DataBaseServices.getWords(tag))
    }


}