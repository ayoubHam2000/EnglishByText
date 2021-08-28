package com.example.englishbytext.Fragments

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Adapters.A_Folders
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.FoldersManagement
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.*


class F_Folders : MyFragment() {


    //region var, int

    //====================================
    //++++++++++++++++++++++  vars
    //====================================
    private lateinit var folderAdapter : A_Folders


    //====================================
    //++++++++++++++++++++++  view
    //====================================
    private lateinit var recyclerView : RecyclerView
    private lateinit var btnAddFolder : ImageView
    private lateinit var btnDeleteFolder : ImageView
    private lateinit var pathView : TextView


    //====================================
    //++++++++++++++++++++++  init
    //====================================
    override fun getMainLayout(): Int {
        return R.layout.f_folders
    }

    override fun getNotifyListenerId(): Int {
        return OpenAllFoldersFrag
    }

    override fun initVar(view : View){
        recyclerView = view.findViewById(R.id.rv_Folders)
        btnAddFolder = activity?.findViewById(R.id.addFolder)!!
        btnDeleteFolder = activity?.findViewById(R.id.deleteFolder)!!
        pathView = activity?.findViewById(R.id.pathView)!!
    }

    override fun initFun(){
        initRecyclerView()

        pathView.text = FoldersManagement.getPath()
        btnAddFolder.setOnClickListener { addFolderClick() }
        btnDeleteFolder.setOnClickListener { deleteFoldersClick() }

    }

    //endregion

    //region Folders

    private fun addFolderClick(){
        var dialog : D_editItem? = null

        dialog = D_editItem(gContext){
            val path = FoldersManagement.getPath()

            if(DataBaseServices.isFolderExist(path, it)){
                Lib.showMessage(gContext, "Name Already Exist")
            }else if(!DataBaseServices.isFolderNameValid(it)){
                Lib.showMessage(gContext, "Name is Not Valid")
            }else{
                DataBaseServices.insertFolder(path, it)
                FoldersManagement.list.add(it)
                folderAdapter.notifyDataSetChanged()
                dialog!!.dismiss()
            }

        }

        dialog.textHint = "Folder Name"
        dialog.maxLine = 1
        dialog.maxChar = MaxSetName
        dialog.build()
        dialog.display()
    }

    private fun deleteFoldersClick(){
        val list = folderAdapter.getSelected()
        println("-->$list")
        DataBaseServices.deleteFolders(list)
    }

    private fun initRecyclerView(){
        val layoutManager = LinearLayoutManager(gContext)

        folderAdapter = A_Folders(gContext){action, folderName->
            recyclerViewAction(action, folderName)
        }

        folderAdapter.changeList()
        recyclerView.adapter = folderAdapter
        recyclerView.layoutManager = layoutManager
    }

    private fun recyclerViewAction(action : Int, folderName : String){
        when(action){
            OpenItem->{
                openItem(folderName)
            }
            OnSelectMode->{
                selectModeActionBarView()
            }
            OpenFolderContent->{
                openFolderContentClick(folderName)
            }
        }
    }

    private fun openItem(folderName: String){
        FoldersManagement.openFolder(folderName)
        pathView.text = FoldersManagement.getPath()
        folderAdapter.changeList()
    }

    private fun selectModeActionBarView(){
        if(folderAdapter.onSelectMode){
            btnAddFolder.visibility = View.GONE
            btnDeleteFolder.visibility = View.VISIBLE
        }else{
            btnAddFolder.visibility = View.VISIBLE
            btnDeleteFolder.visibility = View.GONE
        }
    }

    private fun openFolderContentClick(folderName: String){

    }

    //region back press

    fun folderPop() : Boolean{
        if(!folderAdapter.onSelectMode && FoldersManagement.path.isNotEmpty()){
            FoldersManagement.exitFolder()
            folderAdapter.changeList()
            pathView.text = FoldersManagement.getPath()
            return true
        }
        return false
    }

    fun deaSelectTag() : Boolean{
        val result = folderAdapter.deaSelectMode()
        selectModeActionBarView()
        return result
    }

    //endregion


    //endregion
}