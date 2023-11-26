package ph.edu.rv_realm_quiz.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import ph.edu.rv_realm_quiz.databinding.DialogAddBookBinding
import ph.edu.rv_realm_quiz.databinding.DialogUpdateBookBinding
import ph.edu.rv_realm_quiz.models.Books
import ph.edu.rv_realm_quiz.realm.RealmDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UpdateBookDialog : DialogFragment() {

    private lateinit var binding: DialogUpdateBookBinding
    private var database = RealmDatabase()
    private var isDateSelected = false
    //private var datePickerCallback: DatePickerCallback? = null
    lateinit var refreshDataCallback: RefreshDataInterface
    private var date: Date? = Calendar.getInstance().time
    private lateinit var book: Books
    interface DatePickerCallback {
        fun onDateSelected(selectedDate: Date)
    }

    interface RefreshDataInterface{
        fun refreshData()
    }



    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogUpdateBookBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    fun bindBook(book: Books){
        this.book = book
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            edtBookName.setText(book.bookName)
            edtAuthor.setText(book.author)
            numPages.setText(book.pages.toString())
            seekBarProgress.progress = book.progress
            tvProgress.text = "${seekBarProgress.progress}"
            seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    // Update the text of tvProgress when the SeekBar progress changes
                    tvProgress.text = "$progress"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            btnUpdateBook.setOnClickListener {
                if (edtBookName.text.isNullOrEmpty()) {
                    edtBookName.error = "Required"
                    return@setOnClickListener
                }
                if (edtAuthor.text.isNullOrEmpty()) {
                    edtAuthor.error = "Required"
                    return@setOnClickListener
                }
                if (numPages.text.isNullOrEmpty()) {
                    numPages.error = "Required"
                    return@setOnClickListener
                }

                val bookId = BsonObjectId(book.id)
                val bookName = edtBookName.text.toString()
                val bookAuthor = edtAuthor.text.toString()
                val bookPages = numPages.text.toString().toInt()
                val bookProgress = seekBarProgress.progress
                val currentDate = Calendar.getInstance().time.time

                if(bookId != null){
                    val coroutineContext = Job() + Dispatchers.IO
                    val scope = CoroutineScope(coroutineContext + CoroutineName("addBookToRealm"))
                    scope.launch(Dispatchers.IO) {
                        database.updateBook(bookId, bookName, bookAuthor, bookPages, bookProgress, currentDate)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, "Book has been updated!", Toast.LENGTH_LONG).show()
                            refreshDataCallback.refreshData()
                            dialog?.dismiss()
                        }
                    }
                }

                else{
                    Toast.makeText(activity, "Error! book does not exist!", Toast.LENGTH_LONG).show()
                }
                // ... rest of your logic
            }
        }
    }
}