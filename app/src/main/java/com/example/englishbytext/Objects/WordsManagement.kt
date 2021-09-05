package com.example.englishbytext.Objects

import android.content.Context
import com.example.englishbytext.*
import com.example.englishbytext.Classes.schemas.Word
import com.example.englishbytext.Classes.schemas.WordFrequency
import com.example.englishbytext.Objects.DataBaseServices.toBase64
import com.example.englishbytext.Utilites.*
import kotlin.concurrent.thread


object WordsManagement {

    val wordList = ArrayList<Word>()
    var selectedWordName : String = ""
    val wordsFrequency = HashMap<String, Int>(25285)



    object Setting{
        var tableType = -1
    }

    fun updateWordList(fgType : String, passedData : String){
        wordList.clear()

        val query = getWordQuery(fgType, passedData)
        println(">>> $query ,Random = ${MainSetting.isRandomSortIsActive}")

        when {
            MainSetting.isRandomSortIsActive -> wordList.addAll(DataBaseServices.getWords(query).shuffled())
            MainSetting.sortTypeWordList % 2 == 1 -> wordList.addAll(DataBaseServices.getWords(query).reversed())
            else -> wordList.addAll(DataBaseServices.getWords(query))
        }
    }

    private fun getWordQuery(fgType : String, passedData : String) : String{
        val q1 = "SELECT $A_word, $A_favorite FROM $T_words ?1"
        val q2 = " WHERE $A_word IN (?1) ?2"

        val p = passedData.toBase64()
        val tag = "SELECT $A_word FROM $T_wordTags WHERE $A_tag = '$p'"
        val folder = "Select $A_word From $T_words_Folder Where $A_path = '$p'"
        val text = "SELECT $A_word FROM $T_wordsText WHERE $A_textID = ${TextManagement.selectedItem}"

        val sortType = getSortType()

        return when(fgType){
            "Main" -> q1.replace("?1", sortType)
            "Tags" -> q1.replace("?1", q2).replace("?1", tag).replace("?2", sortType)
            "Folders" -> q1.replace("?1", q2).replace("?1", folder).replace("?2", sortType)
            "Text" -> q1.replace("?1", q2).replace("?1", text).replace("?2", sortType)
            else -> q1.replace("?1", sortType)
        }
    }

    private fun getSortType() : String{
        val sortType = MainSetting.sortTypeWordList
        //no DESC
        return "Order By " + SORT_WORD_LIST_BY[sortType]
    }


    //region words frequency

    fun getWordFrequency(word : String) : Int{
        return wordsFrequency[word] ?: 0
    }

    private fun getText(context : Context) : String{
        val file = context.assets.open("dic.txt")
        val size = file.available()
        val buffer = ByteArray(size)
        file.read(buffer)
        file.close()
        return String(buffer)
    }

    fun addWordFrequency(context: Context){
        if(wordsFrequency.isNotEmpty()) return
        thread {
            val text = getText(context)
            val words = text.split("\r\n")
            for(item in words){
                val s = item.split(" : ")
                if(item.isNotEmpty() && s.count() == 2){
                    wordsFrequency[s[0]] = (s[1]).toInt()
                }
            }
            println(">>> Done Load Word Frequency")
        }

    }

    //endregion

}