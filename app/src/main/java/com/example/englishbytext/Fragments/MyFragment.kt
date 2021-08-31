package com.example.englishbytext.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.englishbytext.Interfaces.NotifyActivity
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.OpenCollectionFrag

abstract class MyFragment : Fragment() {

    protected var listener : NotifyActivity? = null
    protected lateinit var gContext : Context
    protected lateinit var navController : NavController

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
        return inflater.inflate(getMainLayout(), container, false)
    }

    override fun onStart() {
        super.onStart()

        val view = requireView()
        gContext = view.context
        navController = Navigation.findNavController(view)
        listener?.notifyActivity(getNotifyListenerId())

        initVar(view)
        initFun()
    }

    open fun onBackPress() : Boolean{
        return false
    }

    abstract fun getMainLayout() : Int
    abstract fun getNotifyListenerId() : Int
    abstract fun initVar(view : View)
    abstract fun initFun()

}