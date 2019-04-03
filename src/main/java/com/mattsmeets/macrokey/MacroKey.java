package com.mattsmeets.macrokey;

import com.mattsmeets.macrokey.command.CommandMacroKey;
import com.mattsmeets.macrokey.config.ModConfig;
import com.mattsmeets.macrokey.config.ModState;
import com.mattsmeets.macrokey.handler.ChangeHandler;
import com.mattsmeets.macrokey.handler.GameTickHandler;
import com.mattsmeets.macrokey.handler.hook.ClientTickHandler;
import com.mattsmeets.macrokey.handler.hook.GuiEventHandler;
import com.mattsmeets.macrokey.handler.hook.KeyInputHandler;
import com.mattsmeets.macrokey.repository.BindingsRepository;
import com.mattsmeets.macrokey.service.JsonConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(ModReference.MOD_ID)
public class MacroKey {
    private static final Logger LOGGER = LogManager.getLogger(ModReference.MOD_ID);

    public static BindingsRepository bindingsRepository;
    public static ModState modState;

    public MacroKey() {
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
    }

    private void onServerStarting(final FMLServerStartingEvent event) {
        new CommandMacroKey(event.getCommandDispatcher());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void clientSetup(final FMLClientSetupEvent event) throws IOException {
            LOGGER.info("Hello World! Welcome to MacroKey Keybinding. Please sit back while we initialize...");
            LOGGER.debug("PreInitialization");

            // set-up the bindings.json service & files
            final JsonConfig bindingsJSONConfig = new JsonConfig(event.getMinecraftSupplier().get().gameDir.getAbsolutePath(), ModConfig.bindingFile);
            bindingsJSONConfig.initializeFile();

            // BindingsRepository has a dependency on the bindings.json file being created
            bindingsRepository = new BindingsRepository(bindingsJSONConfig);
            // Initialize the mod's state
            modState = new ModState(bindingsRepository, bindingsRepository.findActiveLayer(true));

            LOGGER.info("Init macro keys");

            MinecraftForge.EVENT_BUS.register(new GameTickHandler(null, null));
            MinecraftForge.EVENT_BUS.register(new ChangeHandler.LayerChangeHandler(bindingsRepository));
            MinecraftForge.EVENT_BUS.register(new ChangeHandler.MacroChangeHandler(bindingsRepository));
            MinecraftForge.EVENT_BUS.register(new KeyInputHandler(bindingsRepository, modState));
            MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
            MinecraftForge.EVENT_BUS.register(new GuiEventHandler(modState));
        }

        private RegistryEvents() {
            // Hide the public constructor
        }
    }
}
