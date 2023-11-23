package ph.edu.rv_realm_quiz.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import ph.edu.rv_realm_quiz.databinding.ContentBooksRvBinding
import ph.edu.rv_realm_quiz.models.Books
import java.security.acl.Owner


class BooksAdapter(
    private var booksList: ArrayList<Books>,
    private val context: Context,
    private val bookAdapterCallback: BooksAdapterInterface,
    private var fragmentManager: FragmentManager
) : RecyclerView.Adapter<BooksAdapter.BookViewHolder>(), ItemTouchHelperAdapter {

    interface BooksAdapterInterface {

        //        fun archiveOwner(ownerId: String, position: Int)
//        fun deleteOwnerAndTransferPets(ownerId: String, position: Int)
        fun refreshData()
    }

    inner class BookViewHolder(private val binding: ContentBooksRvBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemData: Books) {
            with(binding) {
                txtBookName.text = String.format("Book: %s", itemData.bookName)
                txtAuthor.text = String.format("Author: %s", itemData.author)
                txtAdded.text = String.format("Date Added: %s", itemData.dateBookAdded)
                txtPublished.text = String.format("Date Published: %s", itemData.dateBookPublished)
                txtModified.text = String.format("Date Modified: %s", itemData.dateBookModified)


                btnToFav.setOnClickListener {

                }

//                val ownedPetAdapter = OwnedPetAdapter(itemData.ownedPets)
//                rvOwnedPets.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//                rvOwnedPets.adapter = ownedPetAdapter
//
//                btnEditOwner.isEnabled = itemData.name != "Lotus"
//
//                btnEditOwner.setOnClickListener {
//                    val editOwnerDialog = EditOwner()
//                    editOwnerDialog.refreshDataCallback = object : EditPet.RefreshDataInterface{
//                        override fun refreshData() {
//                            ownerAdapterCallback.refreshData()
//                        }
//                    }
//                    editOwnerDialog.bindOwnerData(itemData)
//                    editOwnerDialog.show(fragmentManager, null)
//                }
            }
        }
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

    fun updateList(booksList: ArrayList<Books>) {
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
            val ownerId = booksList[position].id

            //ownerAdapterCallback.archiveOwner(ownerId, position)
        } else {
            Log.d("OwnerAdapter", "Error: Position out of bounds")
        }
    }


}