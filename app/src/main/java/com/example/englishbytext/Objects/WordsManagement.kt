package com.example.englishbytext.Objects

import android.content.Context
import com.example.englishbytext.*
import com.example.englishbytext.Classes.schemas.Word
import com.example.englishbytext.Classes.schemas.WordFrequency
import com.example.englishbytext.Objects.DataBaseServices.toBase64
import com.example.englishbytext.Utilites.*
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread


object WordsManagement {

    val wordList = ArrayList<Word>()
    var practiceList = ArrayList<Word>()
    var selectedWordName : String = ""
    val wordsFrequency = HashMap<String, Int>(25285)



    object Setting{
        var tableType = -1
    }

    private fun sortPracticeList()
    {
        practiceList.sortByDescending { getWordFrequency(it.name) }
        practiceList.sortBy { it.isKnown == 4 }
    }

    fun setPracticeWordList(listWord: ArrayList<Word>)
    {
        practiceList = listWord
        //practiceList.removeIf { it.isKnown == 1 }
        if (MainSetting.onSortPractice.get())
            sortPracticeList()
    }

    fun updateWordList(fgType : String, passedData : String){
        wordList.clear()

        val query = getWordQuery(fgType, passedData)
        println(">>> $query ,Random = ${MainSetting.isRandomSortIsActive}, SortType = ${MainSetting.sortTypeWordList}")

        when {
            MainSetting.isRandomSortIsActive -> wordList.addAll(DataBaseServices.getWords(query).shuffled())
            else ->
            {
                wordList.addAll(DataBaseServices.getWords(query))
                //wordList.removeIf { it.isKnown == 1 }
                if (MainSetting.sortTypeWordList.get() / 2 == SORT_MASTERED_ASC / 2) {
                    wordList.sortByDescending { getWordFrequency(it.name) }
                    if (MainSetting.sortTypeWordList.get() % 2 == 1)
                        wordList.sortBy { it.isKnown == 4 }
                    else
                        wordList.sortBy { it.isKnown != 4 }
                }
                else if (MainSetting.sortTypeWordList.get() / 2 == SORT_CREATED_TIME_ASC / 2 && MainSetting.sortTypeWordList.get() % 2 == 1)
                    wordList.reverse()
            }
        }
    }

    private fun getWordQuery(fgType : String, passedData : String) : String{
        val q1 = "SELECT $A_word, $A_favorite, $A_isKnown FROM $T_words ?1"
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
        val sortType = MainSetting.sortTypeWordList.get() / 2
        //no DESC
        return "Order By " + SORT_WORD_LIST_BY[sortType]
    }


    //region words frequency

    fun getWordFrequency(word : String) : Int{
        return wordsFrequency[word] ?: 0
    }

    fun addWordFrequency(context: Context){
        println(">>> Start Load Word Frequency")
        if(wordsFrequency.isNotEmpty()) return
        thread {
            val file = context.assets.open("dic.txt")
            val bufferReader = BufferedReader(InputStreamReader(file))
            while (bufferReader.ready()){
                val line = bufferReader.readLine()
                val item = line.split(" : ")
                if(item.isNotEmpty() && item.count() == 2){
                    wordsFrequency[item[0]] = (item[1]).toInt()
                }
            }
            println(">>> Done Load Word Frequency")
        }

    }

    //endregion

}