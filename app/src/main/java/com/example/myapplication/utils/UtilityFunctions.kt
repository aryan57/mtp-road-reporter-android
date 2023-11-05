package com.example.myapplication.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class UtilityFunctions {
    companion object {
        fun unixTimeToCustomDateString(unixTime: Long): String {
            val locale = Locale.getDefault()
            val instant = Instant.ofEpochSecond(unixTime)
            val zoneId =
                ZoneId.systemDefault() // Use the device's default time zone or specify a specific one

            val formatter =
                DateTimeFormatter.ofPattern("d MMM yyyy, hh:mm a", locale).withZone(zoneId)
            return formatter.format(instant)
        }

        fun getImageUrlFromImgID(imgID: String): String {
            return Constants.SERVER_BASE_URL + Constants.API_VERSION + Constants.API_PATH_GET_IMAGE + "/" + imgID
        }

        fun getGoogleMapsLinkFromLatLong(lat: Double, long: Double): String {
            // https://maps.google.com/?q=22.3218684,87.3086746
            return "https://maps.google.com/?q=$lat,$long"
        }


    }
}