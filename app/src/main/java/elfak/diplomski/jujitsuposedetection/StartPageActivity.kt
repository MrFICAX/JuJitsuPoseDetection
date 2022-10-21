package elfak.diplomski.jujitsuposedetection

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class StartPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_page)

        var b: Button  = findViewById<Button>(R.id.buttonBegin)
        b.setOnClickListener {
            startActivity (Intent(this, MainActivity::class.java))
        }
       var nb: Button  = findViewById<Button>(R.id.buttonCheckPoses)
        nb.setOnClickListener {
            startActivity (Intent(this, CheckPosesActivity::class.java))
        }
    }

}