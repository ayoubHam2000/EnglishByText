package com.example.englishbytext.Classes.schemas

class WordFile(

){
    var word : String = ""
    val definitions = ArrayList<String>()
    val examples = ArrayList<String>()

    fun print(){
        println("$word || $definitions || $examples")
    }
}