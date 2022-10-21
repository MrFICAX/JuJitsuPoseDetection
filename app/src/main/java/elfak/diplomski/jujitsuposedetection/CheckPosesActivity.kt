package elfak.diplomski.jujitsuposedetection

import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import elfak.diplomski.jujitsuposedetection.posedetector.classification.PoseChecker

class CheckPosesActivity : AppCompatActivity() {

    private lateinit var resultsLayout: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_poses)

        resultsLayout = findViewById(R.id.ResultsLayout)

        var button: Button = findViewById(R.id.buttonBegin)
        button.setOnClickListener {
            startActivity(Intent(this, StartPageActivity::class.java))
        }

        addPhotos()
    }

    fun addPhotos() {

        val BORBENI_GARD_ISPRAVNO = "Borbeni gard"
        val BORBENI_GARD_NISKO = "Borbeni gard nisko"
        val BORBENI_GARD_VISOKO = "Borbeni gard visoko"
        val MAE_GERI_BEZ_RUKU = "Mae geri bez ruku"
        val MAE_GERI_ISPRAVNO = "Mae geri"
        val MAE_GERI_NISKO = "Mae geri nisko"
        val MAE_GERI_VISOKO = "Mae geri visoko"
        val DJAKO_ZUKI_NISKO_BLOK = "Djako zuki nisko blok"
        val DJAKO_ZUKI_NEISPRAVNO = "loš Djako zuki"
        val DJAKO_ZUKI_USPRAVNO = "Djako zuki uspravno"
        val DJAKO_ZUKI_USPRAVNO_BLOK = "Djako zuki uspravno blok"
        val DJAKO_ZUKI_NISKO = "Djako zuki nisko"

        val ISPRAVNO = "Ispravno"
        val DEL_NEISPRAVNO = "Delimično neispravno"
        val NEISPRAVNO = "Neispravno"


        val POSE_CLASSES = arrayOf(
            BORBENI_GARD_ISPRAVNO,
            BORBENI_GARD_NISKO,
            BORBENI_GARD_VISOKO,
            MAE_GERI_BEZ_RUKU,
            MAE_GERI_ISPRAVNO,
            MAE_GERI_NISKO,
            MAE_GERI_VISOKO,
            DJAKO_ZUKI_USPRAVNO,
            DJAKO_ZUKI_USPRAVNO_BLOK,
            DJAKO_ZUKI_NISKO,
            DJAKO_ZUKI_NISKO_BLOK,
            DJAKO_ZUKI_NEISPRAVNO
        )
        val photosList: ArrayList<Drawable> = ArrayList();
        photosList.add(resources.getDrawable(R.drawable.bg_ispravno));
        photosList.add(resources.getDrawable(R.drawable.bg_nisko));
        photosList.add(resources.getDrawable(R.drawable.bg_visoko));
        photosList.add(resources.getDrawable(R.drawable.mg_bezruku));
        photosList.add(resources.getDrawable(R.drawable.mg_ispravno));
        photosList.add(resources.getDrawable(R.drawable.mg_nisko));
        photosList.add(resources.getDrawable(R.drawable.mg_visoko));
        photosList.add(resources.getDrawable(R.drawable.djz_u));
        photosList.add(resources.getDrawable(R.drawable.djz_ub));
        photosList.add(resources.getDrawable(R.drawable.djz_n));
        photosList.add(resources.getDrawable(R.drawable.djz_nb));
        photosList.add(resources.getDrawable(R.drawable.djz_neispravno));

        var i: Int = 0
        for (photo in photosList) {
            val viewItem: View =
                layoutInflater.inflate(R.layout.possible_poses_item, resultsLayout, false)

            val textView: TextView = viewItem.findViewById(R.id.PoseClassName) as TextView
            textView.setText(POSE_CLASSES[i])

            val textViewCorrectness: TextView = viewItem.findViewById(R.id.Correctness) as TextView
            var stepenIspravnosti: String? = null
            var bojaTeksta: Int? = null;
            if (POSE_CLASSES[i] == BORBENI_GARD_ISPRAVNO){
                stepenIspravnosti = ISPRAVNO
            }
            if (POSE_CLASSES[i] == BORBENI_GARD_NISKO){
                stepenIspravnosti = DEL_NEISPRAVNO
            }
            if (POSE_CLASSES[i] == BORBENI_GARD_VISOKO){
                stepenIspravnosti = DEL_NEISPRAVNO
            }
            if (POSE_CLASSES[i] == MAE_GERI_BEZ_RUKU){
                stepenIspravnosti = NEISPRAVNO
            }
            if (POSE_CLASSES[i] == MAE_GERI_ISPRAVNO){
                stepenIspravnosti = ISPRAVNO
            }
            if (POSE_CLASSES[i] == MAE_GERI_NISKO){
                stepenIspravnosti = DEL_NEISPRAVNO
            }
            if (POSE_CLASSES[i] == MAE_GERI_VISOKO){
                stepenIspravnosti = DEL_NEISPRAVNO
            }
            if (POSE_CLASSES[i] == DJAKO_ZUKI_NISKO)
            {
                stepenIspravnosti = DEL_NEISPRAVNO
            }
            if (POSE_CLASSES[i] == DJAKO_ZUKI_NISKO_BLOK)
            {
                stepenIspravnosti = ISPRAVNO
            }
            if (POSE_CLASSES[i] == DJAKO_ZUKI_USPRAVNO)
            {
                stepenIspravnosti = DEL_NEISPRAVNO
            }
            if (POSE_CLASSES[i] == DJAKO_ZUKI_USPRAVNO_BLOK)
            {
                stepenIspravnosti = ISPRAVNO
            }
            if (POSE_CLASSES[i] == DJAKO_ZUKI_NEISPRAVNO)
            {
                stepenIspravnosti = NEISPRAVNO
            }

            if (stepenIspravnosti == ISPRAVNO){
                bojaTeksta = R.color.green
            } else if(stepenIspravnosti == DEL_NEISPRAVNO){
                bojaTeksta = R.color.yellow
            } else if(stepenIspravnosti == NEISPRAVNO){
                bojaTeksta = R.color.red
            }

            textViewCorrectness.setText(stepenIspravnosti)
            if (bojaTeksta != null) {
                textViewCorrectness.setBackgroundResource(bojaTeksta)
            }
            val imageView: ImageView = viewItem.findViewById(R.id.imageView) as ImageView
            imageView.setImageDrawable(photo)
            resultsLayout.addView(viewItem)
            i++
        }
    }
}