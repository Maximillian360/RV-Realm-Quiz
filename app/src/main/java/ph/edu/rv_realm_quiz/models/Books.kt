package ph.edu.rv_realm_quiz.models

import java.time.LocalDate
import java.util.Date

data class Books (
    val id: String,
    val author: String,
    val bookName: String,
    val dateBookPublished: Date,
    val dateBookAdded: Date,
    val dateBookModified: Date,
    val pages: Int,
    val progress: Int
//    val isFav: Boolean,
//    val isArchived: Boolean
)