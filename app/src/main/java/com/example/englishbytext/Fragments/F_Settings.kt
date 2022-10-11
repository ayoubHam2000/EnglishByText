package com.example.englishbytext.Fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.example.englishbytext.Classes.Objects.D_TextStyle
import com.example.englishbytext.Classes.Objects.D_TextFontType
import com.example.englishbytext.Classes.Objects.D_textSize
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.Objects.MainSetting
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.OnProcess
import com.example.englishbytext.Utilites.OpenSettings
import com.example.englishbytext.Utilites.RefreshData
import kotlin.concurrent.thread


class F_Settings : MyFragment() {

    //region init

    //====================================
    //++++++++++++++++++++++  Vars
    //====================================
    private lateinit var resultLauncherSaver :  ActivityResultLauncher<Intent>
    private lateinit var resultLauncherLoader :  ActivityResultLauncher<Intent>

    //====================================
    //++++++++++++++++++++++  View
    //====================================
    private lateinit var darkModeSwitch : SwitchCompat
    private lateinit var textSize : TextView
    private lateinit var textStyle : TextView
    private lateinit var textFont : TextView
    private lateinit var saveData : ImageView
    private lateinit var loadData : ImageView

    private lateinit var textSizeSelected : TextView
    private lateinit var textStyleSelected : TextView
    private lateinit var textFontSelected : TextView

    //====================================
    //++++++++++++++++++++++  Init
    //====================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resultLauncherSaver = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == Activity.RESULT_OK){
                val data: Intent? = result.data
                data?.data?.let {
                    saveFile(it)
                }
            }
            else {
                println(">>>couldn't get path for save file")
            }
        }

        resultLauncherLoader = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == Activity.RESULT_OK){
                val data: Intent? = result.data
                data?.data?.let {
                    loadData(it)
                }
            }
            else {
                println(">>>couldn't get path")
            }
        }
    }

    override fun getMainLayout(): Int {
        return R.layout.f__settings
    }

    override fun getNotifyListenerId(): Int {
        return OpenSettings
    }

    override fun initVar(view: View) {
        darkModeSwitch = view.findViewById(R.id.darkModeSwitch)
        textSize = view.findViewById(R.id.textSize)
        textStyle = view.findViewById(R.id.textStyle)
        textFont = view.findViewById(R.id.textFont)
        saveData = view.findViewById(R.id.saveData)
        loadData = view.findViewById(R.id.loadData)

        textSizeSelected = view.findViewById(R.id.textSizeSelected)
        textStyleSelected = view.findViewById(R.id.textStyleSelected)
        textFontSelected = view.findViewById(R.id.textFontSelected)
    }

    override fun initFun(){
        darkModeSwitch.isChecked = MainSetting.isDarkMode.get()
        textSizeSelected.text = MainSetting.getTextSizeText()
        textStyleSelected.text = MainSetting.getTextStyleText()
        textFontSelected.text = MainSetting.getTextFontText()

        darkModeSwitch.setOnClickListener { setDarkMode() }

        saveData.setOnClickListener { startSaveData() }

        loadData.setOnClickListener { startLoadingData() }

        textSize.setOnClickListener { changeTextSize() }

        textStyle.setOnClickListener { textStyle() }

        textFont.setOnClickListener { textFont() }
    }

    //endregion

    //region frg
    private fun setDarkMode(){
        if(darkModeSwitch.isChecked){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            MainSetting.isDarkMode.set(true)
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MainSetting.isDarkMode.set(false)
        }
    }


    private fun startSaveData(){
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_TITLE, "")
        resultLauncherSaver.launch(intent)
    }

    private fun saveFile(uri : Uri){
        thread {
            listener?.notifyActivity(OnProcess, true)
            DataBaseServices.saveData(gContext, uri)
            Handler(gContext.mainLooper).post {
                Lib.showMessage(gContext, "SAVED ON : ${uri.path}")
                listener?.notifyActivity(OnProcess, false)
            }
        }
    }

    private fun startLoadingData(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        resultLauncherLoader.launch(intent)
    }



    private fun loadData(uri: Uri){
        listener?.notifyActivity(OnProcess, true)
        thread {
            DataBaseServices.loadData(gContext, uri)
            Handler(gContext.mainLooper).post{
                listener?.notifyActivity(OnProcess, false)
                listener?.notifyActivity(RefreshData)
            }
        }

    }

    private fun changeTextSize(){
        val dialog = D_textSize(gContext){
            textSizeSelected.text = MainSetting.getTextSizeText()
        }
        dialog.buildAndDisplay()
    }

    private fun textStyle(){
        val dialog = D_TextStyle(gContext){
            textStyleSelected.text = MainSetting.getTextStyleText()
        }
        dialog.buildAndDisplay()
    }

    private fun textFont(){
        val dialog = D_TextFontType(gContext){
            textFontSelected.text = MainSetting.getTextFontText()
        }
        dialog.buildAndDisplay()
    }

    //endregion
}