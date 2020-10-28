package org.pondar.firestoredemokotlin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.SparseBooleanArray
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ArrayAdapter<Book>
    private var books = ArrayList<Book>()
    private var checked: SparseBooleanArray = SparseBooleanArray()
    private var registration: ListenerRegistration? = null
    private var context: Context? = null

    private fun addBook() {
        val newBook = Book(
            author = editAuthor.text.toString(),
            title = editTitle.text.toString()
        )
        db.collection("books")
            .add(newBook)
            .addOnSuccessListener { documentReference ->
                Log.d("Error", "DocumentSnapshot written with ID: " + documentReference.id)
            }
            .addOnFailureListener { e -> Log.w("Error", "Error adding document", e) }
    }

    private fun deleteAll() {
        val batch = db.batch()
        for (book in books) {
            val ref = db.collection("books").document(book.id)
            batch.delete(ref)
        }

        // Commit the batch
        batch.commit().addOnCompleteListener {
            checked.clear()
            books.clear()
            adapter.notifyDataSetChanged()
        }
    }

    private fun deleteSelected() {
        val size = checked.size()
        var removed = 0
        for (i in 0 until size) {
            val value = checked.valueAt(i - removed)
            if (value) //if true then remove
            {
                val position = checked.keyAt(i - removed)
                val book = adapter.getItem(position - removed)

                //delete from firebase
                db.collection("books").document(book!!.id).delete().addOnSuccessListener {
                    Log.d(
                        "Snapshot",
                        "DocumentSnapshot with id: ${book.id} successfully deleted!"
                    )
                }
                    .addOnFailureListener { e -> Log.w("Error", "Error deleting document", e) }
                adapter.remove(book) //delete from listview
                checked.delete(position - removed) //delete, because it is unchecked now
                removed++
            }
        }
        checked.clear() //after deletion nothing should be selected
        adapter.notifyDataSetChanged()
    }



    override fun onCreateContextMenu(
        menu: ContextMenu, v: View,
        menuInfo: ContextMenuInfo?)
    {
        val info = menuInfo as AdapterContextMenuInfo?
        var pos = -1
        if (info != null) pos = info.position
        if (pos == -1) menu.setHeaderTitle("Book options") else {
            val book = adapter.getItem(pos)
            if (book != null) menu.setHeaderTitle(book.title) else
                menu.setHeaderTitle("Book options")
        }
        menu.add(0, 0, 0, "Modify book")
        menu.add(0, 1, 1, "Cancel")
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.d("context item selected", "id: " + item.itemId)
        if (item.itemId == 0) //modify book
        {
            val info = item.menuInfo as AdapterContextMenuInfo
            val pos = info.position
            val book = adapter.getItem(pos)
            if (book != null) {
                Log.d("Book", "Modify book : $book")
                val dialog = OkCancel2InputsDialog(context, "Edit Book", book, ::clickOk)
                dialog.show()
            }
            return true
        }
        return super.onContextItemSelected(item)
    }

    private fun clickOk(book: Book, author: String, title: String) {
        db.collection("books").document(book.id)
            .update("author", author, "title", title)
    }

    private fun findBook(id: String?): Book? {
        for (book in books) {
            if (book.id == id) return book
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(applicationContext)

        addButton.setOnClickListener { addBook() }
        delete.setOnClickListener { deleteSelected() }
        deleteAll.setOnClickListener { deleteAll() }
        db = FirebaseFirestore.getInstance()

        books = ArrayList()
        //now check to see if have have something in the bag
        if (savedInstanceState != null) {
            Log.d("Books", "savedinstance state not null")
            if (savedInstanceState.containsKey("books")) {
                books = savedInstanceState.getParcelableArrayList("books")!!
            }
            if (savedInstanceState.containsKey("map")) {
                val map = savedInstanceState.getSerializable("map") as HashMap<Int, Boolean>?
                for ((key, value) in map!!) {
                    checked.put(key, value)
                }
            }
        }
        adapter = BookAdapter(this, books, checked)
        booklist.adapter = adapter
        registerForContextMenu(booklist)
        val query: Query = db.collection("books")

        registration = query.addSnapshotListener(listener)

    }

    override fun onStop() {
        registration!!.remove()
        super.onStop()
    }

    private var listener = EventListener<QuerySnapshot> { querySnapshot: QuerySnapshot?,
                                                          firebaseFirestoreException: FirebaseFirestoreException? ->
        run {
            val changes = querySnapshot!!.documentChanges
            for (change in changes) {
                if (change.type == DocumentChange.Type.ADDED) {
                    val doc: DocumentSnapshot = change.document
                    val book = doc.toObject(Book::class.java)
                    // Log.d("listening: add", doc.id + " => " + doc.data)
                    book?.id = doc.id
                    if (findBook(doc.id) == null) //no book with this id in list
                        adapter.add(book)
                } else if (change.type == DocumentChange.Type.REMOVED) {
                    //remove the id of the document
                    Log.d("listening", " book removed event")
                    val id = change.document.id
                    val b = findBook(id)
                    if (b != null) //we have a book, so remove it
                    {
                        adapter.remove(b)
                    }
                } else if (change.type == DocumentChange.Type.MODIFIED) {
                    val id = change.document.id
                    Log.d("listening", " book updated event - id = $id")
                    val foundBook = findBook(id)
                    val doc: DocumentSnapshot = change.document
                    val book = doc.toObject(Book::class.java)
                    if (foundBook != null) {
                        foundBook.author = book!!.author
                        foundBook.title = book.title
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList("books", books)
        val map = HashMap<Int, Boolean>()
        val size = checked.size()
        for (i in 0 until size) {
            val value = checked.valueAt(i)
            val key = checked.keyAt(i)
            map[key] = value
        }
        outState.putSerializable("map", map)
        Log.d("Books", "onSaveInstanceState - saving books")
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }
}