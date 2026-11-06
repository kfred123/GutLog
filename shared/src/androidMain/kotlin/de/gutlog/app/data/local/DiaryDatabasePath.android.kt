package de.gutlog.app.data.local

import android.content.Context

fun diaryDatabasePath(context: Context): String =
    context.applicationContext.getDatabasePath("diary.db").absolutePath
