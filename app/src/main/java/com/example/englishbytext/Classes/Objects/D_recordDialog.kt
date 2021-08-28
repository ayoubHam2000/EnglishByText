package com.example.englishbytext.Classes.Objects

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.englishbytext.Classes.Custom.ChronometerController
import com.example.englishbytext.Classes.Custom.MediaAudio
import com.example.englishbytext.Dialogs.MyDialogBuilder
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.R

class D_recordDialog(context : Context, val event : (String) -> Unit) : MyDialogBuilder(context, R.layout.d_recorder) {

    //region view
    private lateinit var recordLayout : LinearLayout
    private lateinit var recordState : TextView
    private lateinit var recordTimer : Chronometer
    private lateinit var recordOrNotView : ImageView
    private lateinit var resetView : ImageView
    //endregion

    //region vars

    private var mediaPlayer : MediaAudio = MediaAudio(context)
    lateinit var recordChronometerController: ChronometerController

    //endregion

    override fun initView(builderView: View) {
        //region view
        val approve : ImageView = builderView.findViewById(R.id.d_add)
        val dismiss : ImageView = builderView.findViewById(R.id.d_dismiss)

        recordLayout = builderView.findViewById(R.id.recordLayout)
        recordState = builderView.findViewById(R.id.recordState)
        recordTimer = builderView.findViewById(R.id.recordTimer)
        recordOrNotView = builderView.findViewById(R.id.recordOrNotView)
        resetView = builderView.findViewById(R.id.resetView)

        //endregion

        recordChronometerController  = ChronometerController(recordTimer)
        dialog.setOnShowListener {
            approve.setOnClickListener {
                val outName = mediaPlayer.recordedFileName
                mediaPlayer.exitMedia(false)
                if(outName != null){
                    event(outName)
                }
                dismiss()
            }
            dismiss.setOnClickListener {
                onDismiss()
                dismiss()
            }
            dialog.setOnCancelListener {
                onDismiss()
            }
            recordOrNotView.setOnClickListener {
                recordClick()
            }
            resetView.setOnClickListener {
                resetOnClick()
            }
        }

        dialog.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun onDismiss(){
        mediaPlayer.exitMedia()
    }

    private fun recordClick(){
        if(mediaPlayer.recordClick()){
            Lib.changeBackgroundTint(context, R.color.red, recordOrNotView)
            recordState.text = context.getString(R.string.recording)
            recordChronometerController.startChronometer()
            resetView.visibility = View.VISIBLE
        }else{
            recordState.text = context.getString(R.string.PauseRecording)
            recordChronometerController.pauseChronometer()
            Lib.changeBackgroundTint(context, R.color.gray2, recordOrNotView)
        }
    }

    private fun resetOnClick(){
        mediaPlayer.recordClick(true)
        Lib.changeBackgroundTint(context, R.color.gray2, recordOrNotView)
        recordState.text = context.getString(R.string.not_recording)
        recordChronometerController.stopChronometer()
        resetView.visibility = View.INVISIBLE
    }

    fun exitMedia(){
        mediaPlayer.exitMedia()
        dismiss()
    }

}