package com.evandev.manual_labour.compat.create;

import com.evandev.manual_labour.Constants;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;

public final class CreateCompat {

    public static final String CREATE = "create";

    private static final String COMMON_IMPL = "com.evandev.manual_labour.compat.create.impl.CreateIntegrationImpl";
    private static final String CLIENT_IMPL = "com.evandev.manual_labour.compat.create.impl.CreateClientIntegrationImpl";

    private static final CreateIntegration ABSENT = new CreateIntegration() {
    };

    private static boolean loaded;
    private static CreateIntegration integration = ABSENT;
    private static CreateClientIntegration clientIntegration = CreateClientIntegration.ABSENT;

    private CreateCompat() {
    }

    public static void init() {
        loaded = ModList.get().isLoaded(CREATE);
        if (!loaded) {
            Constants.LOG.info("Create is not installed; millstone content and Create recipe integration are disabled");
            return;
        }
        integration = instantiate(COMMON_IMPL, ABSENT);
        if (integration == ABSENT) {
            loaded = false;
            return;
        }
        // Must happen here, not at FMLClientSetupEvent: EntityRenderersEvent.RegisterRenderers is fired
        // from ClientHooks.initClientHooks in the Minecraft constructor, which runs *before* the resource
        // reload that dispatches the setup events. ModelEvent.RegisterAdditional is fired from the
        // ModelBakery constructor on a reload worker, concurrently with mod setup. Neither can see an
        // integration resolved during setup. The dist check keeps CLIENT_IMPL off the dedicated server.
        if (FMLEnvironment.dist.isClient()) {
            clientIntegration = instantiate(CLIENT_IMPL, CreateClientIntegration.ABSENT);
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static CreateIntegration get() {
        return integration;
    }

    public static CreateClientIntegration client() {
        return clientIntegration;
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiate(String className, T fallback) {
        try {
            return (T) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | LinkageError e) {
            Constants.LOG.error("Create is installed but the {} integration failed to load. "
                    + "Continuing without Create integration.", className, e);
            return fallback;
        }
    }
}
