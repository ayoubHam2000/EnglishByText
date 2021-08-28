package com.example.englishbytext.Adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Classes.Objects.D_displayImage
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.MediaManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.AUDIO_FOLDER
import com.example.englishbytext.Utilites.IMAGE_FOLDER
import com.example.englishbytext.Utilites.OnSelectMode
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File

class A_imageMedia(
        val context: Context,
        private val theWordName: String,
        val event: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //region vars
    private val imageLayout = R.layout.a_image_item
    private val selected = HashMap<Int, Boolean>()
    private var isImageOnSelectMode = false
    var list = MediaManagement.images

    fun changeList(){
        MediaManagement.updateImagesList(theWordName)
        selected.clear()
        notifyDataSetChanged()
    }

    //endregion

    //region viewHolder

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val imageView : ImageView = itemView.findViewById(R.id.imageItem)
        private val selectView : ImageView = itemView.findViewById(R.id.imageSelected)

        fun bindView(position: Int){
            setImage(position)
            selectView(position)

            imageView.setOnClickListener {
                imageClick(position)
            }
            imageView.setOnLongClickListener {
                turnSelectModeOn(position)
                true
            }
        }

        private fun imageClick(position: Int){
            if(!isImageOnSelectMode){
                val dialog = D_displayImage(context, position)
                dialog.buildAndDisplay()
            }else{
                selectItems(position)
            }
        }

        private fun turnSelectModeOn(position: Int){
            if(!isImageOnSelectMode){
                println("On Select Mode")
                isImageOnSelectMode = true
                selected[position] = true
                selectView.visibility = View.VISIBLE
                event(OnSelectMode)
            }
        }

        private fun setImage(position: Int){
            val imageName = list[position].value
            val path = context.getExternalFilesDir("/$IMAGE_FOLDER")!!.absolutePath
            Picasso.get().load(File("$path/$imageName")).fit().centerCrop().into(imageView)
        }

        private fun selectView(position: Int){
            if(isImageOnSelectMode && (selected[position] != null && selected[position]!!)){
                selectView.visibility = View.VISIBLE
            }else{
                selectView.visibility = View.GONE
            }
        }

        private fun selectItems(position: Int){
            selected[position] = selected[position] == null || selected[position] == false
            notifyItemChanged(position)
        }
    }


    //endregion

    //region Selection Functions

    fun deleteItems(){
        val mainPath = context.getExternalFilesDir("/")!!.absolutePath
        if(isImageOnSelectMode){
            val res = ArrayList<Int>()
            val files = ArrayList<String>()

            for(item in selected.keys){
                if(selected[item]!!){
                    res.add(item)
                    files.add("$mainPath/$IMAGE_FOLDER/${list[item].value}")
                }
            }
            DataBaseServices.deleteImages(res)
            MediaManagement.deleteFiles(files)
        }
    }

    fun disableSelectImage() : Boolean{
        if(!isImageOnSelectMode) return false
        isImageOnSelectMode = false
        selected.clear()
        changeList()
        return true
    }

    //endregion

    //region override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val imageView = LayoutInflater.from(context).inflate(imageLayout, parent, false)
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ImageViewHolder).bindView(position)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    //endregion

}