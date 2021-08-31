package com.example.englishbytext.Classes.Objects

import android.annotation.SuppressLint
import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Adapters.Dialog.A_copy_to_folder
import com.example.englishbytext.Dialogs.MyDialogBuilder
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.R

class D_copy_to_folder(context : Context, val event : (String) -> Unit) : MyDialogBuilder(context, R.layout.d_copy_to_folder) {

    lateinit var copyToFolderAdapter : A_copy_to_folder
    lateinit var approve : ImageView
    lateinit var pathText : TextView
    lateinit var foldersRv : RecyclerView

    @SuppressLint("SetTextI18n")
    override fun initView(builderView: View) {
        //view
        approve = builderView.findViewById(R.id.d_add)
        pathText = builderView.findViewById(R.id.pathText)
        foldersRv = builderView.findViewById(R.id.foldersRv)

        //init view
        pathText.text = "Copy To ./"

        //btn
        approve.setOnClickListener { copyToFolderClick() }

        //fun
        dialogCustomize()
        initRv()

    }

    private fun dialogCustomize(){
        dialog.setCancelable(false)
        dialog.setOnKeyListener { _, keyCode, e ->
            if (e.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                if(!copyToFolderAdapter.back()){
                    dismiss()
                }
            }
            false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initRv(){
        val layoutManger = LinearLayoutManager(context)

        copyToFolderAdapter = A_copy_to_folder(context){
            //when back press or opening folder
            pathText.text = "Copy To " + copyToFolderAdapter.getPath()
        }
        copyToFolderAdapter.changeList()

        foldersRv.adapter = copyToFolderAdapter
        foldersRv.layoutManager = layoutManger
    }

    private fun copyToFolderClick(){
        if(copyToFolderAdapter.isPathNotEmpty()){
            event(copyToFolderAdapter.getPath())
            dismiss()
        }else{
            Lib.showMessage(context, "Can't copy to ./")
        }

    }




}