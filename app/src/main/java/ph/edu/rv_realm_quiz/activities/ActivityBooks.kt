package ph.edu.rv_realm_quiz.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ph.edu.rv_realm_quiz.adapters.BooksAdapter
import ph.edu.rv_realm_quiz.databinding.ActivityBooksBinding
import ph.edu.rv_realm_quiz.models.Books
import ph.edu.rv_realm_quiz.realm.BookRealm
import ph.edu.rv_realm_quiz.realm.RealmDatabase
import java.security.acl.Owner

class ActivityBooks : AppCompatActivity(), BooksAdapter.BooksAdapterInterface {
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
            // Handle swipe for other owners
            getBooks()


            // Handle swipe for Lotus owner
//            if (ownerId == "Lotus") {
//                // Notify adapter to refresh the view
//                adapter.notifyItemChanged(position)
//            } else {
//            }
        }
    }



    override fun refreshData(){
        getBooks()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)


        booksList = arrayListOf()
        adapter = BooksAdapter(booksList, this, this, supportFragmentManager )
        binding.rvBooks.adapter = adapter

        binding.fab.setOnClickListener{

        }

//        getOwners()

//        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
//        itemTouchHelper.attachToRecyclerView(binding.rvBooks)

//        val layoutManger = LinearLayoutManager(this)
//        binding.rvBooks.layoutManager = layoutManger

    }

    private fun mapBooks(books: BookRealm): Books {
        return Books(
            id = books.id.toHexString(),
            bookName = books.bookName,
            author = books.author,
            dateBookAdded = books.dateBookAdded,
            dateBookModified = books.dateBookModified,
            dateBookPublished = books.dateBookPublished

        )
    }

    fun getBooks() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllOwners"))

        scope.launch(Dispatchers.IO) {
            //val books = database.getNonLotusOwner()
            //val booksList = arrayListOf<Books>()
//            booksList.addAll(
//                books.map {
//                    mapBooks(it)
//                }
//            )
            withContext(Dispatchers.Main) {
            }
        }
    }

    override fun archiveBooks(ownerId: String, position: Int) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("archiveBook"))
        scope.launch(Dispatchers.IO) {

        }
    }

}