package com.example.englishbytext.Objects

import android.graphics.Typeface
import com.example.englishbytext.Utilites.*

object MainSetting {

    var categorySection = true
    var collectionSection = true
    var isDarkMode = false
    var selectedTextSize = 2
    var selectedTextFont = 0
    var selectedTextFontType = 0
    var onRegexSearch = false
    var sortTypeWordList = SORT_DEFAULT_DESC

    //not save in the data base
    var isRandomSortIsActive = false

    //region sort

    fun setSortTypeWordList(value : String, update : Boolean = true){
        if(update)
            DataBaseServices.updateVar(V_SortTypeWordList, value)
        sortTypeWordList = value.toInt()
    }

    //endregion

    //region collections

    fun setCategorySection(value : String, update : Boolean = true){
        if(update)
            DataBaseServices.updateVar(V_CategorySection, value)
        categorySection = value.toBoolean()
    }

    fun setCollectionSection(value : String, update : Boolean = true){
        if(update)
            DataBaseServices.updateVar(V_CollectionSection, value)
        collectionSection = value.toBoolean()
    }

    fun setIsDarkMode(value : String, update : Boolean = true){
        if(update)
            DataBaseServices.updateVar(V_DarkMode, value)
        isDarkMode = value.toBoolean()
    }

    fun setTextSize(value: String, update: Boolean = true){
        if(update)
            DataBaseServices.updateVar(V_TextSize, value)
        selectedTextSize = value.toInt()
    }

    fun setTextStyle(value: String, update: Boolean = true){
        if(update)
            DataBaseServices.updateVar(V_TextStyle, value)
        selectedTextFont = value.toInt()
    }

    fun setTextFont(value: String, update: Boolean = true){
        if(update)
            DataBaseServices.updateVar(V_TextFont, value)
        selectedTextFontType = value.toInt()
    }

    fun setRegexSearch(value: String, update: Boolean = true){
        if(update)
            DataBaseServices.updateVar(V_OnRegexSearch, value)
        onRegexSearch = value.toBoolean()
    }


    //endregion

    //region Text
    private val textSizeOptions = arrayOf(0.5f, 0.75f, 1f, 1.5f, 2f)
    private val textFontOptions = arrayOf(
            "sans-serif-thin",
            "casual",
            "cursive",
            "monospace",
            "sans-serif",
            "sans-serif-condensed",
    )
    private val textFontTypeOptions = arrayOf(
            Typeface.NORMAL,
            Typeface.BOLD,
            Typeface.ITALIC,
            Typeface.BOLD_ITALIC
    )

    fun getTextSize() : Float{
        return textSizeOptions[selectedTextSize]
    }

    fun getTextFont() : String{
        return textFontOptions[selectedTextFont]
    }

    fun getFonts() : Array<String>{
        return textFontOptions
    }

    fun getFontsType() : Int{
        return textFontTypeOptions[selectedTextFontType]
    }

    fun getTextFountText() : String{
        val textSize = arrayListOf(
            "Smallest",
            "Small",
            "Default",
            "Large",
            "Largest"
        )
        return textSize[selectedTextSize]
    }

    fun getTextStyleText() : String{
        return textFontOptions[selectedTextFont].replace("_-".toRegex(), " ")
    }

    fun fetTextFontText() : String{
        val textFont = arrayListOf(
            "Normal",
            "Bold",
            "Italic",
            "Bold Italic"
        )
        return textFont[selectedTextFontType]
    }

    //endregion

}