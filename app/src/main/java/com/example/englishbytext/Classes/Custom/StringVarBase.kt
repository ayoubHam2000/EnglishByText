package com.example.englishbytext.Classes.Custom

class StringVarBase(value: String, id: Int) : VarDataBase(value, id) {

    fun get() : String{
        return value
    }
}
