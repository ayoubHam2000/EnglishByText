package com.example.englishbytext.Classes.Custom

class BoolVarBase(value: Boolean, id: Int) : VarDataBase(value.toString(), id) {

    fun get() : Boolean{
        return value.toBoolean()
    }

}