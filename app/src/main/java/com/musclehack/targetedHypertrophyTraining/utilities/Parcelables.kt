package com.musclehack.targetedHypertrophyTraining.utilities

import android.os.Parcel
import android.os.Parcelable

class Parcelables {
    companion object {
        fun toByteArray(p: Parcelable): ByteArray {
            val parcel = Parcel.obtain()
            p.writeToParcel(parcel, 0)
            val result = parcel.marshall()
            parcel.recycle()
            return result
        }

        fun toParcelable(bytes: ByteArray, creator: Parcelable.Creator<*>): Any {
            val parcel = Parcel.obtain()
            parcel.unmarshall(bytes, 0, bytes.count())
            parcel.setDataPosition(0)

            val result = creator.createFromParcel(parcel)
            parcel.recycle()
            return result
        }

    }
}