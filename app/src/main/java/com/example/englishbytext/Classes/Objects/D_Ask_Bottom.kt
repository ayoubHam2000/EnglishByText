package com.example.englishbytext.Classes.Objects

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.englishbytext.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class D_Ask_Bottom(val event : (Boolean) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var approve : TextView

    private fun getMainLayout() : Int{
        return R.layout.d_undo
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getMainLayout(), container, false)
    }

    override fun onStart() {
        super.onStart()

        val view = requireView()
        approve = view.findViewById(R.id.approve)

        approve.setOnClickListener {
            event(true)
            dismiss()
        }

    }

    override fun onCancel(dialog: DialogInterface) {
        event(false)
        super.onCancel(dialog)
    }


}