package com.evandev.manual_labour.config;

import com.evandev.manual_labour.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve(Constants.MOD_ID + ".json").toFile();
    private static ModConfig INSTANCE;

    @SerializedName("item_pile_y")
    public float itemPileY = 0.65F;

    @SerializedName("item_pile_radius")
    public float itemPileRadius = 0.18F;

    @SerializedName("item_float_sink_depth")
    public float itemFloatSinkDepth = 0.15F;

    @SerializedName("pestle_tip_contact_offset")
    public float pestleTipContactOffset = 0.50F;

    @SerializedName("decorative_tool_y")
    public float decorativeToolY = 0.83F;

    @SerializedName("decorative_tool_side_offset")
    public float decorativeToolSideOffset = 0.30F;

    @SerializedName("decorative_tool_tilt")
    public float decorativeToolTilt = 30.0F;

    public static ModConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                Constants.LOG.error("Failed to load " + Constants.MOD_ID + ".json", e);
                INSTANCE = new ModConfig();
                save();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            Constants.LOG.error("Failed to save " + Constants.MOD_ID + ".json", e);
        }
    }

}
