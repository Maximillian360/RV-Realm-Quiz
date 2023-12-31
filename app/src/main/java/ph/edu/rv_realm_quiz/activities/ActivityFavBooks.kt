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
import ph.edu.rv_realm_quiz.adapters.BooksAdapter
import ph.edu.rv_realm_quiz.adapters.FavBooksAdapter
import ph.edu.rv_realm_quiz.databinding.ActivityFavoritesBinding
import ph.edu.rv_realm_quiz.models.Books
import ph.edu.rv_realm_quiz.realm.BookRealm
import ph.edu.rv_realm_quiz.realm.RealmDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityFavBooks : AppCompatActivity(), FavBooksAdapter.FavBooksAdapterInterface {
    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: FavBooksAdapter
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

            AlertDialog.Builder(this@ActivityFavBooks)
                .setTitle("Unfavorite")
                .setMessage("Are you sure you want to unfavorite this?")
                .setPositiveButton("Unfavorite") { _, _ ->
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
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManger = LinearLayoutManager(this)
        binding.rvFavBooks.layoutManager = layoutManger

        booksList = arrayListOf()
        adapter = FavBooksAdapter(booksList, this, this)
        binding.rvFavBooks.adapter = adapter

        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvFavBooks)

        getBooks()

    }

    override fun unFavBook(bookId: String, position: Int) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("favBook"))
        scope.launch(Dispatchers.IO) {
            val book = booksList[position]
            database.unFavBook(book)
            withContext(Dispatchers.Main){
                booksList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.updateBookList(database.getFavoriteBooks().map {mapBooks(it)} as ArrayList<Books>)
                Snackbar.make(binding.root, "Book Unfavorited Successfully", Snackbar.LENGTH_LONG).show()
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
            val books = database.getFavoriteBooks()
            val booksList = arrayListOf<Books>()
            booksList.addAll(
                books.map {
                    mapBooks(it)
                }
            )
            withContext(Dispatchers.Main) {
                adapter.updateBookList(booksList)
                adapter.notifyDataSetChanged()
                binding.empty.text = if (booksList.isEmpty()) "No Favorite Books Yet..." else ""
            }
        }
    }


}