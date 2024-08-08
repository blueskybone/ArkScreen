package com.godot17.arksc.database.recruit;

import static com.godot17.arksc.utils.Utils.getAssets2CacheDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godot17.arksc.App;
import com.godot17.arksc.database.Database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RecruitManager {
    private static class RecruitManagerHolder {
        private static final RecruitManager recruitManager;

        static {
            try {
                recruitManager = new RecruitManager();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Database databaseOpe;
    private JsonNode node;

    private RecruitManager() throws IOException {
        String filepath = getAssets2CacheDir(App.getInstance(), "opedata.json");
        InputStream inputStream = Files.newInputStream(Paths.get(filepath));
        ObjectMapper mp = new ObjectMapper();
        databaseOpe = mp.readValue(inputStream, Database.class);
    }

    public static RecruitManager getInstance() {
        return RecruitManagerHolder.recruitManager;
    }

}
