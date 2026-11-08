package ru.poluten;

import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.data.power.PowerRegistries;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Describes Java-backed powers while preserving Origins' display settings. */
public final class DescriptionPower extends Power {
    public static final MapCodec<DescriptionPower> CODEC =
            BaseSettings.CODEC.xmap(DescriptionPower::new, Power::getSettings);
    public static final DeferredRegister<MapCodec<? extends Power>> TYPES =
            DeferredRegister.create(PowerRegistries.POWER_TYPE_KEY, "poluten");

    static {
        TYPES.register("description", () -> CODEC);
    }

    public DescriptionPower(BaseSettings settings) {
        super(settings);
    }

    @Override
    public MapCodec<? extends Power> codec() {
        return CODEC;
    }
}
