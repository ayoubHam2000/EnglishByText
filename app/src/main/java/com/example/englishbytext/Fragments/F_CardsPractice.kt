package com.example.englishbytext.Fragments

import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.example.englishbytext.Adapters.A_Cards_Practice
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.Edit
import com.example.englishbytext.Utilites.NextPage
import com.example.englishbytext.Utilites.OpenCardsPractice

class F_CardsPractice : MyFragment() {

    //region init
    //====================================
    //++++++++++++++++++++++  vars
    //====================================
    lateinit var cardsAdapter : A_Cards_Practice
    var wordNameEditIndex = -1

    //====================================
    //++++++++++++++++++++++  Views
    //====================================
    lateinit var practiceViewPage : ViewPager


    //====================================
    //++++++++++++++++++++++  Init
    //====================================
    override fun getMainLayout(): Int {
        return R.layout.f_cards_practice
    }

    override fun getNotifyListenerId(): Int {
        return OpenCardsPractice
    }

    override fun initVar(view: View) {
        practiceViewPage = view.findViewById(R.id.practiceViewPage)
    }

    override fun initFun() {
        initPageViewer()
    }

    //endregion

    //region pageViewer

    private fun initPageViewer(){
        cardsAdapter = A_Cards_Practice(gContext){ eventName, pos->
            pageViewerAdapterEvent(eventName, pos)
        }
        //cardsAdapter.changeList()
        practiceViewPage.adapter = cardsAdapter
    }

    private fun pageViewerAdapterEvent(event : Int, position: Int){
        when(event){
            NextPage -> nextPage(position)
            Edit -> openEditWord(position)
        }
    }

    private fun nextPage(position: Int){
        if(position == cardsAdapter.list.count()){
            Lib.showMessage(gContext, "End")
        }
        practiceViewPage.setCurrentItem(position, true)
    }

    private fun openEditWord(position : Int){
        val bundle = Bundle()
        wordNameEditIndex = position
        bundle.putString("WORD_NAME", cardsAdapter.list[position].name)
        navController.navigate(R.id.action_f_CardsPractice_to_f_WordEdit, bundle)
    }

    //endregion

    //region override

    override fun onPause() {
        super.onPause()
        println(">>> onPause")
        saveStatesMap["Pager"] = practiceViewPage.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()
        if (wordNameEditIndex >= 0)
        {
            val wordName = cardsAdapter.list[wordNameEditIndex].name
            val isWordNotExist = DataBaseServices.isWordNotExist(wordName)
            if (isWordNotExist)
            {
                cardsAdapter.list.removeAt(wordNameEditIndex)
                cardsAdapter.notifyDataSetChanged()
                if (cardsAdapter.list.size == 0)
                {
                    //return to word list when all words all deleted
                    activity?.onBackPressed()
                }
            }
            else
            {
                val isFavorite = DataBaseServices.getWordFavorite(wordName)
                val isKnown = DataBaseServices.getWordIsKnown(wordName)
                cardsAdapter.list[wordNameEditIndex].isFavorite = isFavorite
                cardsAdapter.list[wordNameEditIndex].isKnown = isKnown
            }
            //println(">>> onResume $wordName")
        }
        practiceViewPage.onRestoreInstanceState(saveStatesMap["Pager"])
    }

    //endregion
}