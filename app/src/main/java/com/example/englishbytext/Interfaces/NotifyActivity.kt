package com.example.englishbytext.Interfaces

interface NotifyActivity {
    fun notifyActivity(event : Int, onProcess : Boolean = false)
    fun navigateFragment(id : Int)
}