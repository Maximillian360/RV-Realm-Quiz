package ph.edu.rv_realm_quiz.realm

import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.Date
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import org.mongodb.kbson.ObjectId

class BookRealm : RealmObject{
    @PrimaryKey
    val id: ObjectId = ObjectId()
    val author: String = ""
    val bookName: String = ""
    val dateBookPublished: Date? = null
    val dateBookAdded: Date? = null
    val dateBookModified: Date? = null
    val isFav: Boolean = false
    val isArchived: Boolean = false
}