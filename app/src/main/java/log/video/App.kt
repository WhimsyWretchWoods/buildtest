package log.video

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.SingletonImageLoader
import coil3.video.VideoFrameDecoder

class App : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}
