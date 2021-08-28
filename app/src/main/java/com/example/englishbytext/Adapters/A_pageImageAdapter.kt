package com.example.englishbytext.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.example.englishbytext.Objects.MediaManagement
import com.example.englishbytext.Objects.WordsManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.IMAGE_FOLDER


class A_pageImageAdapter(val context: Context) : PagerAdapter() {

    //region init
    private val layout = R.layout.d_image_display
    private val list = MediaManagement.images

    //view
    private lateinit var imageView : ImageView
    private lateinit var imageTitle : TextView
    //endregion

    //region functions
    private fun configView(view: View, position: Int){
        imageView = view.findViewById(R.id.actualImageView)
        imageTitle = view.findViewById(R.id.imageName)

        setImage(position)
        setTitle(position)
    }

    private fun setTitle(position: Int){
        val imageName = list[position].word
        val t = "#${position + 1} $imageName"
        imageTitle.text = t
    }

    private fun setImage(position: Int){
        val imageName = list[position].value
        val path = context.getExternalFilesDir("/$IMAGE_FOLDER")!!.absolutePath
        val bitmap = BitmapFactory.decodeFile("$path/$imageName")
        imageView.setImageBitmap(bitmap)
    }
    //endregion

    //region override
    override fun getCount(): Int {
        return list.count()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as (RelativeLayout)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(layout, container, false)

        configView(view, position)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }

    //endregion
}