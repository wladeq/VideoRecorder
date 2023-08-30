package pl.vi.videorecorder

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.googleapis.media.MediaHttpUploader.UploadState
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpResponseException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.IOUtils
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.vi.videorecorder.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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

//            if (ContextCompat.checkSelfPermission(
//                    this@MainActivity,
//                    android.Manifest.permission.CAMERA
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this@MainActivity,
//                    arrayOf(android.Manifest.permission.CAMERA),
//                    VIDEO_PERMISSION_REQUEST
//                )
//            } else {
            val cameraIntent = Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(cameraIntent)
            //  }

        }


        binding.getVideoBtn.setOnClickListener {

            val intent = Intent()
            intent.setType("video/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(
                Intent.createChooser(intent, "Select Video"),
                VIDEO_PICK_RESULT
            )
//            val intent = Intent()
//            intent.setType("video/*")
//            intent.setAction(Intent.ACTION_GET_CONTENT)
//            startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_PICK_RESULT)
//            if (ContextCompat.checkSelfPermission(
//                    this@MainActivity,
//                    android.Manifest.permission.READ_EXTERNAL_STORAGE
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this@MainActivity,
//                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
//                    STORAGE_PERMISSION_REQUEST
//                )
//            } else {
//                val intent = Intent()
//                intent.setType("video/*")
//                intent.setAction(Intent.ACTION_GET_CONTENT)
//                startActivityForResult(Intent.createChooser(intent,"Select Video"), VIDEO_PICK_RESULT)
//
////                val path = getLastVideoFromGallery(this)
////
////                print("THE PATH IS ${path}")
//            }
            }

        binding.uploadToDrive.setOnClickListener {
            signIn()
        }

        binding.shareBtn.setOnClickListener {
            shareVideo(selectedUri)
        }

    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Scopes.DRIVE_FILE))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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
                    selectedFile = makeCopy(file)
                    displayVideo(file)
//                    val videoFile = fileCopy
//                    val fileMetadata = File()
//                    fileMetadata.name = "video.mp4"
//
//                    val mediaContent = FileContent("video/*", videoFile)
//
//                    val uploadedFile = mDrive.files().create(fileMetadata, mediaContent)
//                        .setFields("id")
//                        .execute()

                    //val fileManagerString = uri?.path
                    // TODO Send video to google drive
                    //val path = getRealPathFromURI(this, uri)
                    //binding.path.text = fileManagerString

                }
            }

            STORAGE_PERMISSION_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    val intent = Intent()
                    intent.setType("video/*")
                    intent.setAction(Intent.ACTION_GET_CONTENT)
                    startActivityForResult(
                        Intent.createChooser(intent, "Select Video"),
                        VIDEO_PICK_RESULT
                    )
//                    val path = getLastVideoFromGallery(this)
//
//                    print("THE PATH IS ${path}")
                }
            }

            RC_SIGN_IN -> {
                if (resultCode == RESULT_OK) {
                    mDrive = getDriveService(this)
                    uploadFileToGDrive(this, selectedFile)

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
//                    val fileName = "Ticket"
                    val raunit = selectedFile
                    val gfile = com.google.api.services.drive.model.File()
                    gfile.name = "First Video1"
                    val mimetype = "video/*"
                    val fileContent = FileContent(mimetype, raunit)
                    var fileid = ""
                    var file: com.google.api.services.drive.model.File? = null

                    withContext(Dispatchers.Main) {

                        withContext(Dispatchers.IO) {


                            launch {
                                var request =
                                    googleDriveService.files().create(gfile, fileContent)
                                        .setFields("id")
                                request.mediaHttpUploader.progressListener =
                                    FileUploadProgressListener()
                                try {
                                    file = request.execute()

                                    updateLink(file)
                                } catch (e: HttpResponseException) {
                                    if (e.statusCode == 308) {

                                        // Resumable upload has started
                                        updateLink(file)
                                    } else {
                                        e.printStackTrace()

                                    }
                                } catch (e: IOException) {
                                    e.printStackTrace()

                                }
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
                        "Some Error Occured in Uploading Files" + e.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }
    private fun displayVideo(uri: Uri) {
        binding.videoView.visibility = View.VISIBLE
        val videoContainer = binding.videoView
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoContainer)
        videoContainer.setMediaController(mediaController)
        videoContainer.setVideoURI(uri)
        videoContainer.requestFocus()
        videoContainer.start()
    }
    fun updateLink(link: com.google.api.services.drive.model.File?) {
        print(link?.webContentLink)
//        lifecycleScope.launch {
//            withContext(Dispatchers.Main) {
//                binding.path.setText(link)
//            }
//        }
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null)
            val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            cursor?.getString(column_index ?: 0)
        } finally {
            cursor?.close()
        }
    }

    private fun shareVideo(uri: Uri?) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "video/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Share Video"))
    }


    private fun makeCopy(fileUri: Uri): File {
        val parcelFileDescriptor =
            applicationContext.contentResolver.openFileDescriptor(fileUri, "r", null)
        val inputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
        val file = File(
            applicationContext.filesDir,
            getFileName(applicationContext.contentResolver, fileUri)
        )
        val outputStream = FileOutputStream(file)
        IOUtils.copy(inputStream, outputStream)

        return file
    }

    private fun getFileName(contentResolver: ContentResolver, fileUri: Uri): String {

        var name = ""
        val returnCursor = contentResolver.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
    }

    fun getDriveService(context: Context): Drive {
        GoogleSignIn.getLastSignedInAccount(context).let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(
                this, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = googleAccount!!.account!!
            return Drive.Builder(
                com.google.api.client.http.javanet.NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(getString(R.string.app_name))
                .build()
        }
        var tempDrive: Drive
        return tempDrive
    }

    fun getLastVideoFromGallery(context: Context): String? {
        val projection = arrayOf(
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_MODIFIED
        )

        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
        )

        var videoPath: String? = null

        cursor?.use {
            if (it.moveToFirst()) {
                val pathIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                videoPath = it.getString(pathIndex)
            }
        }

        cursor?.close()

        return videoPath
    }

    companion object {
        const val VIDEO_PERMISSION_REQUEST = 1002
        const val STORAGE_PERMISSION_REQUEST = 1001
        const val VIDEO_PICK_RESULT = 1003
        const val RC_SIGN_IN = 1004

    }

    class FileUploadProgressListener : MediaHttpUploaderProgressListener {
        @Throws(IOException::class)
        override fun progressChanged(uploader: MediaHttpUploader) {
            when (uploader.uploadState) {
                UploadState.INITIATION_STARTED -> {

                    print("ZONK INITIATION_STARTED")
                }
                UploadState.INITIATION_COMPLETE -> print("ZONK INITIATION_COMPLETE")
                UploadState.MEDIA_IN_PROGRESS ->           // postToDialog("Upload in progress");
                    print("ZONK Upload percentage: " + uploader.progress)

                UploadState.MEDIA_COMPLETE -> print("ZONK MEDIA_COMPLETE")
                UploadState.NOT_STARTED -> print("ZONK NOT_STARTED")
            }
        }


    }

}