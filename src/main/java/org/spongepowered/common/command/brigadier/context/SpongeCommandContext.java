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
package org.spongepowered.common.command.brigadier.context;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.accessor.brigadier.context.CommandContextAccessor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeCommandContext extends CommandContext<CommandSource> implements org.spongepowered.api.command.parameter.CommandContext {

    private final Map<Parameter.Key<?>, Collection<?>> argumentMap;

    @SuppressWarnings("unchecked")
    public SpongeCommandContext(CommandContext<CommandSource> causeCommandContext) {
        this(causeCommandContext.getSource(),
                causeCommandContext.getInput(),
                ((CommandContextAccessor<CommandSource>) causeCommandContext).accessor$getArguments(),
                new HashMap<>(),
                causeCommandContext.getCommand(),
                causeCommandContext.getNodes(),
                causeCommandContext.getRange(),
                causeCommandContext.getChild(),
                causeCommandContext.getRedirectModifier(),
                causeCommandContext.isForked());
    }

    public SpongeCommandContext(
            CommandSource source,
            String input,
            Map<String, ParsedArgument<CommandSource, ?>> brigArguments,
            Map<Parameter.Key<?>, Collection<?>> arguments,
            Command<CommandSource> command,
            List<ParsedCommandNode<CommandSource>> nodes,
            StringRange range,
            @Nullable CommandContext<CommandSource> child,
            @Nullable RedirectModifier<CommandSource> modifier,
            boolean forks) {
        super(source,
                input,
                brigArguments,
                command,
                new RootCommandNode<>(),
                nodes,
                range,
                child,
                modifier,
                forks);
        this.argumentMap = arguments;
    }

    @Override
    @NonNull
    public Cause getCause() {
        return getCommandCause().getCause();
    }

    @Override
    @NonNull
    public Subject getSubject() {
        return getCommandCause().getSubject();
    }

    @Override
    @NonNull
    public Optional<Location> getLocation() {
        return getCommandCause().getLocation();
    }

    @Override
    @NonNull
    public Optional<BlockSnapshot> getTargetBlock() {
        return getCommandCause().getTargetBlock();
    }

    @Override
    public boolean hasAny(final Parameter.@NonNull Key<?> key) {
        Collection<?> value = this.argumentMap.get(key);
        if (value != null) {
            return !value.isEmpty();
        }
        return false;
    }

    @Override
    @NonNull
    public <T> Optional<T> getOne(final Parameter.@NonNull Key<T> key) {
        return Optional.ofNullable(getValue(key));
    }

    @Override
    @NonNull
    public <T> T requireOne(final Parameter.@NonNull Key<T> key) throws NoSuchElementException {
        T value = getValue(key);
        if (value == null) {
            throw new NoSuchElementException("No value exists for key " + key.key());
        }

        return value;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public <T> Collection<? extends T> getAll(Parameter.@NonNull Key<T> key) {
        Collection<? extends T> values = (Collection<? extends T>) this.argumentMap.get(key);
        if (values == null) {
            return ImmutableList.of();
        } else {
            return ImmutableList.copyOf(values);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> T getValue(Parameter.Key<T> key) {
        Collection<?> values = this.argumentMap.get(key);
        if (values == null || values.isEmpty()) {
            return null;
        } else if (values.size() != 1) {
            // Then don't return one
            throw new IllegalArgumentException(values.size() + " values exist for key " + key.key() + " when requireOne was called.");
        }

        return (T) values.iterator().next();
    }

    private CommandCause getCommandCause() {
        return (CommandCause) this.getSource();
    }

}
