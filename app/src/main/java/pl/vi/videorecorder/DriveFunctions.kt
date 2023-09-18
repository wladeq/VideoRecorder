package pl.vi.videorecorder

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes

object DriveFunctions {
    fun signIn(context: Context, fn: (Intent, Int) -> Unit) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Scopes.DRIVE_FILE))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        val signInIntent = googleSignInClient.signInIntent
        fn(signInIntent, MainActivity.RC_SIGN_IN)
    }

    fun getDriveService(context: Context): Drive {
        GoogleSignIn.getLastSignedInAccount(context).let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = googleAccount!!.account!!
            return Drive.Builder(
                com.google.api.client.http.javanet.NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(context.getString(R.string.app_name))
                .build()
        }
        var tempDrive: Drive
        return tempDrive
    }

    fun generateDriveLink(fileId: String): String {
        return "https://drive.google.com/file/d/$fileId/view?usp=drive_link"
    }

}