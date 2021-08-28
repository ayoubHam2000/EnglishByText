package com.example.englishbytext.Classes.Objects

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Adapters.A_LastImageList
import com.example.englishbytext.Dialogs.MyDialogBuilder
import com.example.englishbytext.R

class D_lastImages(context : Context, val list : ArrayList<Uri>, val event: (Uri) -> Unit) : MyDialogBuilder(context, R.layout.d_last_images) {

    private lateinit var imagesRv : RecyclerView
    private lateinit var adapter : A_LastImageList

    override fun initView(builderView: View) {
        imagesRv = builderView.findViewById(R.id.imagesRv)

        initRecyclerView()

        dialog.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initRecyclerView(){
        val layoutManager = GridLayoutManager(context, 3)
        adapter = A_LastImageList(context){
            event(it)
        }

        imagesRv.adapter = adapter
        adapter.changeList(list)
        imagesRv.layoutManager = layoutManager

    }


}