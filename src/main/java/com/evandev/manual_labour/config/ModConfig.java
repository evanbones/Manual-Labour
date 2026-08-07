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
    public float itemPileRadius = 0.3F;

    @SerializedName("item_float_sink_depth")
    public float itemFloatSinkDepth = 0.15F;

    @SerializedName("pestle_tip_contact_offset")
    public float pestleTipContactOffset = 0.3F;

    @SerializedName("decorative_tool_y")
    public float decorativeToolY = 0.7F;

    @SerializedName("decorative_tool_side_offset")
    public float decorativeToolSideOffset = 0.2F;

    @SerializedName("decorative_tool_tilt")
    public float decorativeToolTilt = 30.0F;

    @SerializedName("ladle_stir_speed")
    public float ladleStirSpeed = 12.0F;

    @SerializedName("use_create_milling_recipes")
    public boolean useCreateMillingRecipes = true;

    @SerializedName("use_create_crushing_recipes")
    public boolean useCreateCrushingRecipes = false;

    @SerializedName("use_create_mixing_recipes")
    public boolean useCreateMixingRecipes = true;

    @SerializedName("use_create_deploying_recipes")
    public boolean useCreateDeployingRecipes = true;

    @SerializedName("use_create_pressing_recipes")
    public boolean useCreatePressingRecipes = true;

    @SerializedName("workstone_pressing_yield")
    public float workstonePressingYield = 0.75F;

    @SerializedName("workstone_hammer_cooldown_ticks")
    public int workstoneHammerCooldownTicks = 10;

    @SerializedName("enable_workstone_jei")
    public boolean enableWorkstoneJei = true;

    @SerializedName("enable_mortar_grinding_jei")
    public boolean enableMortarGrindingJei = true;

    @SerializedName("enable_mortar_mixing_jei")
    public boolean enableMortarMixingJei = true;

    @SerializedName("enable_manual_assembly_jei")
    public boolean enableManualAssemblyJei = true;

    @SerializedName("enable_millstone_jei")
    public boolean enableMillstoneJei = true;

    @SerializedName("enable_manual_pressing_jei")
    public boolean enableManualPressingJei = true;

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
