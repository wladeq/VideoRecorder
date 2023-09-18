package pl.vi.videorecorder

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.google.api.client.util.IOUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.vi.videorecorder.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object FileFunctions {
    fun getFileName(contentResolver: ContentResolver, fileUri: Uri): String {

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

    @Throws(WriterException::class)
    fun generateQRCode(text: String): Bitmap? {
        val width = 500  // width of the QR code
        val height = 500 // height of the QR code
        val bitMatrix: BitMatrix

        try {
            bitMatrix = MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                width, height, null
            )
        } catch (Illegalargumentexception: IllegalArgumentException) {
            return null
        }

        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix[x, y]) -0x1000000 else -0x1
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun makeCopy(fileUri: Uri, context: Context): File {
        val parcelFileDescriptor =
            context.contentResolver.openFileDescriptor(fileUri, "r", null)
        val inputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
        val file = File(
            context.filesDir,
            getFileName(context.contentResolver, fileUri)
        )
        val outputStream = FileOutputStream(file)
        IOUtils.copy(inputStream, outputStream)

        return file
    }

    suspend fun updateLink(
        link: com.google.api.services.drive.model.File?,
        context: Context,
        binding: ActivityMainBinding
    ) {
        withContext(Dispatchers.Main) {
            val link = DriveFunctions.generateDriveLink(link?.id ?: "")
            Log.d(
                "UploadProgress",
                "link - ${link}"
            )
            UIFunctions.showQRCodeDialog(context, link, binding)
            binding.path.setText(link)
        }
    }

}