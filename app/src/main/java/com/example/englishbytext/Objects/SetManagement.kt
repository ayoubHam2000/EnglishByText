package com.example.englishbytext.Objects

import com.example.englishbytext.Classes.schemas.Sets
import com.example.englishbytext.Utilites.AllSet

object SetManagement {

    val sets = ArrayList<Sets>()

    //region settings
    private var selectOn = false

    fun getSelectOn() : Boolean{
        return selectOn
    }
    fun setSelectOn(value : Boolean){
        selectOn = value
    }

    fun getSelectedSet() : String{
        return MainSetting.selectedSet.get()
    }

    //endregion

    fun refreshSets(){
        val res = DataBaseServices.getSets()
        sets.clear()
        sets.addAll(res)
    }

    fun insertSet(name : String, color : Int){
        DataBaseServices.insertSet(name, color)
    }

    fun getSet() : Sets?{
        for(item in sets){
            if(item.name == MainSetting.selectedSet.get()){
                return item
            }
        }
        return null
    }

}