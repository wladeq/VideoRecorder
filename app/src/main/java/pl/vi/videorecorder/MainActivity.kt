package pl.vi.videorecorder

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.googleapis.media.MediaHttpUploader.UploadState
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpResponseException
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.vi.videorecorder.DriveFunctions.getDriveService
import pl.vi.videorecorder.DriveFunctions.signIn
import pl.vi.videorecorder.FileFunctions.makeCopy
import pl.vi.videorecorder.FileFunctions.updateLink
import pl.vi.videorecorder.UIFunctions.displayVideo
import pl.vi.videorecorder.UIFunctions.shareVideo
import pl.vi.videorecorder.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var mDrive: Drive
    private var selectedFile: File? = null
    private var selectedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recordBtn.setOnClickListener {
            val cameraIntent = Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(cameraIntent)
        }

        binding.getVideoBtn.setOnClickListener {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Video"),
                VIDEO_PICK_RESULT
            )
        }

        binding.shareBtn.setOnClickListener {
            shareVideo(selectedUri) { intent, title ->
                startActivity(Intent.createChooser(intent, title))
            }
        }

        binding.uploadToDrive.setOnClickListener {
            signIn(applicationContext) { intent, requestInt ->
                startActivityForResult(intent, requestInt)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            VIDEO_PERMISSION_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    val cameraIntent = Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(cameraIntent)
                }
            }

            VIDEO_PICK_RESULT -> {
                if (resultCode == RESULT_OK) {
                    val file = data?.data ?: Uri.EMPTY
                    selectedUri = file
                    selectedFile = makeCopy(file, applicationContext)
                    displayVideo(file, binding, applicationContext)

                }
            }

            STORAGE_PERMISSION_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    val intent = Intent()
                    intent.type = "video/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                        Intent.createChooser(intent, "Select Video"),
                        VIDEO_PICK_RESULT
                    )
                }
            }

            RC_SIGN_IN -> {
                if (resultCode == RESULT_OK) {
                    mDrive = getDriveService(this)
                    uploadFileToGDrive(this, selectedFile)

                } else {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        // Signed in successfully
                    } catch (e: ApiException) {
                        Log.w("SignInError", "signInResult:failed code=" + e.statusCode)
                    }
                }
            }

            else -> {

            }
        }
    }

    fun uploadFileToGDrive(context: Context, selectedFile: File?) {
        mDrive.let { googleDriveService ->
            lifecycleScope.launch {
                try {
                    val raunit = selectedFile
                    val gfile = com.google.api.services.drive.model.File()
                    gfile.name = "First Video1"
                    val mimetype = "video/mp4"
                    val fileContent = FileContent(mimetype, raunit)
                    var file: com.google.api.services.drive.model.File? = null

                    withContext(Dispatchers.IO) {
                        launch {
                            var request =
                                googleDriveService.files().create(gfile, fileContent)
                                    .setFields("id")

                            request.mediaHttpUploader.chunkSize =
                                MediaHttpUploader.MINIMUM_CHUNK_SIZE
                            request.mediaHttpUploader.progressListener = (
                                    MediaHttpUploaderProgressListener { uploader ->
                                        try {
                                            when (uploader.uploadState) {
                                                UploadState.INITIATION_STARTED -> {
                                                    lifecycleScope.launch {
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(
                                                                context,
                                                                "Upload started",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            Log.d(
                                                                "UploadProgress",
                                                                "Upload started"
                                                            )
                                                        }
                                                    }

                                                }

                                                UploadState.INITIATION_COMPLETE -> Log.d(
                                                    "UploadProgress",
                                                    "Initiation complete"
                                                )

                                                UploadState.MEDIA_IN_PROGRESS -> {
                                                    lifecycleScope.launch {
                                                        withContext(Dispatchers.Main) {
                                                            val progressPercentage =
                                                                (uploader.progress * 100).toInt()
                                                            binding.path.setText("Upload percentage: $progressPercentage%")
                                                            Log.d(
                                                                "UploadProgress",
                                                                "Upload percentage: $progressPercentage%"
                                                            )
                                                        }
                                                    }
                                                }

                                                UploadState.MEDIA_COMPLETE -> {
                                                    lifecycleScope.launch {
                                                        withContext(Dispatchers.Main) {
                                                            binding.path.setText("Upload complete")
                                                            Log.d(
                                                                "UploadProgress",
                                                                "Upload completed"
                                                            )
                                                            withContext(Dispatchers.IO) {
                                                                delay(5000)
                                                                if (!file?.id.isNullOrBlank()) {
                                                                    updateLink(
                                                                        file,
                                                                        context,
                                                                        binding
                                                                    )
                                                                    Log.w(
                                                                        "UploadProgress",
                                                                        "id is ${file?.id}"
                                                                    )
                                                                    //fetchWebContentLink(file?.id ?: "")
                                                                } else {
                                                                    Log.w(
                                                                        "UploadProgress",
                                                                        "file?.id is null"
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                UploadState.NOT_STARTED -> {
                                                    Log.d("UploadProgress", "Upload not started")
                                                }

                                                else -> {
                                                    Log.w(
                                                        "UploadProgress",
                                                        "Unknown state: ${uploader.uploadState}"
                                                    )
                                                }

                                            }
                                        } catch (ex: Exception) {
                                            print("pizdec")
                                        }
                                    }
                                    )
                            try {
                                file = request.execute()
                            } catch (e: HttpResponseException) {
                                if (e.statusCode == 308) {

                                    // Resumable upload has started

                                } else {
                                    e.printStackTrace()

                                }
                            } catch (e: IOException) {
                                e.printStackTrace()

                            }
                        }
                    }

                } catch (userAuthEx: UserRecoverableAuthIOException) {
                    startActivity(
                        userAuthEx.intent
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("asdf", e.toString())
                    Toast.makeText(
                        context,
                        "Some Error Occured in Uploading Files$e",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    companion object {
        const val VIDEO_PERMISSION_REQUEST = 1002
        const val STORAGE_PERMISSION_REQUEST = 1001
        const val VIDEO_PICK_RESULT = 1003
        const val RC_SIGN_IN = 1004

    }
}