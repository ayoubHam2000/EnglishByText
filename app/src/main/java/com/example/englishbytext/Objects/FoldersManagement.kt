package com.example.englishbytext.Objects

import java.util.*
import kotlin.collections.ArrayList

object FoldersManagement {

    var path = LinkedList<String>()
    val list = ArrayList<String>()

    fun updateFolderList(){
        list.clear()
        val l = DataBaseServices.getListOfFolders(getPath())
        l.sortBy{it} //sort alphabetically
        for(item in l){
            list.add(getFolderBaseName(item))
        }

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

    fun getFolderBaseName(path : String) : String{
        return path.split("/").last()
    }

    fun getFolderFather(path: String) : String{
        val l = path.split("/")
        return l.subList(0, l.count() - 1).joinToString("/")
    }

}