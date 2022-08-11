/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.saggitt.omega.search

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.saggitt.omega.data.PeopleInfo

class PeopleItems(val context: Context) {

    @SuppressLint("Range")
    fun getPeopleInformation(): ArrayList<PeopleInfo> {
        val contacts = ArrayList<PeopleInfo>()
        val contentResolver = context.contentResolver
        val cursor: Cursor? = contentResolver?.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        if (cursor?.count ?: 0 > 0) {
            while (cursor?.moveToNext() == true) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhone =
                    cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                if (hasPhone > 0) {
                    val pCur = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    while (pCur?.moveToNext() == true) {
                        val phoneNo =
                            pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        contacts.add(PeopleInfo(id, name, phoneNo!!))
                    }
                    pCur?.close()
                }
            }
        }
        cursor?.close()
        return contacts
    }
}