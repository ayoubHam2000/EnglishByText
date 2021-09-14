package com.example.englishbytext.Objects

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.REQUEST_PERMISSION_AUDIO
import com.example.englishbytext.Utilites.REQUEST_PERMISSION_CAMERA
import com.example.englishbytext.Utilites.REQUEST_PERMISSION_STORAGE
import java.io.File
import java.io.FileWriter
import kotlin.random.Random

object Lib {

    //region Permissions

    fun isStoragePermissionGranted(context : Context, fragment : Fragment): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v("-->INFO", "Permission is granted")
                true
            } else {
                Log.v("-->INFO", "Asking Permission")
                fragment.requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_PERMISSION_STORAGE
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("-->INFO", "Permission is granted")
            true
        }
    }

    fun isStorageWritePermissionGranted(context : Context, fragment : Fragment): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v("-->INFO", "Permission is granted")
                true
            } else {
                Log.v("-->INFO", "Asking Permission")
                fragment.requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_STORAGE
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("-->INFO", "Permission is granted")
            true
        }
    }

    fun isCameraPermissionGranted(context : Context, fragment: Fragment) : Boolean{
        return if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED){
            Log.v(">>|", "Permission is granted")
            true
        }else{
            Log.v(">>|", "Asking Permission")
            fragment.requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_PERMISSION_CAMERA
            )
            false
        }
    }

    fun isAudioPermissionGranted(context: Context, fragment: Fragment): Boolean {
        val permission = Manifest.permission.RECORD_AUDIO
        val granted = PackageManager.PERMISSION_GRANTED
        return if (ActivityCompat.checkSelfPermission(context, permission) == granted) {
            Log.v(">>|", "Permission is granted")
            true
        } else {
            Log.v(">>|", "Asking Permission")
            fragment.requestPermissions(arrayOf(permission), REQUEST_PERMISSION_AUDIO)
            Log.d("PERMISSION", "not Granted")
            false
        }
    }

    //region for activity

    fun isStoragePermissionGranted(context : Context, activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v("INFO", "Permission is granted")
                true
            } else {
                Log.v("INFO", "Permission is revoked")
                showMessage(context, R.string.permission_is_not_granted)
                ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("INFO", "Permission is granted")
            true
        }
    }

    //endregion
    //endregion


    //region Keyboard

    fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
                context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(context: Context) {
        val imm: InputMethodManager =
                context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    fun showKeyboardTo(context: Context, view: View){
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun showKeyboardToDialog(dialog: Dialog){
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    //endregion

    //region Background

    fun changeBackgroundTint(context: Context, color: Int, background: View?){
        background?.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, color)
        )
    }

    fun changeBackgroundTint(color: Int, background: View?){
        background?.backgroundTintList = ColorStateList.valueOf(color)
    }

    //endregion

    //region menu
    fun initPopupMenu(context: Context, view: View, menu: Int) : PopupMenu {
        val popUpMenu = PopupMenu(context, view)
        popUpMenu.inflate(menu)
        view.setOnClickListener {
            forceShowIconForMenu(popUpMenu)
            popUpMenu.show()
        }

        /*popUpMenu.setOnMenuItemClickListener {
            when(it.itemId){
                com.example.novelwords.R.id.sortItem -> refreshFragments()
                com.example.novelwords.R.id.displayHide -> println("disolay Hide")
            }
            true
        }*/

        return popUpMenu
    }

    private fun forceShowIconForMenu(popupMenu: PopupMenu){
        try{
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenu)
            menu.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(menu, true)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    //endregion

    //region utilities

    fun fromSecondsToDate(t: Long) : String{
        var time = t / 1000
        val days = time / (3600*24)
        time -= days * 3600 * 24
        val hours = time / 3600
        time -= hours * 3600
        val minute = time / 60
        time -= minute * 60
        val seconds = time

        if(days == 0L && hours == 0L){
            return makeTime(minute, seconds, "m", "s")
        }else if(days == 0L){
            return makeTime(hours, minute, "h", "m")
        }
        return makeTime(days, hours, "d", "h")
    }

    private fun makeTime(a: Long, b: Long, x: String, y: String) : String{
        var result = ""
        result += if(a < 10){
            "0$a$x : "
        }else{
            "$a$x : "
        }

        result += if(b < 10){
            "0$b$y"
        }else{
            "$b$y"
        }
        return result
    }

    fun getBin(name: String) : String{
        var r = ""
        for(c in name){
            r += c.toInt().toString() + " "
        }
        return r
    }

    fun showMessage(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun shortMessage(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showMessage(context: Context, message: Int){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun printLog(s : String, important : Boolean = true){
        if(important){
            println(">> $s")
        }else{
            println(">>| $s")
        }
    }

    fun animFragment(enterAnim: Int, exitAnim: Int) : NavOptions.Builder{
        val navBuilder = NavOptions.Builder()
        navBuilder.setEnterAnim(enterAnim).setExitAnim(exitAnim)
        //.setPopEnterAnim(R.anim.slide_left).setPopExitAnim(R.anim.slide_right)
        return navBuilder
    }

    fun writeFileOnInternalStorage(context: Context, sBody: String?) {
        val theFile = File(context.getExternalFilesDir(""), "treeData")
        var data = ""
        try {
            val writer = FileWriter(theFile)
            writer.append(sBody)
            writer.flush()
            writer.close()
            showMessage(context, data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //endregion

    //region other

    fun pickRandomColor() : Int{
        val r = Random.nextInt(0, 256)
        val g = Random.nextInt(0, 256)
        val b = Random.nextInt(0, 256)
        return Color.rgb(r, g, b)
    }

    fun Int.colorWhiter(per : Int) : Int{
        if(per in 0..255){
            val r = Color.red(this)
            val g = Color.green(this)
            val b = Color.blue(this)
            return Color.argb(per, r, g, b)
        }
        return this
    }

    fun copyContent(context : Context, label : String ,n : String){
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, n)
        clipboard.setPrimaryClip(clip)
    }

    //endregion

}