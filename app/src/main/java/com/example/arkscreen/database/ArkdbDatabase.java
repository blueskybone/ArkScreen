package com.example.arkscreen.database;

import static com.example.arkscreen.Utils.Utils.getAssetsCacheFile;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {EnCh.class,Operator6.class,
        Operator5.class,Operator4.class,
        Operator3n2.class,Operator1.class},version = 1, exportSchema = false)
public abstract class ArkdbDatabase extends RoomDatabase {
    public abstract ArkdbDao ArkdbDao();

    private static volatile ArkdbDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static ArkdbDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ArkdbDatabase.class) {
                if (INSTANCE == null) {
                   String dbPath = getAssetsCacheFile(context.getApplicationContext(),"arkscreen.db");
                   File dbFile = new File(dbPath);
                   INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ArkdbDatabase.class, "ark_database")
                            .createFromFile(dbFile)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
