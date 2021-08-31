package com.example.englishbytext.Fragments

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
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
import com.example.englishbytext.Adapters.A_textItem
import com.example.englishbytext.Classes.Objects.D_ask
import com.example.englishbytext.Classes.schemas.Collections
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Interfaces.NotifyActivity
import com.example.englishbytext.Objects.*
import com.example.englishbytext.Objects.Lib.colorWhiter
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.*


class F_Text : MyFragment() {

    //region int

    //====================================
    //++++++++++++++++++++++  Vars
    //====================================
    private lateinit var collection : Collections
    private lateinit var textAdapter : A_textItem


    //====================================
    //++++++++++++++++++++++  Views
    //====================================
    private lateinit var recyclerView : RecyclerView
    private lateinit var dialogEditItem : D_editItem
    private lateinit var btnAddTextBtn : ImageView
    private lateinit var emptyPageNot : LinearLayout

    //====================================
    //++++++++++++++++++++++  Init
    //====================================
    override fun getMainLayout(): Int {
        return R.layout.f_text
    }

    override fun getNotifyListenerId(): Int {
        return OpenTextFrag
    }

    override fun initVar(view : View){
        navController = Navigation.findNavController(view)
        recyclerView = view.findViewById(R.id.r_textRecyclerView)
        emptyPageNot =  view.findViewById(R.id.t_createNewSet)
        btnAddTextBtn = activity?.findViewById(R.id.addText)!!
    }

    override fun initFun() {
        collection = CollectionManagement.selectedCol
        initText()
    }


    //endregion

    //region initText
    private fun initText(){
        initRecyclerView()
        btnAddTextBtn.setOnClickListener { createNewCollection() }
    }

    private fun createNewCollection(){
        Lib.printLog("AddTextItem")
        dialogEditItem = D_editItem(gContext){
            when {
                it.count() > MaxTextName -> {
                    Lib.showMessage(gContext, "${gContext.getString(R.string.Max_character_is)}$MaxTextName")
                }
                else -> {
                    DataBaseServices.insertText(it)
                    refreshTextRV()
                    dialogEditItem.dismiss()
                }
            }
        }
        dialogEditItem.textHint = gContext.getString(R.string.text_title)
        dialogEditItem.maxLine = 1
        dialogEditItem.maxChar = MaxTextName
        dialogEditItem.buildAndDisplay()
    }

    private fun deleteTexts(){
        val ask = D_ask(gContext, "ARE YOU SURE ?"){
            if(it){
                DataBaseServices.deleteTexts(textAdapter.getSelectedItems())
                refreshTextRV()
                deaSelectMode()
            }
        }
        ask.buildAndDisplay()
    }

    private fun initRecyclerView(){
        textAdapter = A_textItem(gContext){event, id->
            recyclerViewEvent(event, id)
        }
        val layoutManager = LinearLayoutManager(gContext)
        textAdapter.changeList()

        recyclerView.adapter = textAdapter
        recyclerView.layoutManager = layoutManager
    }

    private fun recyclerViewEvent(event : Int, id : Int){
        when(event){
            OpenItem->{
                openItem(id)
            }
            Edit->{
                editItem(id)
            }
            OnSelectMode->{
                onSelectMode()
            }
            NotEmpty->{
                emptyPageNot.visibility = View.GONE
            }
            Empty->{
                emptyPageNot.visibility = View.VISIBLE
            }

        }
    }

    private fun openItem(id : Int){
        println("--> Open Text")
        TextManagement.selectedItem = id
        navController.navigate(R.id.action_f_Text_to_f_TextDisplay)
    }

    private fun editItem(id : Int){
        println("--> Edit Text")
        TextManagement.selectedItem = id
        navController.navigate(R.id.action_f_Text_to_f_EditText)
    }

    private fun onSelectMode(){
        btnAddTextBtn.setBackgroundResource(R.drawable.ic_delete_24)
        btnAddTextBtn.setOnClickListener { deleteTexts() }
    }

    //endregion

    //region function

    fun deaSelectMode() : Boolean{
        return if(textAdapter.onSelectMode){
            textAdapter.deactivateSelection()
            btnAddTextBtn.setBackgroundResource(R.drawable.ic__add)
            btnAddTextBtn.setOnClickListener { createNewCollection() }
            true
        }else{
            false
        }
    }

    private fun refreshTextRV(){
        textAdapter.changeList()
    }

    //endregion

    
}