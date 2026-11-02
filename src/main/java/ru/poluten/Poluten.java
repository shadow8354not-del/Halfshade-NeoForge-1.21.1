package ru.poluten;

import com.iafenvoy.origins.attachment.PowerHelper;
import com.iafenvoy.origins.data.power.component.builtin.ResourceComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.*;
import net.minecraft.tags.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod("poluten")
public final class Poluten {
    private static final String DEADLINE="poluten_deadline", INSIDE="poluten_inside", POISON="poluten_poison";
    private static final ResourceLocation HALF=ResourceLocation.fromNamespaceAndPath("poluten","last_breath_health");
    public Poluten() { NeoForge.EVENT_BUS.register(this); }
    private static ResourceComponent resource(Entity p,String name) {
        return PowerHelper.get(p).getComponent(ResourceLocation.fromNamespaceAndPath("poluten",name),ResourceComponent.class).orElse(null);
    }
    private static boolean chosen(Entity p) { return resource(p,"form")!=null; }
    private static boolean shadow(Entity p) { ResourceComponent r=resource(p,"form"); return r!=null&&r.getValue()==1; }
    private static boolean isSword(ItemStack stack) { return stack.getItem() instanceof SwordItem || stack.is(ItemTags.SWORDS); }
    private static void form(Entity p,int value) { ResourceComponent r=resource(p,"form"); if(r!=null&&r.getValue()!=value)r.setValue(value); }
    private static boolean inside(Player p) {
        AABB box=p.getBoundingBox().deflate(.001);
        for(BlockPos pos:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))) {
            var shape=p.level().getBlockState(pos).getCollisionShape(p.level(),pos);
            for(AABB part:shape.toAabbs()) if(part.move(pos).intersects(box))return true;
        }
        return false;
    }
    private static long now(Player p) { return p.getServer().overworld().getGameTime(); }
    private static boolean dying(Player p) { return p.getPersistentData().getLong(DEADLINE)>0; }
    private static void health(Player p,boolean half) {
        AttributeInstance a=p.getAttribute(Attributes.MAX_HEALTH);
        if(half&&!a.hasModifier(HALF))a.addTransientModifier(new AttributeModifier(HALF,-.5,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if(!half&&a.hasModifier(HALF))a.removeModifier(HALF);
    }
    @SubscribeEvent public void tick(PlayerTickEvent.Post e) {
        if(!(e.getEntity() instanceof ServerPlayer p)||!p.isAlive())return;
        CompoundTag d=p.getPersistentData();
        if(dying(p)) {
            if(now(p)>=d.getLong(DEADLINE)) { p.setHealth(0); p.die(p.damageSources().genericKill()); return; }
            health(p,true); form(p,1);
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,2,1,false,false,true));
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,2,0,false,false,true));
        }
        if(!chosen(p)) { health(p,dying(p)); return; }
        boolean in=inside(p);
        ResourceComponent req=resource(p,"request");
        if(req!=null&&req.getValue()>0) {
            req.setValue(0);
            if(!dying(p)) {
                if(shadow(p)) {
                    if(!in)form(p,0);
                    else p.displayClientMessage(Component.literal("Нельзя выйти из тени внутри блоков."),true);
                } else if(p.getFoodData().getFoodLevel()>6)form(p,1);
            }
        }
        if(!dying(p)&&p.getFoodData().getFoodLevel()<=6)form(p,0);
        if(shadow(p)) {
            p.fallDistance=0;
            if(p.containerMenu!=p.inventoryMenu)p.closeContainer();
            if(p.isUsingItem())p.stopUsingItem();
            if(d.getBoolean(INSIDE)&&!in)p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,40,0));
            if(p.level().isDay()&&p.level().dimensionType().hasSkyLight()&&!p.level().isRainingAt(p.blockPosition())&&p.level().canSeeSky(p.blockPosition().above()))p.igniteForSeconds(2);
            if(!dying(p)&&p.tickCount%20==0&&p.getFoodData().getFoodLevel()<=8)p.displayClientMessage(Component.literal("Голод: "+p.getFoodData().getFoodLevel()+" — тень исчезнет на 6!"),true);
        }
        d.putBoolean(INSIDE,shadow(p)&&in);
        if(p.level().dimensionType().hasSkyLight()&&p.level().isNight())p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,2,0,false,false,true));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public void death(LivingDeathEvent e) {
        if(!(e.getEntity() instanceof ServerPlayer p)||!chosen(p)||dying(p))return;
        p.getPersistentData().putLong(DEADLINE,now(p)+200);
        health(p,true); p.setHealth(p.getMaxHealth()); form(p,1);
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,200,1,false,false,true));
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,0,false,false,true));
        p.displayClientMessage(Component.literal("Последний вздох — осталось 10 секунд."),true);
        e.setCanceled(true);
    }
    @SubscribeEvent public void clone(PlayerEvent.Clone e) {
        if(!e.isWasDeath())return;
        Player p=e.getEntity();
        p.getPersistentData().remove(DEADLINE);p.getPersistentData().remove(POISON);p.getPersistentData().remove(INSIDE);
        health(p,false);form(p,0);
    }
    @SubscribeEvent public void respawn(PlayerEvent.PlayerRespawnEvent e) { if(!e.isEndConquered()){ form(e.getEntity(),0); health(e.getEntity(),false); } }
    @SubscribeEvent public void totem(LivingUseTotemEvent e) { if(chosen(e.getEntity()))e.setCanceled(true); }
    @SubscribeEvent public void damage(LivingDamageEvent.Pre e) { if(chosen(e.getEntity())&&e.getSource().is(DamageTypeTags.IS_FIRE))e.setNewDamage(e.getNewDamage()*2); }
    @SubscribeEvent public void fall(LivingFallEvent e) { if(shadow(e.getEntity()))e.setCanceled(true); }
    @SubscribeEvent public void attack(AttackEntityEvent e) {
        Player p=e.getEntity();
        if(shadow(p)&&(!isSword(p.getMainHandItem())||inside(p)))e.setCanceled(true);
    }
    @SubscribeEvent public void hit(LivingDamageEvent.Post e) {
        if(!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getSource().getDirectEntity()!=p||!shadow(p)||!isSword(p.getMainHandItem())||e.getNewDamage()<=0||inside(p))return;
        CompoundTag d=p.getPersistentData(); long remaining=d.getLong(POISON)-now(p);
        if(remaining>0&&remaining<=120)return;
        // Only spend the cooldown if the target actually accepts the effect.
        if(e.getEntity().addEffect(new MobEffectInstance(MobEffects.WITHER,60,0,false,true,true),p)) {
            d.putLong(POISON,now(p)+120);
            p.displayClientMessage(Component.literal("Теневой яд — Иссушение I (3 с)"),true);
        }
    }
    @SubscribeEvent public void use(LivingEntityUseItemEvent.Start e) { if(shadow(e.getEntity()))e.setCanceled(true); }
    @SubscribeEvent public void rightBlock(PlayerInteractEvent.RightClickBlock e) { if(shadow(e.getEntity()))e.setCanceled(true); }
    @SubscribeEvent public void rightItem(PlayerInteractEvent.RightClickItem e) { if(shadow(e.getEntity()))e.setCanceled(true); }
    @SubscribeEvent public void entityUse(PlayerInteractEvent.EntityInteract e) { if(shadow(e.getEntity()))e.setCanceled(true); }
    @SubscribeEvent public void entityUseSpecific(PlayerInteractEvent.EntityInteractSpecific e) { if(shadow(e.getEntity()))e.setCanceled(true); }
    @SubscribeEvent public void leftBlock(PlayerInteractEvent.LeftClickBlock e) { if(shadow(e.getEntity()))e.setCanceled(true); }
    @SubscribeEvent public void breakBlock(BlockEvent.BreakEvent e) { if(shadow(e.getPlayer()))e.setCanceled(true); }
    @SubscribeEvent public void pickup(ItemEntityPickupEvent.Pre e) { if(shadow(e.getPlayer()))e.setCanPickup(TriState.FALSE); }
}
