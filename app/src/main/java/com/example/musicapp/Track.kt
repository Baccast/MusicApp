import android.os.Parcel
import android.os.Parcelable

data class Track(
    val imageURL: String,
    val artistName: String,
    val songName: String,
    val audioFileResId: Int  // Add this new property to store audio resource ID
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt()  // Retrieve audio file resource ID from parcel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imageURL)
        parcel.writeString(artistName)
        parcel.writeString(songName)
        parcel.writeInt(audioFileResId)  // Write audio file resource ID to parcel
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Track> {
        override fun createFromParcel(parcel: Parcel): Track {
            return Track(parcel)
        }

        override fun newArray(size: Int): Array<Track?> {
            return arrayOfNulls(size)
        }
    }
}