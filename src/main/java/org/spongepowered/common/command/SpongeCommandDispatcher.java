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
package org.spongepowered.common.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.command.CommandMessageFormatting.SPACE_TEXT;
import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandNotFoundException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.ImmutableCommandMapping;
import org.spongepowered.api.command.dispatcher.Disambiguator;
import org.spongepowered.api.command.dispatcher.Dispatcher;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

final class SpongeCommandDispatcher implements Dispatcher {

    private static final BiPredicate<CommandSource, CommandMapping> ON_EXECUTE = (source, mapping) -> {
        if (SpongeImpl.getGlobalConfigAdapter().getConfig().getCommands().getCommandHiding().isHideDuringExecution()) {
            return mapping.getCallable().testPermission(source);
        }

        return true;
    };
    static final BiPredicate<CommandSource, CommandMapping> ON_DISCOVERY = (source, mapping) -> {
        if (SpongeImpl.getGlobalConfigAdapter().getConfig().getCommands().getCommandHiding().isHideDuringDiscovery()) {
            return mapping.getCallable().testPermission(source);
        }

        return true;
    };

    /**
     * This is a disambiguator function that returns the first matching command.
     */
    static final Disambiguator FIRST_DISAMBIGUATOR = (source, aliasUsed, availableOptions) -> {
        for (CommandMapping mapping : availableOptions) {
            if (mapping.getPrimaryAlias().toLowerCase().equals(aliasUsed.toLowerCase())) {
                return Optional.of(mapping);
            }
        }
        return Optional.of(availableOptions.get(0));
    };

    private final Disambiguator disambiguatorFunc;
    private final ListMultimap<String, CommandMapping> commands = ArrayListMultimap.create();

    /**
     * Creates a new dispatcher with a specific disambiguator.
     *
     * @param disambiguatorFunc Function that returns the preferred command if
     *     multiple exist for a given alias
     */
    public SpongeCommandDispatcher(Disambiguator disambiguatorFunc) {
        this.disambiguatorFunc = disambiguatorFunc;
    }

    /**
     * Register a given command using the given list of aliases.
     *
     * <p>If there is a conflict with one of the aliases (i.e. that alias
     * is already assigned to another command), then the alias will be skipped.
     * It is possible for there to be no alias to be available out of
     * the provided list of aliases, which would mean that the command would not
     * be assigned to any aliases.</p>
     *
     * <p>The first non-conflicted alias becomes the "primary alias."</p>
     *
     * @param callable The command
     * @param alias An array of aliases
     * @return The registered command mapping, unless no aliases could be
     *     registered
     */
    public Optional<CommandMapping> register(CommandCallable callable, String... alias) {
        checkNotNull(alias, "alias");
        return register(callable, Arrays.asList(alias));
    }

    /**
     * Register a given command using the given list of aliases.
     *
     * <p>If there is a conflict with one of the aliases (i.e. that alias
     * is already assigned to another command), then the alias will be skipped.
     * It is possible for there to be no alias to be available out of
     * the provided list of aliases, which would mean that the command would not
     * be assigned to any aliases.</p>
     *
     * <p>The first non-conflicted alias becomes the "primary alias."</p>
     *
     * @param callable The command
     * @param aliases A list of aliases
     * @return The registered command mapping, unless no aliases could be
     *     registered
     */
    public Optional<CommandMapping> register(CommandCallable callable, List<String> aliases) {
        return register(callable, aliases, Function.identity());
    }

    /**
     * Register a given command using a given list of aliases.
     *
     * <p>The provided callback function will be called with a list of aliases
     * that are not taken (from the list of aliases that were requested) and
     * it should return a list of aliases to actually register. Aliases may be
     * removed, and if no aliases remain, then the command will not be
     * registered. It may be possible that no aliases are available, and thus
     * the callback would receive an empty list. New aliases should not be added
     * to the list in the callback as this may cause
     * {@link IllegalArgumentException} to be thrown.</p>
     *
     * <p>The first non-conflicted alias becomes the "primary alias."</p>
     *
     * @param callable The command
     * @param aliases A list of aliases
     * @param callback The callback
     * @return The registered command mapping, unless no aliases could
     *     be registered
     */
    public synchronized Optional<CommandMapping> register(CommandCallable callable, List<String> aliases,
            Function<List<String>, List<String>> callback) {
        checkNotNull(aliases, "aliases");
        checkNotNull(callable, "callable");
        checkNotNull(callback, "callback");

        // Invoke the callback with the commands that /can/ be registered
        // noinspection ConstantConditions
        aliases = ImmutableList.copyOf(callback.apply(aliases));
        if (aliases.isEmpty()) {
            return Optional.empty();
        }
        String primary = aliases.get(0);
        List<String> secondary = aliases.subList(1, aliases.size());
        CommandMapping mapping = new ImmutableCommandMapping(callable, primary, secondary);

        for (String alias : aliases) {
            this.commands.put(alias.toLowerCase(), mapping);
        }

        return Optional.of(mapping);
    }

