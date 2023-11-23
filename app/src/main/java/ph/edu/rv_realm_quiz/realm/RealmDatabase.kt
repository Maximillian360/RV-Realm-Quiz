package ph.edu.rv_realm_quiz.realm

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import ph.edu.rv_realm_quiz.realm.BookRealm
import ph.edu.rv_realm_quiz.models.Books

class RealmDatabase {
    private val realm: Realm by lazy {
        val config =
            RealmConfiguration.Builder(setOf(BookRealm::class)).schemaVersion(1).initialData {

            }
                .build()
        Realm.open(config)
    }

    fun getAllBooks(): List<BookRealm>{
        return realm.query<BookRealm>("isArchived == false && isFav == false").find()
    }

    fun getArchivedBooks(): List<BookRealm>{
        return realm.query<BookRealm>("isArchived == true").find()
    }

    fun getFavoriteBooks(): List<BookRealm>{
        return realm.query<BookRealm>("isFav == true").find()
    }

    suspend fun addBook() {

    }

}