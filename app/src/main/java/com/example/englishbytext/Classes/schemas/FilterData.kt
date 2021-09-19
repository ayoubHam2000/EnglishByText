package com.example.englishbytext.Classes.schemas

import com.example.englishbytext.Objects.MainSetting

class FilterData {

    var isFavorite = false
    var hasDefinition = false
    var hasExample = false
    var hasImage = false
    var hasAudio = false
    var hasFolder = false
    var hasTag = false

    var onRegex = MainSetting.onRegexSearch
    var searchWord = ""

}