package com.example.englishbytext.Objects

import com.example.englishbytext.Classes.schemas.Texts
import com.example.englishbytext.Classes.schemas.WordPosItem

object TextManagement {

    val texts = ArrayList<Texts>()

    var selectedItem = -1

    fun getListOfText(){
        val res = DataBaseServices.getTexts()
        texts.clear()
        texts.addAll(res)
    }

    fun getText() : Texts?{
        for(item in texts){
            if(item.id == selectedItem)
                return item
        }
        return null
    }

    fun getWordPos() : ArrayList<WordPosItem>{
        return DataBaseServices.getTextWordsPos(selectedItem)
    }

    fun updateWordPos(wPos : ArrayList<WordPosItem>){
        DataBaseServices.updateTextWordsPos(selectedItem, wPos)
    }

}