package pl.vi.videorecorder

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import pl.vi.videorecorder.databinding.ActivityMainBinding

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
    }
}