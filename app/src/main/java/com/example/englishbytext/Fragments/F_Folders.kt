package com.example.englishbytext.Fragments

import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
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
    private lateinit var modifyFolder : ImageView
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

    override fun initVar(view: View){
        btnAddFolder = activity?.findViewById(R.id.addFolder)!!
        btnDeleteFolder = activity?.findViewById(R.id.deleteFolder)!!
        modifyFolder = activity?.findViewById(R.id.modifyFolder)!!

        recyclerView = view.findViewById(R.id.rv_Folders)
        pathView = view.findViewById(R.id.pathView)!!
    }

    override fun initFun(){
        initRecyclerView()

        pathView.text = FoldersManagement.getPath()

        pathView.setOnClickListener { pathViewClick() }
        btnAddFolder.setOnClickListener { addFolderClick() }
        btnDeleteFolder.setOnClickListener { deleteFoldersClick() }
        modifyFolder.setOnClickListener { modifyFolderClick() }

    }

    //endregion

    //region Folders

    private fun pathViewClick(){
        if(FoldersManagement.getPath() != "."){
            openFolderContentClick(FoldersManagement.getPath())
        }
    }

    private fun addFolderClick(){
        var dialog : D_editItem? = null

        dialog = D_editItem(gContext){
            val path = FoldersManagement.getPath()

            if(DataBaseServices.isFolderExist("$path/$it")){
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
        val list = folderAdapter.getSelectedFormat()
        DataBaseServices.deleteFolders(list)
        folderAdapter.changeList()
        deaSelectFolder()
    }

    private fun modifyFolderClick(){
        val selectedItem = folderAdapter.list[folderAdapter.getSelected()[0]]
        val path = FoldersManagement.getPath() + "/" + selectedItem
        var dialog : D_editItem? = null

        dialog = D_editItem(gContext){folderNewName->
            val newPath = FoldersManagement.getPath() + "/" + folderNewName
            when{
                !DataBaseServices.isFolderExist(newPath) ->{
                    DataBaseServices.updateFolderName(path, newPath)
                    deaSelectFolder() //it also change the list
                    dialog?.dismiss()
                }
                else->{
                    dialog!!.inputName.error = "Tag Already Exist In the List"
                }
            }
        }

        dialog.textHint = "Folder Name"
        dialog.maxChar = MaxTagChars
        dialog.maxLine = 1
        dialog.textInput = selectedItem
        dialog.buildAndDisplay()
    }

    private fun initRecyclerView(){
        val layoutManager = LinearLayoutManager(gContext)

        folderAdapter = A_Folders(gContext){ action, folderName->
            recyclerViewAction(action, folderName)
        }

        folderAdapter.changeList()
        recyclerView.adapter = folderAdapter
        recyclerView.layoutManager = layoutManager

        val itemTouchHelper = ItemTouchHelper(getItemTouchDeleter())
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun recyclerViewAction(action: Int, folderName: String){
        when(action){
            OpenItem -> {
                openItem(folderName)
            }
            OnSelectMode -> {
                selectModeActionBarView()
            }
            OpenFolderContent -> {
                val path = FoldersManagement.getPath() + "/" + folderName
                openFolderContentClick(path)
            }
            SelectModeClick ->{
                selectModeClick()
            }
        }
    }

    private fun getItemTouchDeleter() : ItemTouchHelper.SimpleCallback{
        return object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val path = FoldersManagement.getPath() + "/" + folderAdapter.list[viewHolder.absoluteAdapterPosition]
                openFolderContentClick(path)
            }

            override fun onChildDraw(c: Canvas,
                                     recyclerView: RecyclerView,
                                     viewHolder: RecyclerView.ViewHolder,
                                     dX: Float,
                                     dY: Float,
                                     actionState: Int,
                                     isCurrentlyActive: Boolean) {
                try {

                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        viewHolder.itemView.translationX = dX / 3
                    } else {
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
            modifyFolder.visibility = View.VISIBLE
        }else{
            btnAddFolder.visibility = View.VISIBLE
            btnDeleteFolder.visibility = View.GONE
            modifyFolder.visibility = View.GONE
        }
    }

    private fun openFolderContentClick(path: String){
        val bundle = Bundle()

        bundle.putString(FgType, "Folders")
        bundle.putString(PassedData, path)
        navController.navigate(R.id.f_WordsList, bundle)
    }

    private fun selectModeClick(){
        if(folderAdapter.getSelectedCount() == 1){
            modifyFolder.visibility = View.VISIBLE
        }else{
            modifyFolder.visibility = View.GONE
        }
    }

    //region back press

    private fun folderPop() : Boolean{
        if(!folderAdapter.onSelectMode && FoldersManagement.path.isNotEmpty()){
            FoldersManagement.exitFolder()
            folderAdapter.changeList()
            pathView.text = FoldersManagement.getPath()
            return true
        }
        return false
    }

    private fun deaSelectFolder() : Boolean{
        val result = folderAdapter.deaSelect()
        selectModeActionBarView()
        return result
    }

    //endregion

    //endregion

    //region override

    override fun onBackPress(): Boolean {
        if(folderPop()) return true
        if(deaSelectFolder()) return true
        return false
    }

    //endregion

}