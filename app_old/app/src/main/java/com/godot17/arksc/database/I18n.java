package com.godot17.arksc.database;

import static com.godot17.arksc.utils.Utils.getAssets2CacheDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godot17.arksc.App;
import com.godot17.arksc.database.recruit.RecruitManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class I18n {
    private final JsonNode node;

    private I18n() throws IOException {
        String filepath = getAssets2CacheDir(App.getInstance(), "i18n.json");
        InputStream inputStream = Files.newInputStream(Paths.get(filepath));
        ObjectMapper mp = new ObjectMapper();
        node = mp.readTree(inputStream);
    }

    private static class I18nHolder {
        private static final I18n i18n;

        static {
            try {
                i18n = new I18n();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static I18n getInstance() {
        return I18nHolder.i18n;
    }

    public String convert(String code) {
        return node.get(code).asText();
    }
}
