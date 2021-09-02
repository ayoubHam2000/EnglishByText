package com.example.englishbytext.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Adapters.A_collection
import com.example.englishbytext.Classes.Objects.D_ask
import com.example.englishbytext.Classes.schemas.Collections
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Interfaces.NotifyActivity
import com.example.englishbytext.Objects.*
import com.example.englishbytext.Objects.Lib.printLog
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.*


class F_Collection : MyFragment() {

    //region var, int

    //====================================
    //++++++++++++++++++++++  vars
    //====================================
    private lateinit var collectionAdapter : A_collection


    //====================================
    //++++++++++++++++++++++  view
    //====================================
    private lateinit var recyclerView : RecyclerView
    private lateinit var dialogEditItem : D_editItem
    private lateinit var btnAddCollection : ImageView
    private lateinit var emptyPageNot : LinearLayout


    //====================================
    //++++++++++++++++++++++  init
    //====================================
    override fun getMainLayout(): Int {
        return R.layout.f_collection
    }

    override fun getNotifyListenerId(): Int {
        return OpenCollectionFrag
    }

    override fun initVar(view: View) {
        recyclerView = view.findViewById(R.id.r_CollectionNames)
        emptyPageNot =  view.findViewById(R.id.t_createNewSet)
        btnAddCollection = activity?.findViewById(R.id.addCollection)!!
    }

    override fun initFun() {
        initRecyclerView()
        btnAddCollection.setOnClickListener { createNewCollection() }
    }


    //endregion

    //region collection

    private fun initRecyclerView(){
        collectionAdapter = A_collection(gContext){ event, c ->
            if(c != null){
                recyclerViewEvent(event,  c)
            }else{
                recyclerViewEvent(event,  Collections("", ""))
            }

        }
        val layoutManager = LinearLayoutManager(gContext)
        collectionAdapter.changeList()

        recyclerView.adapter = collectionAdapter
        recyclerView.layoutManager = layoutManager
    }

    private fun createNewCollection(){
        printLog("AddCollection")
        val setName = SetManagement.getSelectedSet()
        dialogEditItem = D_editItem(gContext){
            when {
                it.count() > MaxCollectionName -> {
                    Lib.showMessage(gContext, "${gContext.getString(R.string.Max_character_is)}$MaxCollectionName")
                }
                DataBaseServices.isCollectionNotExist(setName, it) -> {
                    DataBaseServices.insertCollection(it)
                    refreshCollection()
                    dialogEditItem.dismiss()
                }
                else -> {
                    Lib.showMessage(gContext, R.string.this_name_is_taken)
                }
            }
        }
        dialogEditItem.textHint = gContext.getString(R.string.collection_name)
        dialogEditItem.maxLine = 1
        dialogEditItem.maxChar = MaxCollectionName
        dialogEditItem.buildAndDisplay()
    }

    private fun recyclerViewEvent(event: Int, c : Collections){
        when(event){
            Edit -> {
                editCollectionName(c)
            }
            Delete -> {
                deleteCollection(c)
            }
            OpenItem -> {
                openItem(c)
                printLog("info openItem")
            }
            Practice -> {
                printLog("practice")
            }
            NotEmpty -> {
                emptyPageNot.visibility = View.GONE
            }
            Empty -> {
                emptyPageNot.visibility = View.VISIBLE
            }
        }
    }

    private fun editCollectionName(c : Collections){
        dialogEditItem = D_editItem(gContext){
            when {
                it.count() > MaxCollectionName -> {
                    Lib.showMessage(gContext, "${gContext.getString(R.string.Max_character_is)}$MaxCollectionName")
                }
                DataBaseServices.isCollectionNotExist(c.father, it) -> {
                    DataBaseServices.updateCollection(c, it)
                    refreshCollection()
                    dialogEditItem.dismiss()
                }
                else -> {
                    Lib.showMessage(gContext, R.string.this_name_is_taken)
                }
            }
        }
        dialogEditItem.textHint = gContext.getString(R.string.collection_name)
        dialogEditItem.maxChar = MaxCollectionName
        dialogEditItem.maxLine = 1
        dialogEditItem.textInput = c.name
        dialogEditItem.buildAndDisplay()
    }

    private fun deleteCollection(c : Collections){
        val ask = D_ask(gContext, "ARE YOU SURE ?"){
            if(it){
                DataBaseServices.deleteCollection(c)
                refreshCollection()
            }
        }
        ask.buildAndDisplay()
    }

    private fun openItem(c : Collections){
        CollectionManagement.selectedCol = c
        navController.navigate(R.id.action_f_Collection_to_f_Text)
    }

    //endregion

    //region refresh fragment

    fun refreshCollection(){
        collectionAdapter.changeList()
    }

    //endregion

    //region override

    override fun onPause() {
        super.onPause()
        saveStatesMap["View1"] = recyclerView.layoutManager?.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()
        recyclerView.layoutManager?.onRestoreInstanceState(saveStatesMap["View1"])
    }

    //endregion


}