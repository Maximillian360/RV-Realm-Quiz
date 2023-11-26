package ph.edu.rv_realm_quiz.realm

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.find
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import ph.edu.rv_realm_quiz.adapters.BooksAdapter
import ph.edu.rv_realm_quiz.models.Books
import java.lang.IllegalStateException

class RealmDatabase {
    private val realm: Realm by lazy {
        val config =
            RealmConfiguration.Builder(setOf(BookRealm::class)).schemaVersion(2).initialData {
            }
                .build()
        Realm.open(config)
    }

    fun getAllBooks(): List<BookRealm> {
        return realm.query<BookRealm>("isArchived == false && isFav == false").find()
    }

    fun getArchivedBooks(): List<BookRealm> {
        return realm.query<BookRealm>("isArchived == true").find()
    }

    fun getFavoriteBooks(): List<BookRealm> {
        return realm.query<BookRealm>("isFav == true").find()
    }

    suspend fun addBook(
        bookName: String,
        bookAuthor: String,
        bookPages: Int,
        bookProgress: Int,
        datePublished: Long,
        dateAdded: Long,
        dateModified: Long
    ) {
        val dupeBookChecker: BookRealm? = realm.query<BookRealm>(
            "author == $0 && name == $1 && pages == $2 && dateBookPublished == $3",
            bookAuthor,
            bookName,
            bookPages,
            datePublished
        ).first().find()
        realm.write {
            if (dupeBookChecker == null) {
                val newBook = BookRealm().apply {
                    name = bookName
                    author = bookAuthor
                    pages = bookPages
                    progress = bookProgress
                    dateBookPublished = datePublished
                    dateBookAdded = dateAdded
                    dateBookModified = dateModified
                }

                val manageBook = copyToRealm(newBook)
            } else {
                throw IllegalStateException("Book duplicate!")
            }
        }
    }

    suspend fun updateBook(
        bookId: ObjectId,
        bookName: String,
        bookAuthor: String,
        bookPages: Int,
        bookProgress: Int,
        bookPublished: Long,
        dateModified: Long
    ) {
        val dupeBookChecker = realm.query<BookRealm>(
            "id == $0",
            bookId,
        ).first().find()
        realm.write {
            if (dupeBookChecker != null) {
                findLatest(dupeBookChecker)?.apply {
                    name = bookName
                    author = bookAuthor
                    pages = bookPages
                    progress = bookProgress
                    dateBookPublished = bookPublished
                    dateBookModified = dateModified
                }

            } else {
                throw IllegalStateException("Book does not exist!.")
            }
        }
    }

    suspend fun favBook(book: Books) {
        realm.write {
            val bookID = BsonObjectId(book.id)
            val bookRealm = query<BookRealm>("id == $0", bookID).first().find()
            if (bookRealm != null) {
                findLatest(bookRealm).apply {
                    this!!.isFav = true
                }
            } else {
                throw IllegalStateException("Book with ID $bookID not found. Cannot update.")
            }
        }
    }

    suspend fun unFavBook(book: Books) {
        realm.write {
            val bookID = BsonObjectId(book.id)
            val bookRealm = query<BookRealm>("id == $0", bookID).first().find()
            if (bookRealm != null) {
                findLatest(bookRealm).apply {
                    this!!.isFav = false
                }
            } else {
                throw IllegalStateException("Book with ID $bookID not found. Cannot update.")
            }
        }
    }

    suspend fun archiveBook(book: Books) {
        val archiveBookID = BsonObjectId(book.id)
        val bookRealm = realm.query<BookRealm>("id == $0", archiveBookID).first().find()
        realm.write {
            if (bookRealm != null) {
                findLatest(bookRealm).apply {
                    this!!.isArchived = true
                }
            } else {
                throw IllegalStateException("Book with ID $archiveBookID not found. Cannot update.")
            }
        }

    }

    suspend fun unArchiveBook(book: Books) {
        val archiveBookID = BsonObjectId(book.id)
        val bookRealm = realm.query<BookRealm>("id == $0", archiveBookID).first().find()
        realm.write {
            if (bookRealm != null) {
                findLatest(bookRealm).apply {
                    this!!.isArchived = false
                }
            } else {
                throw IllegalStateException("Book with ID $archiveBookID not found. Cannot update.")
            }
        }
    }

    suspend fun deleteBook(bookId: ObjectId) {
        //val deleteID = BsonObjectId(book.id)
        realm.write {
            query<BookRealm>("id == $0", bookId).first().find()?.let { delete(it) }
                ?: throw IllegalStateException("Book not found")
        }
    }

    suspend fun unarchiveAllBook() {
        val bookRealm = realm.query<BookRealm>("isArchived == true").find()
        realm.write {
            for (book in bookRealm) {
                findLatest(book)?.apply {
                    isArchived = false
                }
            }
        }
    }

    suspend fun deleteAllBook() {
        realm.write {
            delete(query<BookRealm>("isArchived == true").find())
        }
    }
}