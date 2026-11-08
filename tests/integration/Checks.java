package ru.poluten.test;

import com.google.gson.JsonParser;
import com.iafenvoy.origins.attachment.*;
import com.iafenvoy.origins.data.origin.*;
import com.iafenvoy.origins.data.power.*;
import com.iafenvoy.origins.data.power.component.builtin.ResourceComponent;
import com.iafenvoy.origins.data.power.builtin.regular.PhasingPower;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Runs in a disposable dedicated server; never packaged in the playable mod. */
@Mod("poluten_checks")
public class Checks {
    static final net.neoforged.neoforge.registries.DeferredRegister.Items ITEMS =
            net.neoforged.neoforge.registries.DeferredRegister.createItems("poluten_checks");
    static final java.util.function.Supplier<Item> MOD_SWORD = ITEMS.register("untagged_sword",
            () -> new SwordItem(Tiers.IRON, new Item.Properties()));
    static final ResourceLocation SOURCE = ResourceLocation.parse("poluten:test");
    static int checks;

    public Checks(net.neoforged.bus.api.IEventBus bus) {
        ITEMS.register(bus);
        NeoForge.EVENT_BUS.addListener(this::run);
    }

    static void check(boolean result, String label) {
        if (!result) throw new AssertionError(label);
        checks++;
        System.out.println("HALFSHADE PASS: " + label);
    }

    static ResourceComponent resource(ServerPlayer p, String name) {
        return PowerHelper.get(p).getComponent(ResourceLocation.parse("poluten:" + name), ResourceComponent.class).orElseThrow();
    }

