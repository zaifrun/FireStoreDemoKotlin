package org.pondar.firestoredemokotlin

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class OkCancel2InputsDialog(
    context: Context?,
    title: String?,
    book: Book,
    clickOk: (Book, String, String) -> Unit
) {


    private var alert: AlertDialog.Builder = AlertDialog.Builder(context)

    init {
        alert.setTitle(title)
        val layout = LinearLayout(context)
        layout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layout.orientation = LinearLayout.VERTICAL
        val layout2 = LinearLayout(context)
        layout2.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        layout2.orientation = LinearLayout.HORIZONTAL
        val input1 = EditText(context)
        val input2 = EditText(context)
        input1.inputType = InputType.TYPE_CLASS_TEXT
        input2.inputType = InputType.TYPE_CLASS_TEXT
        input1.setText(book.author)
        input2.setText(book.title)
        val text1 = TextView(context)
        text1.text = "Author: "
        val text2 = TextView(context)
        text2.text = "Title:"
        layout.addView(text1)
        layout.addView(input1)
        layout.addView(text2)
        layout.addView(input2)
        layout.addView(layout2)
        alert.setView(layout)
        alert.setPositiveButton(R.string.ok) { dialog, whichButton ->
            clickOk(book, input1.text.toString(), input2.text.toString())
        }
    }

    fun show() {
        alert.show()
    }

}