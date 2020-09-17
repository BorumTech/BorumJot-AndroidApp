package com.boruminc.borumjot.android;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class TasksProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.boruminc.borumjot.android.TasksProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/tasks";
    static final Uri CONTENT_URI = Uri.parse(URL);

    /* Database Specific constant declarations */
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "Jottings";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            "CREATE TABLE `tasks` (" +
            "  `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            "  `title` TEXT NOT NULL DEFAULT 'Untitled Task', " +
            "  `status` INTEGER NOT NULL DEFAULT '0', " +
            "  `time_created` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP" +
            ");";
    static final String TASKS_TABLE_NAME = "tasks";

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        // Create a write able database which will trigger its creation if it doesn't already exist.
        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    /**
     *
     * @param uri
     * @param values
     * @return The
     * @throws SQLException
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Add a new student record
        long rowID = db.insert(TASKS_TABLE_NAME, "", values);

        // If record is added successfully
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
