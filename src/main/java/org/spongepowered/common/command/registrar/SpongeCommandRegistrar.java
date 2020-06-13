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
package org.spongepowered.common.command.registrar;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandFailedRegistrationException;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.brigadier.tree.RootCommandNodeBridge;
import org.spongepowered.common.command.manager.SpongeCommandManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public abstract class SpongeCommandRegistrar<T extends Command> implements CommandRegistrar {

    private final Map<String, T> commandMap = new TreeMap<>();
    private final CatalogKey catalogKey;

    SpongeCommandRegistrar(final CatalogKey catalogKey) {
        this.catalogKey = catalogKey;
    }

    /**
     * <strong>Do not call this directly.</strong> This must ONLY be called from
     * {@link SpongeCommandManager}, else the manager won't know to redirect
     * to here!
     *
     * @param container The {@link PluginContainer} of the owning plugin
     * @param command The command to register
     * @param primaryAlias The primary alias
     * @param secondaryAliases The secondary aliases, for the mapping
     * @return The mapping
     * @throws CommandFailedRegistrationException If no mapping could be created.
     */
    public CommandMapping register(final PluginContainer container, final T command, final String primaryAlias, final String[] secondaryAliases)
            throws CommandFailedRegistrationException {
        if (getDispatcher().findNode(Collections.singletonList(primaryAlias.toLowerCase())) != null) {
            // we have a problem
            throw new CommandFailedRegistrationException("The primary alias " + primaryAlias + " has already been registered.");
        }

        Tuple<CommandMapping, LiteralCommandNode<CommandSource>> mappingResult =
                BrigadierCommandRegistrar.INSTANCE
                        .registerInternal(this, container, secondaryAliases, createNode(primaryAlias.toLowerCase(), command));
        return mappingResult.getFirst();
    }

    @NonNull
    @Override
    public CommandResult process(@NonNull final CommandCause cause, @NonNull final String command, @NonNull final String arguments) throws CommandException {
        try {
            return CommandResult.builder().setResult(getDispatcher().execute(createCommandString(command, arguments),
                    (CommandSource) cause)).build();
        } catch (CommandSyntaxException e) {
            // We'll unwrap later.
            throw new CommandException(Text.of(e.getMessage()), e);
        }
    }

    @Override
    @NonNull
    public List<String> suggestions(@NonNull final CommandCause cause, @NonNull final String command, @NonNull final String arguments) {
        try {
            return getDispatcher().getCompletionSuggestions(
                    getDispatcher().parse(createCommandString(command, arguments), (CommandSource) cause)
            ).join().getList().stream().map(Suggestion::getText).collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    @NonNull
    public Optional<Text> help(@NonNull final CommandCause cause, @NonNull final String command) {
        T commandEntry = this.commandMap.get(command.toLowerCase());
        if (commandEntry == null) {
            throw new IllegalArgumentException(command + " is not a valid a valid command!");
        }

        return commandEntry.getHelp(cause);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void unregister(@NonNull final CommandMapping mapping) {
        if (Sponge.getCommandManager().isRegistered(mapping)) {
            this.commandMap.remove(mapping.getPrimaryAlias());
            ((RootCommandNodeBridge<CommandSource>) getDispatcher()).bridge$removeNode(
                    getDispatcher().findNode(Collections.singletonList(mapping.getPrimaryAlias()))
            );
        }
    }

    @Override
    @NonNull
    public CatalogKey getKey() {
        return this.catalogKey;
    }

    Map<String, T> getCommandMap() {
        return this.commandMap;
    }

    private String createCommandString(final String command, final String argument) {
        if (argument.isEmpty()) {
            return command;
        }

        return command + " " + argument;
    }

    abstract LiteralArgumentBuilder<CommandSource> createNode(final String primaryAlias, final T command);

    protected CommandDispatcher<CommandSource> getDispatcher() {
        return SpongeImpl.getServer().getCommandManager().getDispatcher();
    }
}
