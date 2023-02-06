package com.example.englishbytext.Objects

import java.util.*
import kotlin.collections.ArrayList

object FoldersManagement {

    var path = LinkedList<String>()
    val list = ArrayList<String>()

    fun restorePath(){
        path.clear()
        val tmp = MainSetting.folderPath.get().split("/")
        var i = 1 //to skip the first point : "./f1/f2/f3
        while (i < tmp.size){
            path.add(tmp[i])
            i++
        }
    }

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
        MainSetting.folderPath.set(getPath())
    }

    fun exitFolder(){
        if(path.isNotEmpty()){
            path.removeLast()
            MainSetting.folderPath.set(getPath())
        }
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