package com.example.englishbytext.Objects

import com.example.englishbytext.Classes.schemas.Collections
import com.example.englishbytext.Utilites.AllSet

object CollectionManagement {

    val collections = ArrayList<Collections>()
    lateinit var selectedCol : Collections

    private var selectOn = false

    fun getListOfCollection(){
        val selectedSet = SetManagement.getSelectedSet()
        val cols = DataBaseServices.getCollections(selectedSet)
        collections.clear()
        collections.addAll(cols)
    }



}