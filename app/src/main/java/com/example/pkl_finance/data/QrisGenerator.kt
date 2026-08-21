package com.example.pkl_finance.data

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import java.util.Locale

object QrisGenerator {

    /**
     * Calculates mathematical CRC-16 CCITT (0x1021 polynomial, 0xFFFF initial value)
     */
    private fun calculateCrc16(data: String): String {
        var crc = 0xFFFF
        val polynomial = 0x1021
        for (b in data.toByteArray()) {
            for (i in 0 until 8) {
                val bit = (b.toInt() shr (7 - i) and 1) == 1
                val c15 = (crc shr 15 and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) crc = crc xor polynomial
            }
        }
        crc = crc and 0xFFFF
        return String.format(Locale.US, "%04X", crc)
    }

    /**
     * Formats dynamic/static EMVCo QRIS compliant payload string
     */
    fun getQrisPayload(merchantName: String, amount: Double?): String {
        val cleanName = merchantName.take(25).uppercase().replace("[^A-Z0-9 ]".toRegex(), "")
        val formattedName = cleanName.ifEmpty { "MERCHANT" }

        val builder = StringBuilder()
        builder.append("000201") // Payload Format Indicator

        if (amount != null && amount > 0.0) {
            builder.append("010212") // Point of Initiation: Dynamic QR (12)
        } else {
            builder.append("010211") // Point of Initiation: Static QR (11)
        }

        // Merchant Account Info - Domestic QRIS ID
        val qrisMerchantVal = "0019ID.CO.QRIS.WWW0118936009153022205562"
        builder.append("26")
        builder.append(String.format(Locale.US, "%02d", qrisMerchantVal.length))
        builder.append(qrisMerchantVal)

        builder.append("52045812") // Merchant Category Code (General)
        builder.append("5303360")  // Currency Code (IDR - 360)

        // Transaction Amount (Only if dynamic)
        if (amount != null && amount > 0.0) {
            val amountStr = amount.toLong().toString()
            builder.append("54")
            builder.append(String.format(Locale.US, "%02d", amountStr.length))
            builder.append(amountStr)
        }

        builder.append("5802ID") // Country Code (ID)

        // Merchant Name
        builder.append("59")
        builder.append(String.format(Locale.US, "%02d", formattedName.length))
        builder.append(formattedName)

        builder.append("6006SRAGEN") // Merchant City
        builder.append("62070703A01") // Data Field (Invoice / Ref info)

        builder.append("6304") // CRC tag placeholder

        val baseString = builder.toString()
        val crcValue = calculateCrc16(baseString)

        return "$baseString$crcValue"
    }

    /**
     * Generates a square QR Code bitmap from the payload content
     */
    fun generateQrCodeBitmap(content: String, size: Int): Bitmap {
        val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