    /**
     * Remove a mapping identified by the given alias.
     *
     * @param alias The alias
     * @return The previous mapping associated with the alias, if one was found
     */
    public synchronized Collection<CommandMapping> remove(String alias) {
        return this.commands.removeAll(alias.toLowerCase());
    }

    /**
     * Remove all mappings identified by the given aliases.
     *
     * @param aliases A collection of aliases
     * @return Whether any were found
     */
    public synchronized boolean removeAll(Collection<?> aliases) {
        checkNotNull(aliases, "aliases");

        boolean found = false;

        for (Object alias : aliases) {
            if (!this.commands.removeAll(alias.toString().toLowerCase()).isEmpty()) {
                found = true;
            }
        }

        return found;
    }

    /**
     * Remove a command identified by the given mapping.
     *
     * @param mapping The mapping
     * @return The previous mapping associated with the alias, if one was found
     */
    public synchronized Optional<CommandMapping> removeMapping(CommandMapping mapping) {
        checkNotNull(mapping, "mapping");

        CommandMapping found = null;

        Iterator<CommandMapping> it = this.commands.values().iterator();
        while (it.hasNext()) {
            CommandMapping current = it.next();
            if (current.equals(mapping)) {
                it.remove();
                found = current;
            }
        }

        return Optional.ofNullable(found);
    }

    /**
     * Remove all mappings contained with the given collection.
     *
     * @param mappings The collection
     * @return Whether the at least one command was removed
     */
    public synchronized boolean removeMappings(Collection<?> mappings) {
        checkNotNull(mappings, "mappings");

        boolean found = false;

        Iterator<CommandMapping> it = this.commands.values().iterator();
        while (it.hasNext()) {
            if (mappings.contains(it.next())) {
                it.remove();
                found = true;
            }
        }

        return found;
    }

    @Override
    public synchronized Set<CommandMapping> getCommands() {
        return ImmutableSet.copyOf(this.commands.values());
    }

    @Override
    public synchronized Set<String> getPrimaryAliases() {
        Set<String> aliases = new HashSet<>();

        for (CommandMapping mapping : this.commands.values()) {
            aliases.add(mapping.getPrimaryAlias());
        }

        return Collections.unmodifiableSet(aliases);
    }

    @Override
    public synchronized Set<String> getAliases() {
        Set<String> aliases = new HashSet<>();

        for (CommandMapping mapping : this.commands.values()) {
            aliases.addAll(mapping.getAllAliases());
        }

        return Collections.unmodifiableSet(aliases);
    }

    @Override
    public Optional<CommandMapping> get(String alias) {
        return get(alias, null);
    }

    @Override
    public synchronized Optional<CommandMapping> get(String alias, @Nullable CommandSource source) {
        return get(alias, source, (src, mapping) -> mapping.getCallable().testPermission(src));
    }

    public synchronized Optional<CommandMapping> get(String alias,
            @Nullable CommandSource source,
            BiPredicate<CommandSource, CommandMapping> filter) {
        List<CommandMapping> results = this.commands.get(alias.toLowerCase());
        Optional<CommandMapping> result = Optional.empty();
        if (results.size() == 1) {
            result = Optional.of(results.get(0));
        } else if (results.size() > 1) {
            result = this.disambiguatorFunc.disambiguate(source, alias, results);
        }
        return result.filter(x -> source == null || filter.test(source, x));
    }

    @Override
    public synchronized boolean containsAlias(String alias) {
        return this.commands.containsKey(alias.toLowerCase());
    }

