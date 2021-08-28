package com.example.englishbytext.Objects

import com.example.englishbytext.Classes.schemas.StringId
import com.example.englishbytext.Classes.schemas.WordInfoId
import java.io.File

object MediaManagement {

    val images = ArrayList<WordInfoId>()
    val audios = ArrayList<WordInfoId>()

    fun updateImagesList(wordName : String){
        images.clear()
        images.addAll(DataBaseServices.getWordImages(wordName))
    }

    fun updateAudioList(wordName: String){
        audios.clear()
        audios.addAll(DataBaseServices.getWordAudios(wordName))
    }

    fun deleteFiles(listPaths : ArrayList<String>){
        for(item in listPaths){
            File(item).delete()
        }
    }

}