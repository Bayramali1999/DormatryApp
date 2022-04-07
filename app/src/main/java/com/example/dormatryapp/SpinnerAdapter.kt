package com.example.dormatryapp

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.dormatryapp.constants.listener.OnItemSelectListener

class SpinnerAdapter : DialogFragment() {

    private lateinit var title: String
    private lateinit var selectionArray: Array<String>
    private lateinit var listener: OnItemSelectListener
    private var ids: Int = 0

    fun getData(
        title: String,
        list: Array<String>,
        listener: OnItemSelectListener
    ) {
        this.title = title
        selectionArray = list
        this.listener = listener
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity
        if (activity != null) {
            val dialog = AlertDialog.Builder(activity)

            dialog.setTitle(title)
                .setSingleChoiceItems(selectionArray, 0) { _, it ->
                    this.ids = it
                }
                .setPositiveButton("Tanlash") { di, _ ->
                    this.listener.onItemSelected(this.ids)
                    ids = 0
                    di.dismiss()
                }
                .setNegativeButton("Ortga") { di, _ ->
                    di.dismiss()
                }
            return dialog.create()
        }
        return super.onCreateDialog(savedInstanceState)
    }

}