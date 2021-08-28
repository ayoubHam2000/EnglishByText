package com.example.englishbytext.Fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.englishbytext.Classes.Objects.D_TextFont
import com.example.englishbytext.Classes.Objects.D_TextFontType
import com.example.englishbytext.Classes.Objects.D_textSize
import com.example.englishbytext.Interfaces.NotifyActivity
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.Objects.Setting
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.OnProcess
import com.example.englishbytext.Utilites.OpenSettings
import com.example.englishbytext.Utilites.RefreshData
import kotlin.concurrent.thread


class F_Settings : Fragment() {

    //region init
    private lateinit var gContext : Context
    private val layout = R.layout.f__settings
    private var listener : NotifyActivity? = null
    private val FILE_PATH_CODE = 100
    private val FILE_SAVE_FOLDER = 101

    //view
    private lateinit var darkModeSwitch : SwitchCompat
    private lateinit var textSize : TextView
    private lateinit var textStyle : TextView
    private lateinit var textFont : TextView
    private lateinit var saveData : ImageView
    private lateinit var loadData : ImageView

    private lateinit var textSizeSelected : TextView
    private lateinit var textStyleSelected : TextView
    private lateinit var textFontSelected : TextView



    private fun initView(view: View){
        listener?.notifyActivity(OpenSettings)

        darkModeSwitch = view.findViewById(R.id.darkModeSwitch)
        textSize = view.findViewById(R.id.textSize)
        textStyle = view.findViewById(R.id.textStyle)
        textFont = view.findViewById(R.id.textFont)
        saveData = view.findViewById(R.id.saveData)
        loadData = view.findViewById(R.id.loadData)

        textSizeSelected = view.findViewById(R.id.textSizeSelected)
        textStyleSelected = view.findViewById(R.id.textStyleSelected)
        textFontSelected = view.findViewById(R.id.textFontSelected)


        initFun()
    }

    private fun initFun(){
        darkModeSwitch.isChecked = Setting.isDarkMode
        textSizeSelected.text = Setting.getTextFountText()
        textStyleSelected.text = Setting.getTextStyleText()
        textFontSelected.text = Setting.fetTextFontText()

        darkModeSwitch.setOnClickListener { setDarkMode() }

        saveData.setOnClickListener { startSaveData() }

        loadData.setOnClickListener { startLoadingData() }

        textSize.setOnClickListener { changeTextSize() }

        textStyle.setOnClickListener { textStyle() }

        textFont.setOnClickListener { textFont() }
    }

    //endregion

    //region
    private fun setDarkMode(){
        if(darkModeSwitch.isChecked){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Setting.setIsDarkMode("true")
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Setting.setIsDarkMode("false")
        }
    }

    private fun startSaveData(){
        //if(Lib.isStorageWritePermissionGranted(gContext, this)) {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_TITLE, "")
            startActivityForResult(intent, FILE_SAVE_FOLDER)
        //}
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
        startActivityForResult(intent, FILE_PATH_CODE)
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
            textSizeSelected.text = Setting.getTextFountText()
        }
        dialog.buildAndDisplay()
    }

    private fun textStyle(){
        val dialog = D_TextFont(gContext){
            textStyleSelected.text = Setting.getTextStyleText()
        }
        dialog.buildAndDisplay()
    }

    private fun textFont(){
        val dialog = D_TextFontType(gContext){
            textFontSelected.text = Setting.fetTextFontText()
        }
        dialog.buildAndDisplay()
    }

    //endregion

    //region override

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            FILE_PATH_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let {
                        loadData(it)
                    }
                } else {
                    println("-->couldn't get path")
                }
            }
            FILE_SAVE_FOLDER -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let {
                        saveFile(it)
                    }
                } else {
                    println("-->couldn't get path for save file")
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? NotifyActivity
        if (listener == null) {
            throw ClassCastException("$context must implement OnArticleSelectedListener")
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gContext = view.context
        initView(view)
    }

    //endregion

}