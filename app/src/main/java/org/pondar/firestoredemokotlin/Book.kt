package org.pondar.firestoredemokotlin

import android.os.Parcelable
import com.google.firebase.firestore.Exclude

import kotlinx.android.parcel.Parcelize

//The exclude thing on book is because we want to have an id number in the book
//class, but do not want to save it to firebase as firebase will automatically
//generate the IDs (in this application, maybe in other applications you do it
//differently.
@Parcelize
data class Book(var author: String="", var title: String="", @get:Exclude var id: String = "") : Parcelable