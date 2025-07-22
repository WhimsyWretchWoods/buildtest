package test.raku

import android.content.Context
import android.net.Uri
import java.io.File

object MediaStoreHelper {
    fun getSampleVideoUri(context: Context): Uri? {
        val filePath = "/storage/emulated/0/Samples/BigBuckBunny.mp4"
        val file = File(filePath)
        
        return if (file.exists()) {
            Uri.fromFile(file)
        } else {
            null
        }
    }
}
