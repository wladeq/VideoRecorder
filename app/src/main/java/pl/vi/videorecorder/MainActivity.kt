package pl.vi.videorecorder

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import pl.vi.videorecorder.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


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
            startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_PICK_RESULT)
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

        binding.share.setOnClickListener {
            signIn()
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
                    val uri = data?.data ?: Uri.EMPTY
                    val fileManagerString = uri?.path
                    // TODO Send video to google drive

                    val path = getRealPathFromURI(this, uri)

                    binding.path.text = fileManagerString

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
                    val intent = Intent()
                    intent.setType("video/*")
                    intent.setAction(Intent.ACTION_GET_CONTENT)
                    startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_PICK_RESULT)
                }
//                val driveService = Drive.Builder(
//                    AndroidHttp.newCompatibleTransport(),
//                    GsonFactory(),
//                    GoogleSignIn.getLastSignedInAccount(this)
//                )
//                val videoFile = File("/path/to/your/video.mp4")
//                val fileMetadata = File()
//                fileMetadata.name = "video.mp4"
//
//                val mediaContent = FileContent("video/*", videoFile)
//
//                val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
//                    .setFields("id")
//                    .execute()

            }

            else -> {

            }
        }
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

//    fun getDriveService(context: Context): Drive {
//        GoogleSignIn.getLastSignedInAccount(context).let { googleAccount ->
//            val credential = GoogleAccountCredential.usingOAuth2(
//                this, listOf(DriveScopes.DRIVE_FILE)
//            )
//            credential.selectedAccount = googleAccount!!.account!!
//            return Drive.Builder(
//                AndroidHttp.newCompatibleTransport(),
//                JacksonFactory.getDefaultInstance(),
//                credential
//            )
//                .setApplicationName(getString(R.string.app_name))
//                .build()
//        }
//        var tempDrive: Drive
//        return tempDrive
//    }

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
}