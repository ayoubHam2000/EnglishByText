package com.example.englishbytext.Classes.schemas

class StringId(
    val id : Int,
    val value : String
)
{
    override fun toString(): String {
        return value
    }
}