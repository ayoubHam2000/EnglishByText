package com.example.englishbytext.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Classes.Custom.MediaAudio
import com.example.englishbytext.Interfaces.EndPlaying
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.Objects.MediaManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.AUDIO_FOLDER
import com.example.englishbytext.Utilites.MaxTextChars
import com.example.englishbytext.Utilites.OnSelectMode
import kotlin.math.min

class A_audioMedia(
        val context: Context,
        private val theWordName: String,
        val event: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //region vars
    private val audioLayout = R.layout.a_audio_item
    private val selected = HashMap<Int, Boolean>()
    private var isOnSelectMode = false
    var list = MediaManagement.audios

    val mediaPlayer: MediaAudio = initMedia()
    private var selectedReadAudio = 0

    fun changeList(){
        MediaManagement.updateAudioList(theWordName)
        selected.clear()
        notifyDataSetChanged()
    }

    private fun initMedia() : MediaAudio{
        val mediaPlayer = MediaAudio(context)
        mediaPlayer.setCustomClickListener(object : EndPlaying {
            override fun notifyEndPlaying() {
                selectedReadAudio = -1
                notifyDataSetChanged()
            }
        })
        return mediaPlayer
    }

    //endregion

    //region viewHolder

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val audioId : TextView = itemView.findViewById(R.id.audioId)
        private val audioView : ImageView = itemView.findViewById(R.id.audioItem)
        private val selectView : ImageView = itemView.findViewById(R.id.selectItem)

        fun bindView(position: Int){
            val posId = "#${position + 1} ${getText(position)}"
            selectView(position)
            setAudioLayout(position)
            audioId.text = posId

            audioView.setOnClickListener {
                audioClick(position)
            }
            audioView.setOnLongClickListener {
                turnSelectModeOn(position)
                true
            }
        }

        private fun getText(position: Int) : String{
            val str = list[position].word
            val minChar = min(str.length, 8)

            if(minChar > 0){
                var res = str.substring(0, minChar)
                res += if(minChar != str.length) "..." else ""
                return res
            }
            return ""
        }

        private fun audioClick(position: Int){
            if(!isOnSelectMode){
                onAudioClick(position)
            }else{
                selectItems(position)
            }
        }

        private fun onAudioClick(position: Int){
            println(">>>Audio Click")
            val isPlayed = mediaPlayer.playClick(list[position].value)
            if(isPlayed){
                selectedReadAudio = position
            }
            notifyDataSetChanged()
        }

        private fun setAudioLayout(position: Int){
            if(mediaPlayer.statusPlay != MediaAudio.NOT_INIT && selectedReadAudio == position){
                when(mediaPlayer.statusPlay){
                    MediaAudio.PAUSED->{
                        Lib.changeBackgroundTint(context, R.color.wordEdit_item_audio_pause, audioView)
                    }
                    MediaAudio.PLAYING->{
                        Lib.changeBackgroundTint(context, R.color.wordEdit_item_audio_play, audioView)
                    }
                    MediaAudio.NOT_PLAYING->{
                        Lib.changeBackgroundTint(context, R.color.wordEdit_item_audio_stop, audioView)
                    }
                }
            }else{
                Lib.changeBackgroundTint(context, R.color.wordEdit_item_audio_stop, audioView)
            }
        }

        private fun turnSelectModeOn(position: Int){
            if(!isOnSelectMode){
                println("On Select Mode")
                isOnSelectMode = true
                selected[position] = true
                selectView.visibility = View.VISIBLE
                exitMedia()
                notifyDataSetChanged()
                event(OnSelectMode)
            }
        }

        private fun selectView(position: Int){
            if(isOnSelectMode && (selected[position] != null && selected[position]!!)){
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
        if(isOnSelectMode){
            val res = ArrayList<Int>()
            val files = ArrayList<String>()

            for(item in selected.keys){
                if(selected[item]!!){
                    res.add(item)
                    files.add("$mainPath/$AUDIO_FOLDER/${list[item].value}")
                }
            }
            DataBaseServices.deleteAudios(res)
            MediaManagement.deleteFiles(files)
        }
    }

    fun disableSelectAudio() : Boolean{
        if(!isOnSelectMode) return false
        isOnSelectMode = false
        selected.clear()
        changeList()
        return true
    }

    fun exitMedia(){
        mediaPlayer.exitMedia()
        notifyDataSetChanged()
    }

    //endregion

    //region override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val imageView = LayoutInflater.from(context).inflate(audioLayout, parent, false)
        return AudioViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AudioViewHolder).bindView(position)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    //endregion

}