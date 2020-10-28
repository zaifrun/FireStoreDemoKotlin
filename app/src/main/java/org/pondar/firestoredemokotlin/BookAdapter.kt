package org.pondar.firestoredemokotlin

import android.content.Context
import android.util.SparseBooleanArray
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView

class BookAdapter(context: Context, books: List<Book>, private var checked: SparseBooleanArray) : ArrayAdapter<Book>(context, 0, books),
    OnCreateContextMenuListener {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var root = convertView
        if (root == null) {
            root = LayoutInflater.from(context).inflate(R.layout.bookitem, parent, false)
        }
        root?.findViewById<View>(R.id.bookHeading)?.setOnCreateContextMenuListener(this)
        val textView = root?.findViewById<TextView>(R.id.bookText)
        val book = getItem(position)
        textView?.text = book.toString()

        val checkBox = root?.findViewById<CheckBox>(R.id.bookChecked)
        val index = checked.indexOfKey(position)
        if (index >= 0) {
            if (checked.valueAt(index)) checkBox?.isChecked = checked.valueAt(index)
        } else checkBox?.isChecked = false

        checkBox?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) { //see if the position is in the checked array, if not then add it.
                checked.put(position, true)
            } else  //unchecked, so just remove it
            {
                checked.delete(position)
            }
        }
        return root!!
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {}
}