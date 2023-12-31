package ph.edu.rv_realm_quiz.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import ph.edu.rv_realm_quiz.adapters.BooksAdapter
import ph.edu.rv_realm_quiz.databinding.ActivityBooksBinding
import ph.edu.rv_realm_quiz.dialogs.AddBookDialog
import ph.edu.rv_realm_quiz.models.Books
import ph.edu.rv_realm_quiz.realm.BookRealm
import ph.edu.rv_realm_quiz.realm.RealmDatabase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import java.util.Date
import java.util.Locale

class ActivityBooks : AppCompatActivity(), BooksAdapter.BooksAdapterInterface, AddBookDialog.RefreshDataInterface {
    private lateinit var binding: ActivityBooksBinding
    private lateinit var adapter: BooksAdapter
    private lateinit var booksList: ArrayList<Books>
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var database = RealmDatabase()

    private val swipeToDeleteCallback = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val books = adapter.getBooksId(position)

            AlertDialog.Builder(this@ActivityBooks)
                .setTitle("Delete")
                .setMessage("Are you sure you want to archive this?")
                .setPositiveButton("Archive") { _, _ ->
                    adapter.onItemDismiss(position)

                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // User clicked Cancel, dismiss the dialog
                    adapter.notifyItemChanged(position)
                    dialog.dismiss()
                }
                .show()
            getBooks()

        }
    }


    override fun refreshData(){
        getBooks()
    }

    override fun onResume() {
        super.onResume()
        //TODO: REALM DISCUSSION HERE
        getBooks()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManger = LinearLayoutManager(this)
        binding.rvBooks.layoutManager = layoutManger

        booksList = arrayListOf()
        adapter = BooksAdapter(booksList, this, this, supportFragmentManager)
        binding.rvBooks.adapter = adapter

        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvBooks)

        getBooks()

        binding.fab.setOnClickListener{
            val addBookDialog = AddBookDialog()
            addBookDialog.refreshDataCallback = this
            addBookDialog.show(supportFragmentManager, "AddBookDialog")
        }
//        getOwners()

    }

    private fun mapBooks(books: BookRealm): Books {
        return Books(
            id = books.id.toHexString(),
            bookName = books.name,
            author = books.author,
            pages = books.pages,
            progress = books.progress,
            dateBookAdded = Date(books.dateBookAdded),
            dateBookModified = Date(books.dateBookModified),
            dateBookPublished = Date(books.dateBookPublished)

        )
    }


    fun getBooks() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllBooks"))

        scope.launch(Dispatchers.IO) {
            val books = database.getAllBooks()
            val booksList = arrayListOf<Books>()
            booksList.addAll(
                books.map {
                    mapBooks(it)
                }
            )
            withContext(Dispatchers.Main) {
                adapter.updateBookList(booksList)
                adapter.notifyDataSetChanged()
                binding.empty.text = if (booksList.isEmpty()) "No Books Yet..." else ""
            }
        }
    }

    override fun archiveBook(bookId: ObjectId, position: Int) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("archiveBook"))
        scope.launch(Dispatchers.IO) {
            val book = booksList[position]
            database.archiveBook(book)
            withContext(Dispatchers.Main){
                booksList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.updateBookList(database.getAllBooks().map {mapBooks(it)} as ArrayList<Books>)
                Snackbar.make(binding.root, "Book Archived Successfully", Snackbar.LENGTH_LONG).show()
            }

        }
    }

}