package com.evandev.manual_labour.recipe;

import com.evandev.manual_labour.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ManualProcessingExclusions extends SimpleJsonResourceReloadListener {
    private static volatile Set<ResourceLocation> excluded = Set.of();

    public ManualProcessingExclusions() {
        super(new Gson(), "manual_labour/recipe_exclusions");
    }

    private static Optional<ResourceLocation> readId(ResourceLocation fileId, JsonElement element) {
        if (!element.isJsonPrimitive()) {
            Constants.LOG.error("Recipe exclusion {} has a non-string recipe id: {}", fileId, element);
            return Optional.empty();
        }
        String raw = element.getAsString();
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            Constants.LOG.error("Recipe exclusion {} has an invalid recipe id '{}'", fileId, raw);
            return Optional.empty();
        }
        return Optional.of(id);
    }

    public static boolean isExcluded(ResourceLocation recipeId) {
        return excluded.contains(recipeId);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        Set<ResourceLocation> parsed = new HashSet<>();
        object.forEach((fileId, json) -> {
            if (!json.isJsonObject()) {
                Constants.LOG.error("Recipe exclusion {} must be a JSON object", fileId);
                return;
            }
            JsonObject obj = json.getAsJsonObject();

            if (obj.has("recipe")) {
                readId(fileId, obj.get("recipe")).ifPresent(parsed::add);
            }

            if (obj.has("recipes")) {
                JsonElement recipes = obj.get("recipes");
                if (!recipes.isJsonArray()) {
                    Constants.LOG.error("Recipe exclusion {} has a 'recipes' field that isn't an array", fileId);
                } else {
                    for (JsonElement entry : recipes.getAsJsonArray()) {
                        readId(fileId, entry).ifPresent(parsed::add);
                    }
                }
            }

            if (!obj.has("recipe") && !obj.has("recipes")) {
                Constants.LOG.error("Recipe exclusion {} has neither a 'recipe' nor a 'recipes' field", fileId);
            }
        });
        excluded = Set.copyOf(parsed);
        Constants.LOG.info("Loaded {} manual processing recipe exclusion(s)", excluded.size());
    }
}
