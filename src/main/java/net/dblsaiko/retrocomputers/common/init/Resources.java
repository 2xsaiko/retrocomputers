package net.dblsaiko.retrocomputers.common.init;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import net.dblsaiko.hctm.init.RegistryObject;
import net.dblsaiko.retrocomputers.RetroComputers;

import static net.dblsaiko.retrocomputers.RetroComputersKt.MOD_ID;

public record Resources(
    byte[] bootloader,
    byte[] charset,
    Map<Identifier, byte[]> disks
) {
    private static final Identifier ID = new Identifier(MOD_ID, "data");

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleResourceReloadListener<Resources>() {
            @Override
            public CompletableFuture<Resources> load(ResourceManager manager, Profiler profiler, Executor executor) {
                return CompletableFuture.supplyAsync(() -> {
                    byte[] bootloader = loadImage(manager, "bootldr.bin");
                    byte[] charset = loadImage(manager, "charset.bin");
                    Map<Identifier, byte[]> disks =
                        RetroComputers.INSTANCE.getItems().getSysDiskObjects()
                            .stream()
                            .collect(Collectors.toUnmodifiableMap(
                                RegistryObject::id,
                                k -> loadImage(manager, "disks/%s.img".formatted(k.get().getImage().getPath()))
                            ));
                    return new Resources(bootloader, charset, disks);
                }, executor);
            }

            @Override
            public CompletableFuture<Void> apply(Resources data, ResourceManager manager, Profiler profiler, Executor executor) {
                return CompletableFuture.runAsync(() -> {
                    RetroComputers.INSTANCE.setResources(data);
                }, executor);
            }

            @Override
            public Identifier getFabricId() {
                return ID;
            }
        });
    }

    private static byte[] loadImage(ResourceManager manager, String name) {
        try (var res = manager.getResource(new Identifier(MOD_ID, name))) {
            return res.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
