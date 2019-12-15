package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class AreaEffectCloudEntityReapplicationDelayProvider extends GenericMutableDataProvider<AreaEffectCloudEntity, Integer> {

    public AreaEffectCloudEntityReapplicationDelayProvider() {
        super(Keys.AREA_EFFECT_CLOUD_REAPPLICATION_DELAY);
    }

    @Override
    protected Optional<Integer> getFrom(AreaEffectCloudEntity dataHolder) {
        return Optional.of(dataHolder.ticksExisted);
    }

    @Override
    protected boolean set(AreaEffectCloudEntity dataHolder, Integer value) {
        dataHolder.ticksExisted = value;
        return true;
    }
}