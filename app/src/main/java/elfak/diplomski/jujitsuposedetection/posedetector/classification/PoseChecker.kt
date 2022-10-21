/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package elfak.diplomski.jujitsuposedetection.posedetector.classification

import android.content.Context
import android.media.MediaPlayer
import elfak.diplomski.jujitsuposedetection.R


class PoseChecker @JvmOverloads constructor(
    private val context: Context,
    val className: String,
    private val enterThreshold: Float = DEFAULT_ENTER_THRESHOLD,
    private val exitThreshold: Float = DEFAULT_EXIT_THRESHOLD
) {
    private var lastPoseClassName: String? = null

    private var poseEntered = false

    fun checkPoseAndEmitAudio(classificationResult: ClassificationResult) {

        val poseConfidence = classificationResult.getClassConfidence(className)
        if (!poseEntered) {
            poseEntered = poseConfidence > enterThreshold
            if (poseEntered && lastPoseClassName != className) {
                mp = DetermineMediaFile()
                mp?.start();
            }
        }
        if (poseConfidence < exitThreshold) {
             poseEntered = false
        }

        if (classificationResult.getClassConfidence(classificationResult.maxConfidenceClass) > enterThreshold) {
            lastPoseClassName = classificationResult.maxConfidenceClass.toString()
        }
    }

    fun stepenIspravnosti(): String? {
        return stepenIspravnosti
    }

    companion object {
        // These thresholds can be tuned in conjunction with the Top K values in {@link PoseClassifier}.
        // The default Top K value is 10 so the range here is [0-10].
        private const val DEFAULT_ENTER_THRESHOLD = 8f
        private const val DEFAULT_EXIT_THRESHOLD = 6f
        private var stepenIspravnosti: String? = null
        private var mp: MediaPlayer? = null
    }

    private fun DetermineMediaFile(): MediaPlayer? {
        val BORBENI_GARD_ISPRAVNO = "Borbeni gard"
        val BORBENI_GARD_NISKO = "Borbeni gard nisko"
        val BORBENI_GARD_VISOKO = "Borbeni gard visoko"
        val MAE_GERI_BEZ_RUKU = "Mae geri bez ruku"
        val MAE_GERI_ISPRAVNO = "Mae geri"
        val MAE_GERI_NISKO = "Mae geri nisko"
        val MAE_GERI_VISOKO = "Mae geri visoko"

        val DJAKO_ZUKI_NISKO_BLOK = "Djako zuki nisko blok"
        val DJAKO_ZUKI_NEISPRAVNO = "los Djako zuki"
        val DJAKO_ZUKI_USPRAVNO = "Djako zuki uspravno"
        val DJAKO_ZUKI_USPRAVNO_BLOK = "Djako zuki uspravno blok"
        val DJAKO_ZUKI_NISKO = "Djako zuki nisko"

        val ISPRAVNO = "Ispravno"
        val DEL_NEISPRAVNO = "Del. neispravno"
        val NEISPRAVNO = "Neispravno"


        if (className.equals(BORBENI_GARD_ISPRAVNO)){
            stepenIspravnosti = ISPRAVNO
            return MediaPlayer.create(context, R.raw.odlican_gard)
        }
        if (className.equals(BORBENI_GARD_NISKO)){
            stepenIspravnosti = DEL_NEISPRAVNO
            return MediaPlayer.create(context, R.raw.podigni_ruke_gore)
        }
        if (className.equals(BORBENI_GARD_VISOKO)){
            stepenIspravnosti = DEL_NEISPRAVNO
            return MediaPlayer.create(context, R.raw.spusti_ruke_nize)
        }
        if (className.equals(MAE_GERI_BEZ_RUKU)){
            stepenIspravnosti = NEISPRAVNO
            return MediaPlayer.create(context, R.raw.podigni_ruke_gore)
        }
        if (className.equals(MAE_GERI_ISPRAVNO)){
            stepenIspravnosti = ISPRAVNO
            return MediaPlayer.create(context, R.raw.odlican_mae_geri)
        }
        if (className.equals(MAE_GERI_NISKO)){
            stepenIspravnosti = DEL_NEISPRAVNO
            return MediaPlayer.create(context, R.raw.podigni_nogu_vise)
        }
        if (className.equals(MAE_GERI_VISOKO)){
            stepenIspravnosti = DEL_NEISPRAVNO
            return MediaPlayer.create(context, R.raw.spusti_nogu_nize)
        }
        if (className.equals(DJAKO_ZUKI_NISKO))
        {
            stepenIspravnosti = DEL_NEISPRAVNO
            return MediaPlayer.create(context, R.raw.odlican_niski_djako_zuki)
        }
        if (className.equals(DJAKO_ZUKI_NISKO_BLOK))
        {
            stepenIspravnosti = ISPRAVNO
            return MediaPlayer.create(context, R.raw.odlican_niski_djako_zuki_sa_blokom)
        }
        if (className.equals(DJAKO_ZUKI_USPRAVNO))
        {
            stepenIspravnosti = DEL_NEISPRAVNO
            return MediaPlayer.create(context, R.raw.vrlo_dobar_djako_zuki)
        }
        if (className.equals(DJAKO_ZUKI_USPRAVNO_BLOK))
        {
            stepenIspravnosti = ISPRAVNO
            return MediaPlayer.create(context, R.raw.odlican_blok_sa_djako_zukijem)
        }
        if (className.equals(DJAKO_ZUKI_NEISPRAVNO))
        {
            stepenIspravnosti = NEISPRAVNO
            return MediaPlayer.create(context, R.raw.neispravan_djako_zuki)
        }

        return null
    }
}