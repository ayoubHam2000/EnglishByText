package com.example.englishbytext.Classes.Custom

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import com.example.englishbytext.Interfaces.EndPlaying
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.AUDIO_FOLDER
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MediaAudio(
        val context: Context
) {

    lateinit var endPlayingListener : EndPlaying

    //region var
    companion object CONST{
        const val NOT_INIT = -1
        const val NOT_RECORDING = 0
        const val RECORDING = 1
        const val NOT_PLAYING = 0
        const val PLAYING = 1
        const val PAUSED = 2
    }


    private var statusRecord = NOT_INIT
    var statusPlay = NOT_INIT


    var recordedFileName: String? = null
    var playedFileName: String? = null
    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null


    //endregion

    //region play
    fun playClick(fileName: String, reset: Boolean = false) : Boolean{
        //true playing , false not playing
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setOnCompletionListener {
                resetPlayer()
                endPlayingListener.notifyEndPlaying()
            }
            statusPlay = NOT_PLAYING
        }
        if(reset){
            resetPlayer()
            return false
        }

        if(playedFileName != null && playedFileName != fileName){
            resetPlayer()
            playAudio(fileName)
        }else{
            when(statusPlay){
                NOT_PLAYING -> {
                    playAudio(fileName)
                }
                PAUSED -> {
                    resumeAudio()
                }
                PLAYING -> {
                    pauseAudio()
                }
            }
        }
        return mediaPlayer!!.isPlaying
    }

    private fun playAudio(fileName: String){
        try{
            println("-->try playAudio audio : $playedFileName (status $statusPlay)")
            val file = context.getExternalFilesDir("$AUDIO_FOLDER/$fileName")
            mediaPlayer?.setDataSource(file?.absolutePath)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            statusPlay = PLAYING
            playedFileName = fileName
            println("-->success playing Audio : $playedFileName (status $statusPlay)")
        }catch (e: IOException) {
            e.printStackTrace()
            println("-->failed playing Audio : $playedFileName (status $statusPlay)")
            Lib.showMessage(context, R.string.something_went_wrong)
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        statusPlay = PAUSED
        println("-->pauseAudio audio : $playedFileName (status $statusPlay)")
    }

    private fun resumeAudio() {
        mediaPlayer?.start()
        statusPlay = PLAYING
        println("-->resumePlaying audio : $playedFileName (status $statusPlay)")
    }

    private fun resetPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        statusPlay = NOT_PLAYING
        println("-->resetPlayer audio : $playedFileName (status $statusPlay)")
    }

    //endregion

    //region record
    fun recordClick(reset: Boolean = false) : Boolean{
        var isRecording = false
        if(mediaRecorder == null){
            mediaRecorder = MediaRecorder()
            statusRecord = NOT_RECORDING
        }
        if(reset){
            resetRecord()
            discardRecoding()
            return isRecording
        }

        when(statusRecord){
            NOT_RECORDING -> {
                recordAudio()
                isRecording = statusRecord == RECORDING
            }
            RECORDING -> {
                pauseRecording()
                isRecording = false
            }
            PAUSED -> {
                resumeRecording()
                isRecording = true
            }
        }
        return isRecording
    }

    private fun recordAudio(){
        File(context.getExternalFilesDir("/$AUDIO_FOLDER").toString())
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss.SSS", Locale.US).format(Date())
        val name = "Audio_$timeStamp.m4a"
        val fileOutPut = context.getExternalFilesDir("/")!!.absolutePath
        println("-->try recordAudio audio : $name (status $statusRecord)")
        try {
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setOutputFile("$fileOutPut/$AUDIO_FOLDER/$name")
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder?.setAudioChannels(1)
            mediaRecorder?.setAudioSamplingRate(44100)
            mediaRecorder?.setAudioEncodingBitRate(96000)

            mediaRecorder?.prepare()
            mediaRecorder?.start()
            statusRecord = RECORDING
            recordedFileName = name
            println("-->success recordAudio audio : $name (status $statusRecord)")
        } catch (e: IOException) {
            println("-->failed recordAudio audio : $name (status $statusRecord)")
            Lib.showMessage(context, R.string.something_went_wrong)
            e.printStackTrace()
        }
    }

    private fun pauseRecording() {
        mediaRecorder?.pause()
        statusRecord = PAUSED
        println("-->pauseRecording audio : $recordedFileName (status $statusRecord)")
    }

    private fun resumeRecording() {
        mediaRecorder?.resume()
        statusRecord = RECORDING
        println("-->resumeRecording audio : $recordedFileName (status $statusRecord)")
    }

    private fun resetRecord() {
        mediaRecorder?.stop()
        mediaRecorder?.reset()
        statusRecord = NOT_RECORDING
        println("-->stopRecording audio : $recordedFileName (status $statusRecord)")
    }

    private fun discardRecoding() {
        if(recordedFileName != null){
            println("-->discardRecoding audio : $recordedFileName (status $statusRecord)")
            deleteFile(recordedFileName)
            recordedFileName = null
        }
    }
    //endregion

    //region utilities

    fun exitMedia(deleteAudio: Boolean = true){
        if(mediaPlayer != null && statusPlay != NOT_PLAYING){resetPlayer()}
        mediaPlayer?.release()
        mediaPlayer = null
        statusPlay = NOT_INIT
        playedFileName = null

        if(mediaRecorder != null && statusRecord != NOT_RECORDING){
            resetRecord()
            if(deleteAudio){ discardRecoding() }
        }
        mediaRecorder?.release()
        mediaRecorder = null
        statusRecord = NOT_INIT
        recordedFileName = null
        println("-->exit Media Release resource")
    }

    //listener
    fun setCustomClickListener(yourCustomListener: EndPlaying) {
        endPlayingListener = yourCustomListener
    }

    private fun deleteFile(name: String?) {
        val theFile = context.getExternalFilesDir("$AUDIO_FOLDER/$name")
        theFile?.delete()
        println("-->file $name deleted")
    }
    //endregion

}