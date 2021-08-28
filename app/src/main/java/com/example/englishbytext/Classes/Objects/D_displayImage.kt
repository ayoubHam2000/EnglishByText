package com.example.englishbytext.Classes.Objects

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.WindowManager
import androidx.viewpager.widget.ViewPager
import com.example.englishbytext.Adapters.A_pageImageAdapter
import com.example.englishbytext.Dialogs.MyDialogBuilder
import com.example.englishbytext.R

class D_displayImage(context : Context, private val position : Int) : MyDialogBuilder(context, R.layout.d_image_frame) {

    lateinit var viewPager : ViewPager

    @SuppressLint("ClickableViewAccessibility")
    override fun initView(builderView: View) {
        viewPager = builderView.findViewById(R.id.viewPage)


        dialog.setOnShowListener {
            val pageAdapter = A_pageImageAdapter(context)
            viewPager.adapter = pageAdapter
            viewPager.currentItem = position
            viewPager
        }
        dialog.window?.setBackgroundDrawableResource(R.color.transparentForWordInfo)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog.window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }



}