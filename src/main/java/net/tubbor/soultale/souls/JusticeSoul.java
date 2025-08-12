package net.tubbor.soultale.souls;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.tubbor.soultale.attachment.ModAttachmentType;
import net.tubbor.soultale.attachment.ModCustomAttachedData;

import java.util.*;

public class JusticeSoul {
    private static final Map<UUID, Long> REVENGE_MARKS = new HashMap<>();
    private static final long REVENGE_DURATION = 5000; // ms
    private static final Set<UUID> REENTRANCY_GUARD = Collections.synchronizedSet(new HashSet<>());

    public static void register() {
        // Hasar olayları
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            LivingEntity attacker = source.getAttacker() instanceof LivingEntity le ? le : null;

            // 1) Revenge mark verme
            if (entity instanceof ServerPlayerEntity damagedPlayer) {
                ModCustomAttachedData data = damagedPlayer.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
                if (data != null && "Justice".equals(data.soul()) && attacker != null) {
                    REVENGE_MARKS.put(attacker.getUuid(), System.currentTimeMillis() + REVENGE_DURATION);
                }
            }

            // 2) Bonus hasar uygulama
            if (attacker instanceof ServerPlayerEntity attackingPlayer) {
                ModCustomAttachedData data = attackingPlayer.getAttached(ModAttachmentType.SOUL_ATTACHMENT_TYPE);
                if (data != null && "Justice".equals(data.soul())) {
                    Long expire = REVENGE_MARKS.get(entity.getUuid());
                    if (expire != null) {
                        if (System.currentTimeMillis() <= expire) {
                            if (REENTRANCY_GUARD.add(entity.getUuid())) {
                                try {
                                    float extra = damageTaken * 0.25f; // %25 ek
                                    if (extra > 0) {
                                        entity.damage(attackingPlayer.getDamageSources().playerAttack(attackingPlayer), extra);
                                    }
                                } finally {
                                    REENTRANCY_GUARD.remove(entity.getUuid());
                                }
                            }
                        } else {
                            REVENGE_MARKS.remove(entity.getUuid());
                        }
                    }
                }
            }
        });

        // Revenge mark particle gösterimi
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<UUID, Long>> it = REVENGE_MARKS.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<UUID, Long> entry = it.next();
                if (now > entry.getValue()) {
                    it.remove(); // süresi bitmiş
                    continue;
                }

                for (ServerWorld world : server.getWorlds()) {
                    LivingEntity target = (LivingEntity) world.getEntity(entry.getKey());
                    if (target != null) {
                        double x = target.getX();
                        double y = target.getY() + target.getHeight() + 0.5; // başın biraz üstü
                        double z = target.getZ();
                        world.spawnParticles(ParticleTypes.ANGRY_VILLAGER, x, y, z, 1, 0, 0, 0, 0);
                    }
                }
            }
        });
    }
}
