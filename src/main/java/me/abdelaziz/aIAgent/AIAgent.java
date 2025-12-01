package me.abdelaziz.aIAgent;

import me.abdelaziz.aIAgent.command.CreateCommand;
import me.abdelaziz.aIAgent.handler.GeminiHandler;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class AIAgent extends JavaPlugin {

    private static AIAgent instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        reloadConfig();

        loadDependencies();

        final String apiKey = getConfig().getString("api-key");
        if (apiKey == null) {
            getLogger().warning("API key is not set, exiting...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            GeminiHandler.initialize(apiKey);

            Objects.requireNonNull(getCommand("create")).setExecutor(new CreateCommand());

            getLogger().info("Gemini AI Builder Agent initialized.");
        } catch (final Exception e) {
            getLogger().warning("Invalid API key, exiting...");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled Gemini AI");
    }

    private void loadDependencies() {
        final BukkitLibraryManager libraryManager = new BukkitLibraryManager(this);
        libraryManager.addMavenCentral();

        final String jacksonPattern = "com.fasterxml.jackson";
        final String jacksonRelocated = "me.abdelaziz.aIAgent.libs.jackson";

        libraryManager.loadLibrary(Library.builder()
                .groupId("com.fasterxml.jackson.core")
                .artifactId("jackson-core")
                .version("2.16.0")
                .relocate(jacksonPattern, jacksonRelocated)
                .build());

        libraryManager.loadLibrary(Library.builder()
                .groupId("com.fasterxml.jackson.core")
                .artifactId("jackson-annotations")
                .version("2.16.0")
                .relocate(jacksonPattern, jacksonRelocated)
                .build());

        libraryManager.loadLibrary(Library.builder()
                .groupId("com.fasterxml.jackson.core")
                .artifactId("jackson-databind")
                .version("2.16.0")
                .relocate(jacksonPattern, jacksonRelocated)
                .build());

        libraryManager.loadLibrary(Library.builder()
                .groupId("com.fasterxml.jackson.datatype")
                .artifactId("jackson-datatype-jdk8")
                .version("2.16.0")
                .relocate(jacksonPattern, jacksonRelocated)
                .build());

        libraryManager.loadLibrary(Library.builder()
                .groupId("com.fasterxml.jackson.datatype")
                .artifactId("jackson-datatype-jsr310")
                .version("2.16.0")
                .relocate(jacksonPattern, jacksonRelocated)
                .build());

        libraryManager.loadLibrary(Library.builder()
                .groupId("com.google.genai")
                .artifactId("google-genai")
                .version("1.0.0")
                .relocate(jacksonPattern, jacksonRelocated)
                .build());
    }

    public static AIAgent getInstance() {
        return instance;
    }
}