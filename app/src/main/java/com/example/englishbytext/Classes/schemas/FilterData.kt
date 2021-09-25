package com.example.englishbytext.Classes.schemas

import com.example.englishbytext.Objects.MainSetting

class FilterData {

    enum class Options{
        ALL, ON, OFF
    }

    var isFavorite = Options.ALL
    var hasDefinition = Options.ALL
    var hasExample = Options.ALL
    var hasImage = Options.ALL
    var hasAudio = Options.ALL
    var hasFolder = Options.ALL
    var hasTag = Options.ALL
    var hasText = Options.ALL

    var onRegex = MainSetting.onRegexSearch
    var searchWord = ""

    companion object{
         fun getOptionType(i : Int) : Options{
            return when(i){
                0 -> Options.ON
                1 -> Options.OFF
                else -> Options.ALL
            }
        }
    }

}