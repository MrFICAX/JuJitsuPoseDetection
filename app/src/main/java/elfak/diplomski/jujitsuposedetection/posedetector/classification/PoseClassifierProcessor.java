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

package elfak.diplomski.jujitsuposedetection.posedetector.classification;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.google.common.base.Preconditions;
import com.google.mlkit.vision.pose.Pose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import elfak.diplomski.jujitsuposedetection.viewmodels.resultsViewModel;

/**
 * Accepts a stream of {@link Pose} for classification and Rep counting.
 */
public class PoseClassifierProcessor {
    private static final String TAG = "PoseClassifierProcessor";
    //    private static final String POSE_SAMPLES_FILE = "pose/karate data out.csv"; //"pose/fitness_pose_samples.csv";
    private static final String POSE_SAMPLES_FILE = "pose/karate_data_out.csv"; //"pose/fitness_pose_samples.csv";

    private static final String BORBENI_GARD_ISPRAVNO = "Borbeni gard";
    private static final String BORBENI_GARD_NISKO = "Borbeni gard nisko";
    private static final String BORBENI_GARD_VISOKO = "Borbeni gard visoko";
    private static final String MAE_GERI_BEZ_RUKU = "Mae geri bez ruku";
    private static final String MAE_GERI_ISPRAVNO = "Mae geri";
    private static final String MAE_GERI_NISKO = "Mae geri nisko";
    private static final String MAE_GERI_VISOKO = "Mae geri visoko";
    private static final String DJAKO_ZUKI_NISKO_BLOK = "Djako zuki nisko blok";
    private static final String DJAKO_ZUKI_NEISPRAVNO = "loš Djako zuki";
    private static final String DJAKO_ZUKI_USPRAVNO = "Djako zuki uspravno";
    private static final String DJAKO_ZUKI_USPRAVNO_BLOK = "Djako zuki uspravno blok";
    private static final String DJAKO_ZUKI_NISKO = "Djako zuki nisko";

    private static final String[] POSE_CLASSES = {
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
    };

    private final boolean isStreamMode;
    private resultsViewModel resultsViewModel;
    private EMASmoothing emaSmoothing;
    private List<RepetitionCounter> repCounters;
    private List<PoseChecker> poseCheckers;
    private PoseClassifier poseClassifier;
    private String lastRepResult;

    @WorkerThread
    public PoseClassifierProcessor(Context context, boolean isStreamMode, resultsViewModel resultsViewModel) {
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
        this.isStreamMode = isStreamMode;
        this.resultsViewModel = resultsViewModel;
        if (isStreamMode) {
            emaSmoothing = new EMASmoothing();
            repCounters = new ArrayList<>();
            poseCheckers = new ArrayList<>();
            lastRepResult = "";
        }
        loadPoseSamples(context);
    }

    private void loadPoseSamples(Context context) {
        List<PoseSample> poseSamples = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(POSE_SAMPLES_FILE)));
            String csvLine = reader.readLine();
            while (csvLine != null) {
                // If line is not a valid {@link PoseSample}, we'll get null and skip adding to the list.
                PoseSample poseSample = PoseSample.getPoseSample(csvLine, ",");
                if (poseSample != null) {
                    poseSamples.add(poseSample);
                }
                csvLine = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error when loading pose samples.\n" + e);
        }
        poseClassifier = new PoseClassifier(poseSamples);
        if (isStreamMode) {
            for (String className : POSE_CLASSES) {
                repCounters.add(new RepetitionCounter(context, className));
                poseCheckers.add(new PoseChecker(context, className));
                this.resultsViewModel.addClassName(className);
            }
        }
    }

    /**
     * Given a new {@link Pose} input, returns a list of formatted {@link String}s with Pose
     * classification results.
     *
     * <p>Currently it returns up to 2 strings as following:
     * 0: PoseClass : X reps
     * 1: PoseClass : [0.0-1.0] confidence
     */
    @WorkerThread
    public List<String> getPoseResult(Pose pose) {
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
        List<String> result = new ArrayList<>();
        ClassificationResult classification = poseClassifier.classify(pose);
        RepetitionCounter maxRepCounter = null;

        if (isStreamMode) {
            classification = emaSmoothing.getSmoothedResult(classification);

            if (pose.getAllPoseLandmarks().isEmpty()) {
                result.add(lastRepResult);
                return result;
            }

            for (PoseChecker poseChecker : poseCheckers) {
                poseChecker.checkPoseAndEmitAudio(classification);
            }

            for (RepetitionCounter repCounter : repCounters) {
                int repsBefore = repCounter.getNumRepeats();
                int repsAfter = repCounter.addClassificationResult(classification);
                if (repsAfter > repsBefore) {
                    this.resultsViewModel.addRepToClassName(repCounter.getClassName());
                    break;
                }
                if (repCounter.getClassName().equals(classification.getMaxConfidenceClass())) {
                    maxRepCounter = repCounter;
                }
            }
        }

        if (!pose.getAllPoseLandmarks().isEmpty()) {
            String maxConfidenceClass = classification.getMaxConfidenceClass();
            String maxConfidenceClassResult = String.format(
                    Locale.US,
                    "%s :\n %.2f sigurnost",
                    maxConfidenceClass,
                    classification.getClassConfidence(maxConfidenceClass)
                            / poseClassifier.confidenceRange());
            if ((classification.getClassConfidence(maxConfidenceClass) / poseClassifier.confidenceRange()) > 0.6) {

                if (maxRepCounter != null) {
                    String pon = "ponavljanja";
                    if(maxRepCounter.getNumRepeats() == 1){
                        pon = "ponavljanje";
                    }
                    lastRepResult = String.format(
                            Locale.US, "%s : %d %s", maxConfidenceClass, maxRepCounter.getNumRepeats(), pon);
                    result.add(lastRepResult);
                }

                result.add(maxConfidenceClassResult);
                String stepenIspravnosti = null;
                for (PoseChecker poseChecker : poseCheckers) {
                    if (poseChecker.getClassName().equals(maxConfidenceClass)) {
                        stepenIspravnosti = poseChecker.stepenIspravnosti();
                    }
                }
                if (stepenIspravnosti != null) {
                    result.add("Stepen ispravnosti: "+stepenIspravnosti);
                }
            }
        }

        return result;
    }

}
