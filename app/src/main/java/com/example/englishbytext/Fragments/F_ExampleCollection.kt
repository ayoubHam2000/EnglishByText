package com.example.englishbytext.Fragments

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Adapters.A_expCollection
import com.example.englishbytext.Classes.Objects.D_ask
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.MainSetting
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.*


class F_ExampleCollection : MyFragment() {

    //region init

    //====================================
    //++++++++++++++++++++++  Vars
    //====================================
    lateinit var expCollectionAdapter : A_expCollection

    //====================================
    //++++++++++++++++++++++  View
    //====================================
    private lateinit var addBtn : ImageView
    private lateinit var deleteBtn : ImageView
    private lateinit var modifyBtn : ImageView
    private lateinit var expCollectionRv : RecyclerView


    //====================================
    //++++++++++++++++++++++  init
    //====================================

    override fun getMainLayout(): Int {
        return R.layout.f_example_collection
    }

    override fun getNotifyListenerId(): Int {
        return OpenExamplesCollection
    }

    override fun initVar(view: View) {
        addBtn = activity?.findViewById(R.id.addExampleCollection)!!
        deleteBtn = activity?.findViewById(R.id.deleteExampleCollection)!!
        modifyBtn = activity?.findViewById(R.id.modifyExampleCollection)!!

        expCollectionRv = view.findViewById(R.id.expCollectionRv)
    }

    override fun initFun() {
        addBtn.setOnClickListener { addItem() }
        deleteBtn.setOnClickListener { deleteItems() }
        modifyBtn.setOnClickListener { modifyItemClick() }

        initRv()
    }

    //endregion

    private fun addItem(){
        var dialog : D_editItem? = null
        dialog = D_editItem(gContext){name->
            when{
                DataBaseServices.isExampleCollectionExist(name) ->{
                    DataBaseServices.insertExampleCollection(name)
                    expCollectionAdapter.changeList()
                    dialog!!.dismiss()
                }
                else->{
                    dialog!!.inputName.error = "Collection Already Exist In the List"
                }
            }
        }
        dialog.textHint = "Example Collection Name"
        dialog.maxChar = MaxTagChars
        dialog.buildAndDisplay()
    }

    private fun deleteItems(){
        val ask = D_ask(gContext, "ARE YOU SURE ? All Examples of this collection(s) will be deleted!!"){
            if(it){
                val list = expCollectionAdapter.getSelectedIds()
                DataBaseServices.deleteExamplesCollection(list)
                if (list.indexOf(MainSetting.selectedExamplesCollection) != -1)
                    MainSetting.selectedExamplesCollection = DEFAULT_EXAMPLE_COLLECTION
                deaSelectItems()
            }
        }
        ask.buildAndDisplay()
    }

    private fun modifyItemClick(){
        val selectedItem = expCollectionAdapter.list[expCollectionAdapter.getSelected()[0]]
        var dialog : D_editItem? = null

        dialog = D_editItem(gContext){item->
            when{
                DataBaseServices.isExampleCollectionExist(item) ->{
                    DataBaseServices.updateExampleCollection(selectedItem.id, item)
                    deaSelectItems() //it also change the list
                    dialog?.dismiss()
                }
                else->{
                    dialog!!.inputName.error = "Collection Already Exist In the List"
                }
            }
        }

        dialog.textHint = "Example Collection Name"
        dialog.maxChar = MaxTagChars
        dialog.maxLine = 1
        dialog.textInput = selectedItem.value
        dialog.buildAndDisplay()
    }

    private fun initRv(){
        val layout = GridLayoutManager(gContext, 1)
        expCollectionAdapter = A_expCollection(gContext){ event, item ->
            rvEvent(event, item)
        }

        expCollectionAdapter.changeList()
        expCollectionRv.adapter = expCollectionAdapter
        expCollectionRv.layoutManager = layout
    }

    private fun rvEvent(event : Int, item : Int){
        when(event){
            OnSelectMode -> activateSelectModeView()
            OpenItem -> openItemClick(item)
            SelectModeClick -> selectModeClick()
        }
    }

    private fun openItemClick(selectedCollectionId : Int){
        MainSetting.selectedExamplesCollection = selectedCollectionId
        val size = expCollectionAdapter.list.size
        println(">> ? ---")
        expCollectionAdapter.notifyDataSetChanged()
    }

    private fun activateSelectModeView(){
        addBtn.visibility = View.GONE
        deleteBtn.visibility = View.VISIBLE
        modifyBtn.visibility = View.VISIBLE
    }



    private fun selectModeClick(){
        if(expCollectionAdapter.getSelectedCount() == 1){
            modifyBtn.visibility = View.VISIBLE
        }else{
            modifyBtn.visibility = View.GONE
        }
    }

    private fun deactivateSelectModeView(){
        addBtn.visibility = View.VISIBLE
        deleteBtn.visibility = View.GONE
        modifyBtn.visibility = View.GONE
    }

    //region functions

    private fun deaSelectItems() : Boolean{
        deactivateSelectModeView()
        return expCollectionAdapter.deaSelect()
    }

    //endregion

    //region override

    override fun onBackPress() : Boolean{
        if (deaSelectItems()) return true
        return false
    }

    override fun onPause() {
        super.onPause()
        saveStatesMap["View1"] = expCollectionRv.layoutManager?.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()
        expCollectionRv.layoutManager?.onRestoreInstanceState(saveStatesMap["View1"])
    }

    //endregion
}