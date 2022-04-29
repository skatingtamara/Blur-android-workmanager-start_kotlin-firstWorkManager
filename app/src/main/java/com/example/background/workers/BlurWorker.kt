package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import com.example.background.R

private const val TAG = "BlurWorker"
class BlurWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val appContext = applicationContext

        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        makeStatusNotification("Blurring image", appContext)

        return try {

            val sizeDownOption = BitmapFactory.Options()
            sizeDownOption.inSampleSize = 2

            val pictureFromResource = BitmapFactory.decodeResource(
                appContext.resources,
                R.drawable.android_cupcake, sizeDownOption)



            if (TextUtils.isEmpty(resourceUri)) {
               Log.e(TAG, "Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }



            val resolver = appContext.contentResolver

            // difference is in this part!!!!!
            val pictureFromUri = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri)))





            val outputFromResource = blurBitmap(
                pictureFromResource,
                appContext
            )

            val outputFromUri = blurBitmap(
                pictureFromUri!!,
                appContext
            )

            // Write bitmap to a temp file
            val outputUriFromResource = writeBitmapToFile(appContext, outputFromResource)
            val outputUriFromUri = writeBitmapToFile(appContext, outputFromUri)

           // makeStatusNotification("Output is $outputUri", appContext)

            val outputDataFromResource = workDataOf(KEY_IMAGE_URI to outputUriFromResource.toString())
            val outputDataFromUri = workDataOf(KEY_IMAGE_URI to outputUriFromUri.toString())

            Result.success(outputDataFromResource)
            Result.success(outputDataFromUri)

            Result.success()
        } catch (throwable: Throwable) {
            Log.e(TAG, "Error applying blur")
            Result.failure()
        }
    }
}