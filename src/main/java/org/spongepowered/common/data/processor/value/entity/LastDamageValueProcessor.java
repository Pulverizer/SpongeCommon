/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.mixin.core.entity.EntityLivingBaseAccessor;

import java.util.Optional;

public class LastDamageValueProcessor extends AbstractSpongeValueProcessor<EntityLivingBase, Optional<Double>, OptionalValue<Double>> {

    public LastDamageValueProcessor() {
        super(EntityLivingBase.class, Keys.LAST_DAMAGE);
    }

    @Override
    protected OptionalValue<Double> constructValue(final Optional<Double> actualValue) {
        return SpongeValueFactory.getInstance().createOptionalValue(Keys.LAST_DAMAGE, actualValue.orElse(null));
    }

    @Override
    protected boolean set(final EntityLivingBase container, final Optional<Double> value) {
        if (value.isPresent()) {
            ((EntityLivingBaseAccessor) container).accessor$setLastDamage(value.get().floatValue());
            return true;
        }
        return false;
    }

    @Override
    protected Optional<Optional<Double>> getVal(final EntityLivingBase container) {
        return Optional.of(Optional.ofNullable(container.getRevengeTarget() == null ?
                null : new Double(((EntityLivingBaseAccessor) container).accessor$getLastDamage())));
    }

    @Override
    protected ImmutableValue<Optional<Double>> constructImmutableValue(final Optional<Double> value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
