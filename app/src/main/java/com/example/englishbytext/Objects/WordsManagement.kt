package com.example.englishbytext.Objects

import com.example.englishbytext.Classes.schemas.Word

object WordsManagement {

    val wordList = ArrayList<Word>()
    var selectedWordName : String = ""

    object Setting{
        var tableType = -1
    }

    fun updateWordList(fgType : String, passedData : String){
        wordList.clear()
        when(fgType){
            "Main" -> wordList.addAll(DataBaseServices.getWords(""))
            "Tags" -> wordList.addAll(DataBaseServices.getWords(passedData))
            "Folders" -> wordList.addAll(DataBaseServices.getListOfWordsFromFolder(passedData))
        }

    }


}