package ph.edu.rv_realm_quiz.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import org.mongodb.kbson.BsonObjectId
import ph.edu.rv_realm_quiz.adapters.ArchivedBooksAdapter
import ph.edu.rv_realm_quiz.adapters.FavBooksAdapter
import ph.edu.rv_realm_quiz.databinding.ActivityArchivedBinding
import ph.edu.rv_realm_quiz.databinding.ActivityFavoritesBinding
import ph.edu.rv_realm_quiz.models.Books
import ph.edu.rv_realm_quiz.realm.BookRealm
import ph.edu.rv_realm_quiz.realm.RealmDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityArchivedBooks: AppCompatActivity(), ArchivedBooksAdapter.ArchivedBooksAdapterInterface {
    private lateinit var binding: ActivityArchivedBinding
    private lateinit var adapter: ArchivedBooksAdapter
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
            val bookId = adapter.getBooksId(position)

            AlertDialog.Builder(this@ActivityArchivedBooks)
                .setTitle("Delete")
                .setMessage("Are you sure you want to permanently delete this?")
                .setPositiveButton("Delete") { _, _ ->
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchivedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManger = LinearLayoutManager(this)
        binding.rvArchivedBooks.layoutManager = layoutManger

        booksList = arrayListOf()
        adapter = ArchivedBooksAdapter(booksList, this, this)
        binding.rvArchivedBooks.adapter = adapter

        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvArchivedBooks)

        getBooks()

        if(booksList.isNotEmpty()){
            binding.btnRestoreAll.isEnabled = true
            binding.btnDeleteAll.isEnabled = true
        }
        else{
            binding.btnRestoreAll.isEnabled = false
            binding.btnDeleteAll.isEnabled = false
        }

        binding.btnRestoreAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Restore All")
                .setMessage("Are you sure you want to restore all archived books?")
                .setPositiveButton("Restore All") { _, _ ->
                    val coroutineContext = Job() + Dispatchers.IO
                    val scope = CoroutineScope(coroutineContext + CoroutineName("RestoreAllBook"))
                    scope.launch(Dispatchers.IO) {
                        database.unarchiveAllBook()
                        withContext(Dispatchers.Main) {
                            // You can update the UI or show a message if needed
                            Toast.makeText(this@ActivityArchivedBooks, "All Books Unarchived!", Toast.LENGTH_LONG).show()
                            refreshData()
                        }

                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // User clicked Cancel, dismiss the dialog
                    dialog.dismiss()
                }
                .show()
        }

        binding.btnDeleteAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete All")
                .setMessage("Are you sure you want to delete all archived books?")
                .setPositiveButton("Delete All") { _, _ ->
                    val coroutineContext = Job() + Dispatchers.IO
                    val scope = CoroutineScope(coroutineContext + CoroutineName("DeleteAllBook"))
                    scope.launch(Dispatchers.IO) {
                        database.deleteAllBook()
                        withContext(Dispatchers.Main) {
                            // You can update the UI or show a message if needed
                            Toast.makeText(this@ActivityArchivedBooks, "All Archived Books Deleted!", Toast.LENGTH_LONG).show()
                            refreshData()
                        }

                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // User clicked Cancel, dismiss the dialog
                    dialog.dismiss()
                }
                .show()

        }



    }

    override fun unArchiveBook(bookId: String, position: Int) {

    }

    override fun deleteBook(bookId: String, position: Int) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("deleteBook"))
        scope.launch(Dispatchers.IO) {
            val book = booksList[position]
            val bookDelete = BsonObjectId(book.id)
            database.deleteBook(bookDelete)
            withContext(Dispatchers.Main){
                booksList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.updateBookList(database.getArchivedBooks().map {mapBooks(it)} as ArrayList<Books>)
                Snackbar.make(binding.root, "Book Deleted Successfully", Snackbar.LENGTH_LONG).show()
                getBooks()
            }
        }
    }

    override fun refreshData(){
        getBooks()
    }



    private fun mapBooks(books: BookRealm): Books {
        return Books(
            id = books.id.toHexString(),
            bookName = books.name,
            author = books.author,
            dateBookAdded = Date(books.dateBookAdded),
            dateBookModified = Date(books.dateBookModified),
            dateBookPublished = Date(books.dateBookPublished)

        )
    }

    fun getBooks() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadArchivedBooks"))

        scope.launch(Dispatchers.IO) {
            val books = database.getArchivedBooks()
            val booksList = arrayListOf<Books>()
            booksList.addAll(
                books.map {
                    mapBooks(it)
                }
            )
            withContext(Dispatchers.Main) {
                adapter.updateBookList(booksList)
                adapter.notifyDataSetChanged()
                binding.empty.text = if (booksList.isEmpty()) "No Archived Books Yet..." else ""
                binding.btnRestoreAll.isEnabled = booksList.isNotEmpty()
                binding.btnDeleteAll.isEnabled = booksList.isNotEmpty()
            }
        }
    }
}