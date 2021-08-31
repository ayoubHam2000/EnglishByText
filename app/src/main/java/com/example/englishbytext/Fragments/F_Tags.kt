package com.example.englishbytext.Fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Adapters.A_tag
import com.example.englishbytext.Classes.Objects.D_ask
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.*

class F_Tags : MyFragment() {

    //region init

    //====================================
    //++++++++++++++++++++++  Vars
    //====================================
    lateinit var tagAdapter : A_tag

    //====================================
    //++++++++++++++++++++++  View
    //====================================
    private lateinit var addBtn : ImageView
    private lateinit var deleteBtn : ImageView
    private lateinit var modifyTag : ImageView
    private lateinit var tagRv : RecyclerView


    //====================================
    //++++++++++++++++++++++  init
    //====================================
    override fun getMainLayout(): Int {
        return R.layout.f_tags
    }

    override fun getNotifyListenerId(): Int {
        return OpenTagFg
    }

    override fun initVar(view: View) {
        addBtn = activity?.findViewById(R.id.addTag)!!
        deleteBtn = activity?.findViewById(R.id.deleteTag)!!
        modifyTag = activity?.findViewById(R.id.modifyTag)!!
        tagRv = view.findViewById(R.id.tagRv)
    }

    override fun initFun() {
        addBtn.setOnClickListener { addTag() }
        deleteBtn.setOnClickListener { deleteTags() }
        modifyTag.setOnClickListener { modifyTagClick() }

        initRv()
    }

    //endregion

    //region ragRv

    private fun addTag(){
        var dialog : D_editItem? = null
        dialog = D_editItem(gContext){tag->
            when{
                DataBaseServices.isTagNotExist(tag) ->{
                    DataBaseServices.insertTag(tag)
                    tagAdapter.changeList()
                    dialog!!.dismiss()
                }
                else->{
                    dialog!!.inputName.error = "Tag Already Exist In the List"
                }
            }
        }
        dialog.textHint = "Tag"
        dialog.maxChar = MaxTagChars
        dialog.buildAndDisplay()
    }

    private fun deleteTags(){
        val ask = D_ask(gContext, "ARE YOU SURE ?"){
            if(it){
                DataBaseServices.deleteTags(tagAdapter.getSelectedIds())
                deaSelectTag()
            }
        }
        ask.buildAndDisplay()
    }

    private fun modifyTagClick(){
        val selectedItem = tagAdapter.list[tagAdapter.getSelected()[0]]
        var dialog : D_editItem? = null

        dialog = D_editItem(gContext){tag->
            when{
                DataBaseServices.isTagNotExist(tag) ->{
                    DataBaseServices.updateTags(selectedItem.id, tag)
                    deaSelectTag() //it also change the list
                    dialog?.dismiss()
                }
                else->{
                    dialog!!.inputName.error = "Tag Already Exist In the List"
                }
            }
        }

        dialog.textHint = "Tag"
        dialog.maxChar = MaxTagChars
        dialog.maxLine = 1
        dialog.textInput = selectedItem.value
        dialog.buildAndDisplay()

    }

    private fun initRv(){
        val layout = GridLayoutManager(gContext, 2)
        tagAdapter = A_tag(gContext){event, item ->
            rvEvent(event, item)
        }

        tagAdapter.changeList()
        tagRv.adapter = tagAdapter
        tagRv.layoutManager = layout
    }

    private fun rvEvent(event : Int, item : String){
        when(event){
            OnSelectMode -> activateSelectModeView()
            OpenItem -> openTag(item)
            SelectModeClick -> selectModeClick()
        }
    }

    private fun openTag(tag : String){
        val bundle = Bundle()

        bundle.putString(FgType, "Tags")
        bundle.putString(PassedData, tag)
        navController.navigate(R.id.f_WordsList, bundle)
    }

    private fun activateSelectModeView(){
        addBtn.visibility = View.GONE
        deleteBtn.visibility = View.VISIBLE
        modifyTag.visibility = View.VISIBLE
    }

    private fun deactivateSelectModeView(){
        addBtn.visibility = View.VISIBLE
        deleteBtn.visibility = View.GONE
        modifyTag.visibility = View.GONE
    }

    private fun selectModeClick(){
        if(tagAdapter.getSelectedCount() == 1){
            modifyTag.visibility = View.VISIBLE
        }else{
            modifyTag.visibility = View.GONE
        }
    }

    //endregion

    //region functions

    fun deaSelectTag() : Boolean{
        deactivateSelectModeView()
        return tagAdapter.deaSelect()
    }

    //endregion

}