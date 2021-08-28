package com.example.englishbytext.Objects

import java.util.*
import kotlin.collections.ArrayList

object FoldersManagement {

    var path = LinkedList<String>()
    val list = ArrayList<String>()

    fun updateFolderList(){
        list.clear()
        list.addAll(DataBaseServices.getListOfFolders(getPath()))
    }

    fun openFolder(name : String){
        path.add(name)
    }

    fun exitFolder(){
        if(path.isNotEmpty()) path.removeLast()
    }

    fun getPath() : String{
        if(path.isEmpty()){
            return "."
        }
        return "./" + path.joinToString("/")
    }

}