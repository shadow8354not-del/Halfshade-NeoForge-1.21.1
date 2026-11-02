package ru.poluten.test;
import ru.poluten.Poluten;
import com.iafenvoy.origins.attachment.*;
import com.iafenvoy.origins.data.power.*;
import com.iafenvoy.origins.data.power.component.builtin.ResourceComponent;
import com.iafenvoy.origins.data.power.builtin.regular.PhasingPower;
import com.iafenvoy.origins.data.power.builtin.regular.OverlayPower;
import com.mojang.authlib.GameProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.UUID;
@Mod("poluten_checks")
public class Checks {
 static final net.neoforged.neoforge.registries.DeferredRegister.Items ITEMS=net.neoforged.neoforge.registries.DeferredRegister.createItems("poluten_checks");
 static final java.util.function.Supplier<Item> MOD_SWORD=ITEMS.register("untagged_sword",()->new SwordItem(Tiers.IRON,new Item.Properties()));
 public Checks(net.neoforged.bus.api.IEventBus bus){ITEMS.register(bus);NeoForge.EVENT_BUS.addListener(this::run);}
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);System.out.println("POLUTEN PASS: "+s);}
 void run(ServerStartedEvent e){
  try {
   var level=e.getServer().overworld();level.setDayTime(18000);
   var p=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"PolutenTest"));p.setPos(0,100,0);
   var holder=OriginDataHolder.get(p);var reg=level.registryAccess().registryOrThrow(PowerRegistries.POWER_KEY);
   reg.holders().filter(x->x.key().location().getNamespace().equals("poluten")).forEach(x->holder.grantPower(ResourceLocation.parse("poluten:test"),x));
   holder.tick();check(p.getMaxHealth()==14,"seven hearts");
   var form=PowerHelper.get(p).getComponent(ResourceLocation.parse("poluten:form"),ResourceComponent.class).orElseThrow();
   var req=PowerHelper.get(p).getComponent(ResourceLocation.parse("poluten:request"),ResourceComponent.class).orElseThrow();
   var mod=new Poluten();p.setHealth(14);p.getFoodData().setFoodLevel(20);req.setValue(1);mod.tick(new PlayerTickEvent.Post(p));holder.tick();check(form.getValue()==1,"primary ability enters shadow");
   check(PowerHelper.get(p).anyActive(PhasingPower.class),"phasing active");
   p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));
   var cow=new net.minecraft.world.entity.animal.Cow(net.minecraft.world.entity.EntityType.COW,level);cow.setPos(1,100,0);
   check(cow.hurt(p.damageSources().playerAttack(p),3),"melee damage accepted");
   check(cow.hasEffect(MobEffects.WITHER),"sword hit applies Wither");
   check(cow.getEffect(MobEffects.WITHER).getDuration()==60,"Wither lasts three seconds");
   float before=cow.getHealth();cow.invulnerableTime=0;
   for(int i=0;i<41;i++)cow.getEffect(MobEffects.WITHER).tick(cow,()->{});
   check(cow.getHealth()<before,"Wither deals damage");
   var second=new net.minecraft.world.entity.animal.Cow(net.minecraft.world.entity.EntityType.COW,level);second.setPos(1,100,1);
   second.hurt(p.damageSources().playerAttack(p),2);check(!second.hasEffect(MobEffects.WITHER),"cooldown is shared across targets");
   ((net.minecraft.world.level.storage.ServerLevelData)level.getLevelData()).setGameTime(level.getGameTime()+120);
   p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(MOD_SWORD.get()));
   check(!p.getMainHandItem().is(net.minecraft.tags.ItemTags.SWORDS),"test mod sword deliberately has no sword tag");
   second.invulnerableTime=0;second.hurt(p.damageSources().playerAttack(p),2);check(second.hasEffect(MobEffects.WITHER),"untagged mod sword applies Wither after cooldown");
   p.getFoodData().setFoodLevel(20);
   level.setBlock(p.blockPosition(),Blocks.STONE.defaultBlockState(),3);req.setValue(1);mod.tick(new PlayerTickEvent.Post(p));check(form.getValue()==1,"cannot exit inside stone");
   level.setBlock(p.blockPosition(),Blocks.AIR.defaultBlockState(),3);mod.tick(new PlayerTickEvent.Post(p));check(p.hasEffect(MobEffects.BLINDNESS),"blindness on leaving wall");
   p.getFoodData().setFoodLevel(6);mod.tick(new PlayerTickEvent.Post(p));holder.tick();check(form.getValue()==0,"hunger forces exit");
   check(!PowerHelper.get(p).anyActive(PhasingPower.class),"phasing disabled");
   check(!PowerHelper.get(p).anyActive(OverlayPower.class),"vignette removed outside shadow");
   var totem=new LivingUseTotemEvent(p,p.damageSources().generic(),new ItemStack(Items.TOTEM_OF_UNDYING),InteractionHand.OFF_HAND);mod.totem(totem);check(totem.isCanceled(),"totem blocked");
   var death=new LivingDeathEvent(p,p.damageSources().generic());mod.death(death);check(death.isCanceled()&&p.getHealth()==7&&p.getMaxHealth()==7,"last breath starts at 3.5 hearts");
   mod.tick(new PlayerTickEvent.Post(p));check(form.getValue()==1,"last breath ignores low hunger");
   var again=new LivingDeathEvent(p,p.damageSources().generic());mod.death(again);check(!again.isCanceled(),"can be finished during last breath");
   ((net.minecraft.world.level.storage.ServerLevelData)level.getLevelData()).setGameTime(level.getGameTime()+200);mod.tick(new PlayerTickEvent.Post(p));check(p.isDeadOrDying(),"deadline forces death");
   System.out.println("POLUTEN ALL CHECKS PASSED");
  }catch(Throwable t){t.printStackTrace();System.out.println("POLUTEN CHECKS FAILED");}
  e.getServer().halt(false);
 }
}



