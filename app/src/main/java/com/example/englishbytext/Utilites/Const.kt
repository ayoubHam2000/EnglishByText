package com.example.englishbytext.Utilites

import com.example.englishbytext.A_created_time
import com.example.englishbytext.A_isKnown

//$s,$e|
/*
Folders
Tags
Main
Text
 */

//Names
const val AllSet = "AllSets"
const val PassedData = "PassedData"
const val FgType = "FgType"

//settings
const val MaxSetName = 30
const val MaxCollectionName = 30
const val MaxFolderName = 50
const val MaxTextName = 30
const val MaxTextChars = 50
const val MaxTagChars = 70
const val WhitePercentage = 40

//request
const val REQUEST_IMAGE_CAPTURE = 1
const val REQUEST_Audio_CAPTURE = 2
const val REQUEST_PERMISSION_STORAGE = 3
const val REQUEST_PERMISSION_CAMERA = 4
const val REQUEST_PERMISSION_AUDIO = 5
const val GALLERY_REQUEST_CODE = 6

//Actions
const val OpenItem = 0
const val SelectItem = 1
const val Edit = 2
const val Delete = 3
const val ChangeColor = 4
const val MoveToAction = 5
const val NotEmpty = 6
const val Empty = 7
const val AddWord = 9
const val Practice = 11
const val OnSelectMode = 12
const val OpenDefinition = 13
const val OpenExample = 14
const val AddImage = 15
const val OpenFolderContent = 16
const val SelectModeClick = 17
const val NextPage = 18
const val END = 19

//section
const val NormalSection = -1

//notifyActivity
const val OpenCollectionFrag = 0
const val OpenTextFrag = 1
const val OpenTextEditFrag = 2
const val SaveChanges = 3
const val OpenTextDisplayFrag = 4
const val OpenWordEdit = 5
const val OpenSettings = 6
const val OpenWordList = 7
const val OpenTagFg = 8
const val OnProcess = 9
const val RefreshData = 10
const val OpenAllFoldersFrag = 11
const val OpenCardsPractice = 12
const val OpenStatisticFrag = 13
const val OpenFrequencyFrag = 14

//arg
const val ARG_CollectionName = "ARG_CollectionName"
const val ARG_CollectionFather = "ARG_CollectionFather"

//Folders
const val FILE_CODE = "start-SXFP-CHYK-ONI6-S89U-XNSS-HSJW-3NGU-8XTJ-APP-DATA-CODE-end"
const val AUDIO_FOLDER = "audios"
const val IMAGE_FOLDER = "images"
const val FILES_FOLDER = "Files"

//bundel
const val SELECTED_TAG = "SELECTED_TAG"

//Sort
//sort should be continuous 0 1 2 3 ..
//always type1 ASC, type1 DESC, type2 ASC, type2 DESC ...
const val SORT_CREATED_TIME_ASC = 0
const val SORT_CREATED_TIME_DESC = 1

val SORT_WORD_LIST_BY = arrayListOf(A_created_time, A_isKnown)

const val EXAMPLES_MAX = 3