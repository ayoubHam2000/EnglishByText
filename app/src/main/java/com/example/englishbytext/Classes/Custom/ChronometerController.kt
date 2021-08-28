package com.example.englishbytext.Classes.Custom

import android.os.SystemClock
import android.widget.Chronometer

class ChronometerController(val timer: Chronometer) {
    private var timeWhenStopped = 0L

    companion object CONST{
        const val NOT_START = 0
        const val ON_START = 1
        const val ON_PAUSE = 2
    }
    private var state = NOT_START

    //------------------------
    //methods
    fun startChronometer() {
        if(state == NOT_START){
            timer.base = SystemClock.elapsedRealtime()
            timer.start()
            state = ON_START
        }else if(state == ON_PAUSE){
            resumeChronometer()
        }
    }

    fun pauseChronometer() {
        if(state == ON_START){
            timeWhenStopped = timer.base - SystemClock.elapsedRealtime()
            timer.stop()
            state = ON_PAUSE
        }
    }

    private fun resumeChronometer() {
        timer.base = timeWhenStopped + SystemClock.elapsedRealtime()
        timer.start()
        state = ON_START
    }

    fun stopChronometer() {
        if(state != NOT_START){
            timer.base = SystemClock.elapsedRealtime()
            timer.stop()
            state = NOT_START
        }
    }

}