package com.example.englishbytext.Adapters

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.R
import com.squareup.picasso.Picasso
import java.io.File
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class A_LastImageList(val context: Context, val event : (Uri) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    private val list: ArrayList<Uri> = ArrayList()
    private val layout = R.layout.a_last_image_item
    private val queue = HashMap<Int, Thread>()
    private var working = false

    fun changeList(l: ArrayList<Uri>){
        list.clear()
        list.addAll(l)
        queue.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!){
        private val theImage = itemView?.findViewById<ImageView>(R.id.theImage)

        fun bindView(position: Int){
            theImage?.setImageBitmap(null)
            theImage?.setOnClickListener {
                event(list[position])
            }

            val b = Thread{
                Handler(context.mainLooper).post {
                    val i = list[position]
                    Picasso.get().load(i).fit().centerCrop().into(theImage)
                    queue.remove(position)
                    if(queue.isEmpty()){
                        working = false
                    }else{
                        queue[queue.keys.first()]!!.start()
                    }
                }
            }
            queue[position] = b

            if(!working && queue.isNotEmpty()){
                working = true
                queue[queue.keys.first()]!!.start()
            }
        }

    }

    //region override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindView(position)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        queue.remove(holder.adapterPosition)
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return list.count()
    }


    //endregion
}