package elfak.diplomski.jujitsuposedetection

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import elfak.diplomski.jujitsuposedetection.viewmodels.resultsViewModel

class ResultActivity : AppCompatActivity() {

    private val resultsViewModel: resultsViewModel by viewModels()
    private lateinit var resultsLayout: LinearLayout
    private var repCounterResults: HashMap<String, Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        resultsLayout = findViewById(R.id.ResultsLayout)

        var button: Button = findViewById(R.id.buttonBegin)
        button.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }


        val intent = intent
        val repCounters = intent.getSerializableExtra("map") as HashMap<String, Int>?
        //Log.v("HashMapTest", repCounters?.keys.toString())
        //Toast.makeText(this, repCounters?.size.toString(), Toast.LENGTH_SHORT).show()
        repCounterResults = repCounters

        addResults();
    }

    private fun addResults() {

        repCounterResults?.forEach { (key, value) ->

            val viewItem: View = layoutInflater.inflate(R.layout.result_item, resultsLayout, false)
            val PoseClassName: TextView = viewItem.findViewById(R.id.PoseClassName)
            val Result: TextView = viewItem.findViewById(R.id.result)
            PoseClassName.setText(key)
            Result.setText(value.toString())
            resultsLayout.addView(viewItem)

        }
    }

}