    @Override
    public boolean containsMapping(CommandMapping mapping) {
        checkNotNull(mapping, "mapping");

        for (CommandMapping test : this.commands.values()) {
            if (mapping.equals(test)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public CommandResult process(CommandSource source, String commandLine) throws CommandException {
        final String[] argSplit = commandLine.split(" ", 2);
        Optional<CommandMapping> cmdOptional = get(argSplit[0], source, ON_EXECUTE);
        if (!cmdOptional.isPresent()) {
            throw new CommandNotFoundException(t("commands.generic.notFound"), argSplit[0]); // TODO: Fix properly to use a SpongeTranslation??
        }
        final String arguments = argSplit.length > 1 ? argSplit[1] : "";
        CommandMapping mapping = cmdOptional.get();
        Optional<PluginContainer> pluginOwner = Sponge.getCommandManager().getOwner(mapping);
        pluginOwner.ifPresent(pluginContainer -> Sponge.getCauseStackManager().pushCause(pluginContainer));
        final CommandCallable spec = mapping.getCallable();
        Sponge.getCauseStackManager().pushCause(spec);
        try {
            return spec.process(source, arguments);
        } catch (CommandNotFoundException e) {
            throw new CommandException(t("No such child command: %s", e.getCommand()));
        } finally {
            if (pluginOwner.isPresent()) {
                Sponge.getCauseStackManager().popCause();
            }

            Sponge.getCauseStackManager().popCause();
        }
    }

    @Override
    public List<String> getSuggestions(CommandSource src, final String arguments, @Nullable Location<World> targetPosition) throws CommandException {
        final String[] argSplit = arguments.split(" ", 2);
        Optional<CommandMapping> cmdOptional = get(argSplit[0], src, ON_DISCOVERY);
        if (argSplit.length == 1) {
            return filterCommands(src, argSplit[0]).stream().collect(ImmutableList.toImmutableList());
        } else if (!cmdOptional.isPresent()) {
            return ImmutableList.of();
        }
        return cmdOptional.get().getCallable().getSuggestions(src, argSplit[1], targetPosition);
    }

    @Override
    public boolean testPermission(CommandSource source) {
        for (CommandMapping mapping : this.commands.values()) {
            if (mapping.getCallable().testPermission(source)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        if (this.commands.isEmpty()) {
            return Optional.empty();
        }
        Text.Builder build = t("Available commands:\n").toBuilder();
        for (Iterator<String> it = filterCommands(source).iterator(); it.hasNext();) {
            final Optional<CommandMapping> mappingOpt = get(it.next(), source);
            if (!mappingOpt.isPresent()) {
                continue;
            }
            CommandMapping mapping = mappingOpt.get();
            final Optional<Text> description = mapping.getCallable().getShortDescription(source);
            build.append(Text.builder(mapping.getPrimaryAlias())
                            .color(TextColors.GREEN)
                            .style(TextStyles.UNDERLINE)
                            .onClick(TextActions.suggestCommand("/" + mapping.getPrimaryAlias())).build(),
                    SPACE_TEXT, description.orElse(mapping.getCallable().getUsage(source)));
            if (it.hasNext()) {
                build.append(Text.NEW_LINE);
            }
        }
        return Optional.of(build.build());
    }

    private Set<String> filterCommands(final CommandSource src) {
        return Multimaps.filterValues(this.commands, input -> input.getCallable().testPermission(src)).keys().elementSet();
    }

    // Filter out commands by String first
    private Set<String> filterCommands(final CommandSource src, String start) {
        ListMultimap<String, CommandMapping> map = Multimaps.filterKeys(this.commands,
                input -> input != null && input.toLowerCase().startsWith(start.toLowerCase()));
        return Multimaps.filterValues(map, input -> input.getCallable().testPermission(src)).keys().elementSet();
    }

    /**
     * Gets the number of registered aliases.
     *
     * @return The number of aliases
     */
    public synchronized int size() {
        return this.commands.size();
    }

    @Override
    public Text getUsage(final CommandSource source) {
        final Text.Builder build = Text.builder();
        Iterable<String> filteredCommands = filterCommands(source).stream()
                .filter(input -> {
                    if (input == null) {
                        return false;
                    }
                    final Optional<CommandMapping> ret = get(input, source);
                    return ret.isPresent() && ret.get().getPrimaryAlias().equals(input);
                })
                .collect(Collectors.toList());

        for (Iterator<String> it = filteredCommands.iterator(); it.hasNext();) {
            build.append(Text.of(it.next()));
            if (it.hasNext()) {
                build.append(CommandMessageFormatting.PIPE_TEXT);
            }
        }
        return build.build();
    }

    @Override
    public synchronized Set<CommandMapping> getAll(String alias) {
        return ImmutableSet.copyOf(this.commands.get(alias));
    }

    @Override
    public Multimap<String, CommandMapping> getAll() {
        return ImmutableMultimap.copyOf(this.commands);
    }

}