    static void tick(ServerPlayer p) {
        OriginDataHolder.get(p).tick();
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(p));
        OriginDataHolder.get(p).tick();
    }

    static ServerPlayer player(ServerLevel level, boolean halfshade) {
        var p = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "HalfshadeTest"));
        p.setPos(0.5, 100, 0.5);
        if (halfshade) {
            var holder = OriginDataHolder.get(p);
            var registry = level.registryAccess().registryOrThrow(PowerRegistries.POWER_KEY);
            registry.holders().filter(h -> h.key().location().getNamespace().equals("poluten"))
                    .forEach(h -> holder.grantPower(SOURCE, h));
            holder.tick();
        }
        p.setHealth(p.getMaxHealth());
        p.getFoodData().setFoodLevel(20);
        p.getFoodData().setSaturation(0);
        return p;
    }

    static Cow cow(ServerLevel level) {
        var cow = new Cow(EntityType.COW, level);
        cow.setPos(2, 100, 0);
        return cow;
    }

    static void time(ServerLevel level, long ticks) {
        ((net.minecraft.world.level.storage.ServerLevelData) level.getLevelData()).setGameTime(level.getGameTime() + ticks);
    }

    static void verifyTranslations(ServerLevel level) throws Exception {
        var powers = level.registryAccess().registryOrThrow(PowerRegistries.POWER_KEY);
        var origin = level.registryAccess().registryOrThrow(OriginRegistries.ORIGIN_KEY)
                .getHolderOrThrow(net.minecraft.resources.ResourceKey.create(OriginRegistries.ORIGIN_KEY, ResourceLocation.parse("poluten:poluten")));
        var previous = Language.getInstance();
        Set<String> keys = null;
        try {
            for (String locale : List.of("en_us", "ru_ru")) {
                Map<String, String> translations = new HashMap<>();
                try (var stream = Files.newInputStream(ModList.get().getModFileById("poluten").getFile()
                        .findResource("assets", "poluten", "lang", locale + ".json"))) {
                    Language.loadFromJson(stream, translations::put);
                }
                if (keys != null) check(keys.equals(translations.keySet()), "both languages cover the same keys");
                keys = translations.keySet();
                Language.inject(new Language() {
                    public String getOrDefault(String key, String fallback) { return translations.getOrDefault(key, fallback); }
                    public boolean has(String key) { return translations.containsKey(key); }
                    public boolean isDefaultRightToLeft() { return false; }
                    public FormattedCharSequence getVisualOrder(FormattedText text) { return FormattedCharSequence.EMPTY; }
                });
                check(Origin.getName(origin).getString().equals(locale.equals("en_us") ? "Halfshade" : "Полутень"), locale + " origin name resolves");
                check(!Origin.getDescription(origin).getString().startsWith("origin."), locale + " origin description resolves");
                int visible = 0;
                for (var entry : powers.holders().filter(h -> h.key().location().getNamespace().equals("poluten")).toList()) {
                    var power = entry.value();
                    check(!(power instanceof EmptyPower), "decoded " + entry.key().location() + " has a real codec");
                    if (power.isHidden()) continue;
                    visible++;
                    String prefix = "power.poluten." + entry.key().location().getPath();
                    check(power.getName(entry.key().location()).getString().equals(translations.get(prefix + ".name")), locale + " title: " + entry.key().location());
                    check(power.getDescription(entry.key().location()).getString().equals(translations.get(prefix + ".description")), locale + " description: " + entry.key().location());
                }
                check(visible == 10, locale + " exactly ten visible powers; helpers stay hidden");
                check(net.minecraft.network.chat.Component.translatable("message.poluten.low_hunger", 8).getString().contains("8"), locale + " hunger substitution");
            }
        } finally { Language.inject(previous); }
    }

    void run(ServerStartedEvent event) {
        String version = ModList.get().getModContainerById("origins").orElseThrow().getModInfo().getVersion().toString();
        Path report = Path.of("halfshade-checks.txt");
        try {
            Files.deleteIfExists(report);
            var level = event.getServer().overworld();
            level.setDayTime(18000);
            level.setBlock(new BlockPos(0, 100, 0), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(new BlockPos(0, 101, 0), Blocks.AIR.defaultBlockState(), 3);
            verifyTranslations(level);
            var p = player(level, true);
            check(p.getMaxHealth() == 14, "seven hearts");
            var form = resource(p, "form");
            var request = resource(p, "request");
            p.getFoodData().setFoodLevel(6); request.setValue(1); tick(p);
            check(form.getValue() == 0, "cannot enter shadow at six hunger");
            p.getFoodData().setFoodLevel(7); request.setValue(1); tick(p);
            check(form.getValue() == 1, "can enter shadow at seven hunger");
            check(PowerHelper.get(p).anyActive(PhasingPower.class), "phasing active");
            check(p.hasEffect(MobEffects.MOVEMENT_SPEED), "night speed");
            p.getFoodData().setFoodLevel(20);
            p.getFoodData().setExhaustion(0); p.causeFoodExhaustion(0.2f);
            check(Math.abs(p.getFoodData().getExhaustionLevel() - 1f) < 0.001, "shadow exhaustion is fivefold");
            for (Item sword : List.of(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD, MOD_SWORD.get())) {
                time(level, 120);
                p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(sword));
                var target = cow(level);
                target.hurt(p.damageSources().playerAttack(p), 2);
                check(target.hasEffect(MobEffects.WITHER), "venom: " + sword);
                check(target.getEffect(MobEffects.WITHER).getDuration() == 60, "venom duration: " + sword);
                check(target.getEffect(MobEffects.WITHER).getAmplifier() == 0, "Wither I: " + sword);
            }
            check(!p.getMainHandItem().is(net.minecraft.tags.ItemTags.SWORDS), "mod sword works without vanilla sword tag");
            var blocked = cow(level); blocked.hurt(p.damageSources().playerAttack(p), 2);
            check(!blocked.hasEffect(MobEffects.WITHER), "venom cooldown shared across targets");
            time(level, 119); blocked.invulnerableTime = 0; blocked.hurt(p.damageSources().playerAttack(p), 1);
            check(!blocked.hasEffect(MobEffects.WITHER), "venom cooldown lasts full six seconds");
            time(level, 1);
            var horse = new Horse(EntityType.HORSE, level);
            horse.hurt(p.damageSources().playerAttack(p), 1);
            check(horse.hasEffect(MobEffects.WITHER), "venom applies to horses after cooldown");
            float horseHealth = horse.getHealth(); horse.invulnerableTime = 0;
            for (int i = 0; i < 41; i++) horse.getEffect(MobEffects.WITHER).tick(horse, () -> {});
            check(horse.getHealth() < horseHealth, "Wither actually deals damage");
            time(level, 120);
            var reflected = cow(level); reflected.hurt(p.damageSources().thorns(p), 1);
            check(!reflected.hasEffect(MobEffects.WITHER), "thorns is not a sword strike");
            var arrow = new net.minecraft.world.entity.projectile.Arrow(EntityType.ARROW, level);
            var ranged = cow(level); ranged.hurt(p.damageSources().arrow(arrow, p), 1);
            check(!ranged.hasEffect(MobEffects.WITHER), "projectiles cannot apply venom");
            for (Item item : List.of(Items.AIR, Items.IRON_AXE, Items.STICK)) {
                p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
                var attack = new AttackEntityEvent(p, cow(level)); NeoForge.EVENT_BUS.post(attack);
                check(attack.isCanceled(), "shadow blocks attack with " + item);
            }
            p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
            var attack = new AttackEntityEvent(p, cow(level)); NeoForge.EVENT_BUS.post(attack);
            check(!attack.isCanceled(), "shadow allows sword attack in open air");
            var use = new LivingEntityUseItemEvent.Start(p, new ItemStack(Items.APPLE), 32); NeoForge.EVENT_BUS.post(use);
            check(use.isCanceled(), "cannot eat in shadow");
            var right = new PlayerInteractEvent.RightClickItem(p, InteractionHand.MAIN_HAND); NeoForge.EVENT_BUS.post(right);
            check(right.isCanceled(), "cannot use items in shadow");
            var interact = new PlayerInteractEvent.EntityInteract(p, InteractionHand.MAIN_HAND, cow(level)); NeoForge.EVENT_BUS.post(interact);
            check(interact.isCanceled(), "cannot interact with mobs in shadow");
            var pickup = new ItemEntityPickupEvent.Pre(p, new net.minecraft.world.entity.item.ItemEntity(level, 0, 100, 0, new ItemStack(Items.STICK))); NeoForge.EVENT_BUS.post(pickup);
            check(pickup.canPickup() == TriState.FALSE, "cannot pick up items in shadow");
            var breakBlock = new BlockEvent.BreakEvent(level, p.blockPosition(), Blocks.STONE.defaultBlockState(), p); NeoForge.EVENT_BUS.post(breakBlock);
            check(breakBlock.isCanceled(), "cannot break blocks in shadow");
            var fall = new LivingFallEvent(p, 30, 1); NeoForge.EVENT_BUS.post(fall);
            check(fall.isCanceled(), "shadow blocks fall damage");
            level.setBlock(p.blockPosition(), Blocks.STONE.defaultBlockState(), 3);
            request.setValue(1); tick(p); check(form.getValue() == 1, "manual exit inside stone blocked");
            var wallAttack = new AttackEntityEvent(p, cow(level)); NeoForge.EVENT_BUS.post(wallAttack);
            check(wallAttack.isCanceled(), "cannot attack from inside walls");
            level.setBlock(p.blockPosition(), Blocks.AIR.defaultBlockState(), 3); tick(p);
            check(p.hasEffect(MobEffects.BLINDNESS) && p.getEffect(MobEffects.BLINDNESS).getDuration() == 40, "leaving walls gives two seconds blindness");
            level.setBlock(p.blockPosition(), Blocks.STONE.defaultBlockState(), 3); tick(p);
            p.getFoodData().setFoodLevel(6); tick(p); check(form.getValue() == 0, "hunger forces exit even inside stone");
            check(!PowerHelper.get(p).anyActive(PhasingPower.class), "phasing disabled after forced exit");
            level.setBlock(p.blockPosition(), Blocks.AIR.defaultBlockState(), 3);
            p.getFoodData().setExhaustion(0); p.causeFoodExhaustion(0.2f);
            check(Math.abs(p.getFoodData().getExhaustionLevel() - 0.2f) < 0.001, "normal exhaustion after exit");
            var humanTarget = cow(level); humanTarget.hurt(p.damageSources().playerAttack(p), 2);
            check(!humanTarget.hasEffect(MobEffects.WITHER), "no venom in human form");
            var humanFall = new LivingFallEvent(p, 30, 1); NeoForge.EVENT_BUS.post(humanFall);
            check(!humanFall.isCanceled(), "human form takes fall damage");
            var totem = new LivingUseTotemEvent(p, p.damageSources().generic(), new ItemStack(Items.TOTEM_OF_UNDYING), InteractionHand.OFF_HAND);
            NeoForge.EVENT_BUS.post(totem); check(totem.isCanceled(), "totem blocked in human form");
            var humanUse = new LivingEntityUseItemEvent.Start(p, new ItemStack(Items.APPLE), 32); NeoForge.EVENT_BUS.post(humanUse);
            check(!humanUse.isCanceled(), "human form may eat");
            for (var source : List.of(p.damageSources().onFire(), p.damageSources().inFire(), p.damageSources().lava())) {
                var fire = new LivingDamageEvent.Pre(p, new net.neoforged.neoforge.common.damagesource.DamageContainer(source, 2));
                NeoForge.EVENT_BUS.post(fire);
                check(fire.getNewDamage() == 4, "double damage: " + source.getMsgId());
            }
            var normalDamage = new LivingDamageEvent.Pre(p, new net.neoforged.neoforge.common.damagesource.DamageContainer(p.damageSources().generic(), 2));
            NeoForge.EVENT_BUS.post(normalDamage); check(normalDamage.getNewDamage() == 2, "non-fire damage unchanged");
            var normal = player(level, false);
            var normalDeath = new LivingDeathEvent(normal, normal.damageSources().generic()); NeoForge.EVENT_BUS.post(normalDeath);
            check(!normalDeath.isCanceled() && normal.getMaxHealth() == 20, "other origins unaffected");
            var death = new LivingDeathEvent(p, p.damageSources().generic()); NeoForge.EVENT_BUS.post(death);
            check(death.isCanceled() && p.getHealth() == 7 && p.getMaxHealth() == 7, "last breath starts with 3.5 hearts");
            tick(p); check(form.getValue() == 1, "last breath ignores low hunger");
            request.setValue(1); tick(p); check(form.getValue() == 1, "last breath cannot be toggled off");
            check(p.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() == 1, "last breath grants Strength II");
            var endReturn = player(level, true);
            NeoForge.EVENT_BUS.post(new PlayerEvent.Clone(endReturn, p, false));
            check(endReturn.getPersistentData().getLong("poluten_deadline") == p.getPersistentData().getLong("poluten_deadline"), "End return preserves last breath deadline");
            check(endReturn.getMaxHealth() == 7 && resource(endReturn, "form").getValue() == 1, "End return preserves dying form and health");
            var again = new LivingDeathEvent(p, p.damageSources().generic()); NeoForge.EVENT_BUS.post(again);
            check(!again.isCanceled(), "can be finished during last breath");
            time(level, 199); tick(p); check(p.isAlive(), "last breath still alive before ten seconds");
            time(level, 1); tick(p); check(p.isDeadOrDying(), "last breath dies exactly at deadline");
            tick(endReturn); check(endReturn.isDeadOrDying(), "End return cannot escape guaranteed death");
            var respawn = player(level, true);
            respawn.getPersistentData().putLong("poluten_deadline", level.getGameTime() + 50);
            resource(respawn, "form").setValue(1);
            NeoForge.EVENT_BUS.post(new PlayerEvent.Clone(respawn, p, true));
            tick(respawn);
            check(respawn.getMaxHealth() == 14 && resource(respawn, "form").getValue() == 0 && !respawn.getPersistentData().contains("poluten_deadline"), "respawn clears death state and restores seven hearts");
            Files.writeString(report, "PASS Origins " + version + ": " + checks + " checks\n", StandardCharsets.UTF_8);
            System.out.println("HALFSHADE ALL CHECKS PASSED: Origins " + version + ", " + checks + " checks");
        } catch (Throwable error) {
            error.printStackTrace();
            try { Files.writeString(report, "FAIL Origins " + version + ": " + error + "\n", StandardCharsets.UTF_8); }
            catch (Exception ignored) { }
            System.out.println("HALFSHADE CHECKS FAILED");
        }
        event.getServer().halt(false);
    }
}
