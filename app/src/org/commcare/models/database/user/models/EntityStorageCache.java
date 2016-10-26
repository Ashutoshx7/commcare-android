package org.commcare.models.database.user.models;

import android.content.ContentValues;
import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.commcare.CommCareApplication;
import org.commcare.models.database.SqlStorage;
import org.commcare.models.database.app.DatabaseAppOpenHelper;
import org.commcare.modern.database.DatabaseHelper;

/**
 * @author ctsims
 */
public class EntityStorageCache {
    private static final String TAG = EntityStorageCache.class.getSimpleName();
    private static final String TABLE_NAME = "entity_cache";

    private static final String COL_CACHE_NAME = "cache_name";
    private static final String COL_ENTITY_KEY = "entity_key";
    private static final String COL_CACHE_KEY = "cache_key";
    private static final String COL_VALUE = "value";
    private static final String COL_TIMESTAMP = "timestamp";

    private final SQLiteDatabase db;
    private final String mCacheName;

    public EntityStorageCache(String cacheName) {
        this(cacheName, CommCareApplication._().getUserDbHandle());
    }

    public EntityStorageCache(String cacheName, SQLiteDatabase db) {
        this.db = db;
        this.mCacheName = cacheName;
    }

    public static String getTableDefinition() {
        return "CREATE TABLE " + TABLE_NAME + "(" +
                DatabaseHelper.ID_COL + " INTEGER PRIMARY KEY, " +
                COL_CACHE_NAME + ", " +
                COL_ENTITY_KEY + ", " +
                COL_CACHE_KEY + ", " +
                COL_VALUE + ", " +
                COL_TIMESTAMP +
                ")";
    }

    public static void createIndexes(SQLiteDatabase db) {
        db.execSQL(DatabaseAppOpenHelper.indexOnTableCommand("CACHE_TIMESTAMP", TABLE_NAME, COL_CACHE_NAME + ", " + COL_TIMESTAMP));
        db.execSQL(DatabaseAppOpenHelper.indexOnTableCommand("NAME_ENTITY_KEY", TABLE_NAME, COL_CACHE_NAME + ", " + COL_ENTITY_KEY + ", " + COL_CACHE_KEY));
    }

    //TODO: We should do some synchronization to make it the case that nothing can hold
    //an object for the same cache at once

    public void cache(String entityKey, String cacheKey, String value) {
        long timestamp = System.currentTimeMillis();
        //TODO: this should probably just be an ON CONFLICT REPLACE call
        int removed = db.delete(TABLE_NAME, COL_CACHE_NAME + " = ? AND " + COL_ENTITY_KEY + " = ? AND " + COL_CACHE_KEY + " =?", new String[]{this.mCacheName, entityKey, cacheKey});
        if (SqlStorage.STORAGE_OUTPUT_DEBUG) {
            System.out.println("Deleted " + removed + " cached values for existing cache value on entity " + entityKey + " on insert");
        }
        //We need to clear this cache value if it exists first.
        ContentValues cv = new ContentValues();
        cv.put(COL_CACHE_NAME, mCacheName);
        cv.put(COL_ENTITY_KEY, entityKey);
        cv.put(COL_CACHE_KEY, cacheKey);
        cv.put(COL_VALUE, value);
        cv.put(COL_TIMESTAMP, timestamp);
        db.insert(TABLE_NAME, null, cv);

        if (SqlStorage.STORAGE_OUTPUT_DEBUG) {
            Log.d(TAG, "Cached value|" + entityKey + "|" + cacheKey);
        }
    }

    public String retrieveCacheValue(String entityKey, String cacheKey) {
        String whereClause = String.format("%s = ? AND %s = ? AND %s = ?", COL_CACHE_NAME, COL_ENTITY_KEY, COL_CACHE_KEY);

        Cursor c = db.query(TABLE_NAME, new String[]{COL_VALUE}, whereClause, new String[]{mCacheName, entityKey, cacheKey}, null, null, null);
        try {
            if (c.moveToNext()) {
                return c.getString(0);
            } else {
                return null;
            }
        } finally {
            c.close();
        }
    }

    /**
     * Removes cache records associated with the provided ID
     */
    public void invalidateCache(String recordId) {
        int removed = db.delete(TABLE_NAME, COL_CACHE_NAME + " = ? AND " + COL_ENTITY_KEY + " = ?", new String[]{this.mCacheName, recordId});
        if (SqlStorage.STORAGE_OUTPUT_DEBUG) {
            Log.d(TAG, "Invalidated " + removed + " cached values for entity " + recordId);
        }
    }

    public static int getSortFieldIdFromCacheKey(String detailId, String cacheKey) {
        String intId = cacheKey.substring(detailId.length() + 1);
        try {
            return Integer.parseInt(intId);
        } catch (NumberFormatException nfe) {
            //TODO: Kill this cache key if this didn't work
            return -1;
        }
    }
}
