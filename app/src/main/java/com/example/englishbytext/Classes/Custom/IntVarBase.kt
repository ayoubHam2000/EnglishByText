package com.example.englishbytext.Classes.Custom

class IntVarBase(value: Int, id: Int) : VarDataBase(value.toString(), id) {

    fun get() : Int{
        return value.toInt()
    }

}