/**
 * Copyright (c) 2024 Dylan Sperrer - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/SchematicCopyPlugin/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.schematiccopyplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.neuralnexus.ampapi.auth.AuthProvider;
import dev.neuralnexus.ampapi.auth.BasicAuthProvider;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public final class SchematicCopyPlugin extends JavaPlugin {
    private SchematicCopyConfig config;
    private AuthProvider authProvider;
    private static Logger logger;

    public static Logger logger() {
        return logger;
    }

    @Override
    public void onEnable() {
        logger = this.getLogger();

        File file = new File(this.getDataFolder(), "config.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(new SchematicCopyConfig());
        if (file.exists()) {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(file.getPath()));
                jsonString = new String(bytes, StandardCharsets.UTF_8);
                this.config = gson.fromJson(jsonString, SchematicCopyConfig.class);
            } catch (IOException e) {
                logger.warning("Failed to read config file: " + e.getMessage());
            }
        } else {
            this.config = new SchematicCopyConfig();
            try {
                Files.write(Paths.get(file.getPath()), jsonString.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                logger.warning("Failed to write config file: " + e.getMessage());
            }
        }

        this.authProvider =
                new BasicAuthProvider.Builder()
                        .panelUrl(this.config.panelUrl)
                        .username(this.config.username)
                        .password(this.config.password)
                        .build();
        this.getCommand("schemcopy").setExecutor(new SchematicCopyCommand(this.authProvider));
    }

    @Override
    public void onDisable() {
        this.authProvider = null;
    }
}
