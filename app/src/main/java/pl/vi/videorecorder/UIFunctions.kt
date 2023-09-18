package pl.vi.videorecorder

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import pl.vi.videorecorder.databinding.ActivityMainBinding

object UIFunctions {
    fun showQRCodeDialog(context: Context, link: String) {
        val qrCodeBitmap = FileFunctions.generateQRCode(link)
        val imageView = ImageView(context)
        imageView.setImageBitmap(qrCodeBitmap)

        AlertDialog.Builder(context)
            .setView(imageView)
            .setTitle("Scan this QR Code")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun displayVideo(uri: Uri, binding: ActivityMainBinding, context: Context) {
        binding.videoView.visibility = View.VISIBLE
        val videoContainer = binding.videoView
        val mediaController = MediaController(context)
        mediaController.setAnchorView(videoContainer)
        videoContainer.setMediaController(mediaController)
        videoContainer.setVideoURI(uri)
        videoContainer.requestFocus()
        videoContainer.start()
    }

    fun shareVideo(uri: Uri?, fn: (Intent, String) -> Unit) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "video/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        fn(shareIntent, "Share Video")
    }
}