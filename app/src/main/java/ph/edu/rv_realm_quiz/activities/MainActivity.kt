package ph.edu.rv_realm_quiz.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import ph.edu.rv_realm_quiz.R
import ph.edu.rv_realm_quiz.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBooks.setOnClickListener(this)
        binding.btnFavBook.setOnClickListener(this)
        binding.btnArchive.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.btn_books -> {
                val intent = Intent(this, ActivityBooks::class.java)
                startActivity(intent)
            }
            R.id.btn_fav_book -> {
                val intent = Intent(this, ActivityFavBooks::class.java)
                startActivity(intent)
            }
            R.id.btn_archive -> {
                val intent = Intent(this, ActivityArchivedBooks::class.java)
                startActivity(intent)
            }
        }
    }
}