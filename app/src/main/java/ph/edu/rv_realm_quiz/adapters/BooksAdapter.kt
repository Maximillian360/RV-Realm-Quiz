package ph.edu.rv_realm_quiz.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import ph.edu.rv_realm_quiz.databinding.ContentBooksRvBinding
import ph.edu.rv_realm_quiz.dialogs.AddBookDialog
import ph.edu.rv_realm_quiz.dialogs.UpdateBookDialog
import ph.edu.rv_realm_quiz.models.Books
import ph.edu.rv_realm_quiz.realm.RealmDatabase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class BooksAdapter(
    private var booksList: ArrayList<Books>,
    private val context: Context,
    private val bookAdapterCallback: BooksAdapterInterface,
    private var fragmentManager: FragmentManager
) : RecyclerView.Adapter<BooksAdapter.BookViewHolder>(), ItemTouchHelperAdapter, UpdateBookDialog.RefreshDataInterface {

    private lateinit var book: Books
    private var database = RealmDatabase()
    //lateinit var refreshDataCallback: AddBookDialog.RefreshDataInterface

    interface BooksAdapterInterface {
        fun archiveBook(bookId: ObjectId, position: Int)

        //        fun archiveOwner(ownerId: String, position: Int)
//        fun deleteOwnerAndTransferPets(ownerId: String, position: Int)
        fun refreshData()
    }


    fun setBook(book: Books) {
        this.book = book
    }

    override fun refreshData() {

    }

    inner class BookViewHolder(private val binding: ContentBooksRvBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Books) {
            with(binding) {
                txtBookName.text = String.format("Book: %s", book.bookName)
                txtAuthor.text = String.format("Author: %s", book.author)
                txtPages.text = String.format("Pages: %s", book.pages)
                txtProgress.text = String.format("Progress: %s", book.progress)
                txtPublished.text =
                    String.format("Date Published: %s", formatDate(book.dateBookPublished))
                txtAdded.text = String.format("Date Added: %s", formatDate(book.dateBookAdded))
                txtModified.text =
                    String.format("Date Modified: %s", formatDate(book.dateBookModified))


                btnToFav.setOnClickListener {
                    val coroutineContext = Job() + Dispatchers.IO
                    val scope = CoroutineScope(coroutineContext + CoroutineName("favBook"))
                    scope.launch(Dispatchers.IO) {
                        database.favBook(book)
                        withContext(Dispatchers.Main) {
                            // You can update the UI or show a message if needed
                            Toast.makeText(context, "Book Moved to Favorites!", Toast.LENGTH_LONG).show()
                            // Refresh data if necessary
                            bookAdapterCallback.refreshData()
                        }
                    }
                }

                btnToUpdate.setOnClickListener {
                    val updateBookDialog = UpdateBookDialog()
                    updateBookDialog.refreshDataCallback = object : UpdateBookDialog.RefreshDataInterface {
                        override fun refreshData() {
                            bookAdapterCallback.refreshData()
                        }
                    }
                    updateBookDialog.bindBook(book)
                    updateBookDialog.show(fragmentManager, "UpdateBookDialog")
                }
            }
        }
    }

    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        return formatter.format(date)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding =
            ContentBooksRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val bookData = booksList[position]
        holder.bind(bookData)
        holder.itemView.tag = position
    }

    override fun getItemCount(): Int {
        return booksList.size
    }

    fun updateBookList(booksList: ArrayList<Books>) {
        this.booksList.clear()
        this.booksList.addAll(booksList)
        notifyDataSetChanged()
    }

    fun getBooksId(position: Int): String? {
        if (position in 0 until booksList.size) {
            return booksList[position].id
        }
        return null
    }

    override fun onItemDismiss(position: Int) {
        if (position in 0 until booksList.size) {
            val bookId = BsonObjectId(booksList[position].id)
            bookAdapterCallback.archiveBook(bookId, position)
            //ownerAdapterCallback.archiveOwner(ownerId, position)
        } else {
            //Log.d("OwnerAdapter", "Error: Position out of bounds")
        }
    }


}