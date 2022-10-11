package com.example.englishbytext.Classes.Custom

import com.example.englishbytext.Objects.DataBaseServices

abstract class VarDataBase(var value: String, val id: Int) {
    init {
        //store and get or just get value if the value exist
        value = DataBaseServices.getVar(id, value)
    }

    private fun setNewValue(newValue: String){
        DataBaseServices.updateVar(id, newValue)
        value = newValue
    }

    fun set(newValue : Any){
        setNewValue(newValue.toString())
    }

}
