package elfak.diplomski.jujitsuposedetection

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import elfak.diplomski.jujitsuposedetection.preference.PreferenceUtils
import java.io.IOException

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    //private val OBJECT_DETECTION = "Object Detection"
    private val POSE_DETECTION = "Pose Detection"

    private  val  TAG: String = "LivePreviewActivity"

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private val selectedModel: String = POSE_DETECTION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate")


        if (!allRuntimePermissionsGranted()) {
            getRuntimePermissions()
        }

        preview = findViewById(R.id.preview_view)
        if (preview == null) {
            Log.d(TAG, "Preview is null")
        }
        graphicOverlay = findViewById(R.id.graphic_overlay)
        if (graphicOverlay == null) {
            Log.d(
                TAG,
                "graphicOverlay is null"
            )
        }

        val facingSwitch = findViewById<ToggleButton>(R.id.facing_switch)
        facingSwitch.setOnCheckedChangeListener(this)

        createCameraSource(selectedModel)
        startCameraSource()

    }

    private fun allRuntimePermissionsGranted(): Boolean {
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val permissionsToRequest = ArrayList<String>()
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission.let {
                if (!isPermissionGranted(this, it)) {
                    permissionsToRequest.add(permission)
                }
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUESTS
            )
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "Permission granted: $permission")
            return true
        }
        Log.i(TAG, "Permission NOT granted: $permission")
        return false
    }

    companion object {
        //private const val TAG = "EntryChoiceActivity"
        private const val PERMISSION_REQUESTS = 1

        private val REQUIRED_RUNTIME_PERMISSIONS =
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
    }

    override fun onCheckedChanged(p0: CompoundButton?, isChecked: Boolean) {
        Log.d(TAG, "Set facing")
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource!!.setFacing(CameraSource.CAMERA_FACING_FRONT)
            } else {
                cameraSource!!.setFacing(CameraSource.CAMERA_FACING_BACK)
            }
        }
        preview!!.stop()
        startCameraSource()
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(
                        TAG,
                        "resume: Preview is null"
                    )
                }
                if (graphicOverlay == null) {
                    Log.d(
                        TAG,
                        "resume: graphOverlay is null"
                    )
                }
                preview!!.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "Unable to start camera source.",
                    e
                )
                cameraSource!!.release()
                cameraSource = null
            }
        }
    }

    private fun createCameraSource(model: String) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = CameraSource(this, graphicOverlay)
        }
        try {
            when (model) {
                POSE_DETECTION -> {
                    val poseDetectorOptions: PoseDetectorOptionsBase =
                        PreferenceUtils.getPoseDetectorOptionsForLivePreview(this)
                    Log.i(
                        TAG,
                        "Using Pose Detector with options $poseDetectorOptions"
                    )
                    val shouldShowInFrameLikelihood: Boolean =
                        PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this)
                    val visualizeZ: Boolean = PreferenceUtils.shouldPoseDetectionVisualizeZ(this)
                    val rescaleZ: Boolean =
                        PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this)
                    val runClassification: Boolean =
                        PreferenceUtils.shouldPoseDetectionRunClassification(this)
                    cameraSource!!.setMachineLearningFrameProcessor(
                        PoseDetectorProcessor(
                            this,
                            poseDetectorOptions,
                            shouldShowInFrameLikelihood,
                            visualizeZ,
                            rescaleZ,
                            runClassification,  /* isStreamMode = */
                            true
                        )
                    )
                }
                else -> Log.e(
                    TAG,
                    "Unknown model: $model"
                )
            }
        } catch (e: RuntimeException) {
            Log.e(
                TAG,
                "Can not create image processor: $model", e
            )
            Toast.makeText(
                applicationContext,
                "Can not create image processor: " + e.message,
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        createCameraSource(selectedModel)
        startCameraSource()
    }

    /** Stops the camera.  */
    override fun onPause() {
        super.onPause()
        preview!!.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cameraSource != null) {
            cameraSource!!.release()
        }
    }
}