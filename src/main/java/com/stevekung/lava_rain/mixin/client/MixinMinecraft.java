package com.stevekung.lava_rain.mixin.client;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.util.Function4;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DatapackFailureScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldLoadProgressScreen;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.network.login.ClientLoginNetHandler;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.login.client.CLoginStartPacket;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.chunk.listener.ChainedChunkStatusListener;
import net.minecraft.world.chunk.listener.TrackingChunkStatusListener;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft
{
    private final Minecraft that = (Minecraft) (Object) this;

    @Shadow
    private SaveFormat saveFormat;

    @Shadow
    private AtomicReference<TrackingChunkStatusListener> refChunkStatusListener;

    @Shadow
    private IntegratedServer integratedServer;

    @Shadow
    private Queue<Runnable> queueChunkTracking;

    @Shadow
    private boolean integratedServerIsRunning;

    @Shadow
    private CrashReport crashReporter;

    @Shadow
    private NetworkManager networkManager;

    @Shadow
    private static Logger LOGGER;

    @Shadow
    private void runGameLoop(boolean renderWorldIn) {}

    @Overwrite
    private void loadWorld(String worldName, DynamicRegistries.Impl dynamicRegistries, Function<SaveFormat.LevelSave, DatapackCodec> levelSaveToDatapackFunction, Function4<SaveFormat.LevelSave, DynamicRegistries.Impl, IResourceManager, DatapackCodec, IServerConfiguration> quadFunction, boolean vanillaOnly, Minecraft.WorldSelectionType selectionType) {
        SaveFormat.LevelSave saveformat$levelsave;
        try {
            saveformat$levelsave = this.saveFormat.getLevelSave(worldName);
        } catch (IOException ioexception2) {
            LOGGER.warn("Failed to read level {} data", worldName, ioexception2);
            SystemToast.func_238535_a_(this.that, worldName);
            this.that.displayGuiScreen((Screen)null);
            return;
        }

        Minecraft.PackManager minecraft$packmanager;
        try {
            minecraft$packmanager = this.that.reloadDatapacks(dynamicRegistries, levelSaveToDatapackFunction, quadFunction, vanillaOnly, saveformat$levelsave);
        } catch (Exception exception) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", exception);
            this.that.displayGuiScreen(new DatapackFailureScreen(() -> {
                this.loadWorld(worldName, dynamicRegistries, levelSaveToDatapackFunction, quadFunction, true, selectionType);
            }));

            try {
                saveformat$levelsave.close();
            } catch (IOException ioexception) {
                LOGGER.warn("Failed to unlock access to level {}", worldName, ioexception);
            }

            return;
        }

        IServerConfiguration iserverconfiguration = minecraft$packmanager.getServerConfiguration();
        if (true) {
            this.that.unloadWorld();
            this.refChunkStatusListener.set((TrackingChunkStatusListener)null);

            try {
                saveformat$levelsave.saveLevel(dynamicRegistries, iserverconfiguration);
                minecraft$packmanager.getDataPackRegistries().updateTags();
                YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(this.that.getProxy(), UUID.randomUUID().toString());
                MinecraftSessionService minecraftsessionservice = yggdrasilauthenticationservice.createMinecraftSessionService();
                GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
                PlayerProfileCache playerprofilecache = new PlayerProfileCache(gameprofilerepository, new File(this.that.gameDir, MinecraftServer.USER_CACHE_FILE.getName()));
                SkullTileEntity.setProfileCache(playerprofilecache);
                SkullTileEntity.setSessionService(minecraftsessionservice);
                PlayerProfileCache.setOnlineMode(false);
                this.integratedServer = MinecraftServer.startServer(thread ->
                {
                    return new IntegratedServer(thread, this.that, dynamicRegistries, saveformat$levelsave, minecraft$packmanager.getResourcePacks(), minecraft$packmanager.getDataPackRegistries(), iserverconfiguration, minecraftsessionservice, gameprofilerepository, playerprofilecache, (radius) -> {
                        TrackingChunkStatusListener trackingchunkstatuslistener = new TrackingChunkStatusListener(radius + 0);
                        trackingchunkstatuslistener.startTracking();
                        this.refChunkStatusListener.set(trackingchunkstatuslistener);
                        return new ChainedChunkStatusListener(trackingchunkstatuslistener, this.queueChunkTracking::add);
                    });
                });
                this.integratedServerIsRunning = true;
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Starting integrated server");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Starting integrated server");
                crashreportcategory.addDetail("Level ID", worldName);
                crashreportcategory.addDetail("Level Name", iserverconfiguration.getWorldName());
                throw new ReportedException(crashreport);
            }

            while(this.refChunkStatusListener.get() == null) {
                Thread.yield();
            }

            WorldLoadProgressScreen worldloadprogressscreen = new WorldLoadProgressScreen(this.refChunkStatusListener.get());
            this.that.displayGuiScreen(worldloadprogressscreen);
            this.that.getProfiler().startSection("waitForServer");

            while(!this.integratedServer.serverIsInRunLoop()) {
                worldloadprogressscreen.tick();
                this.runGameLoop(false);

                try {
                    Thread.sleep(16L);
                } catch (InterruptedException interruptedexception) {
                }

                if (this.crashReporter != null) {
                    Minecraft.displayCrashReport(this.crashReporter);
                    return;
                }
            }

            this.that.getProfiler().endSection();
            SocketAddress socketaddress = this.integratedServer.getNetworkSystem().addLocalEndpoint();
            NetworkManager networkmanager = NetworkManager.provideLocalClient(socketaddress);
            networkmanager.setNetHandler(new ClientLoginNetHandler(networkmanager, this.that, (Screen)null, statusMessage ->
            {
            }));
            networkmanager.sendPacket(new CHandshakePacket(socketaddress.toString(), 0, ProtocolType.LOGIN));
            com.mojang.authlib.GameProfile gameProfile = this.that.getSession().getProfile();
            if (!this.that.getSession().hasCachedProperties()) {
                gameProfile = this.that.getSessionService().fillProfileProperties(gameProfile, true); //Forge: Fill profile properties upon game load. Fixes MC-52974.
                this.that.getSession().setProperties(gameProfile.getProperties());
            }
            networkmanager.sendPacket(new CLoginStartPacket(gameProfile));
            this.networkManager = networkmanager;
        }
    }
}