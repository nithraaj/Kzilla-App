package org.kzilla.srmkzilla

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.*

object Utils {
    @JvmStatic
    fun generateQrCode(inputValue: String): Bitmap {
        try {
            val hintMap = Hashtable<EncodeHintType, Any>()
            hintMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H // H = 30% damage
            hintMap[EncodeHintType.MARGIN] = 0
            val qrCodeWriter = QRCodeWriter()

            val size = 256

            val bitMatrix = qrCodeWriter.encode(inputValue, BarcodeFormat.QR_CODE, size, size, hintMap)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            return BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.kzilla_logo)
        }

    }

    @JvmStatic
    fun getShareLink(event_id: String): String {
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://www.srmkzilla.net/events?event_id=$event_id"))
            .setDomainUriPrefix("https://srmkzilla.page.link")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("org.kzilla.srmkzilla")
                    .build()
            )
            .buildDynamicLink()
        return dynamicLink.uri.toString()
    }
}