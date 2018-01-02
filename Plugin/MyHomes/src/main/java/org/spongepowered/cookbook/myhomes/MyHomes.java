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
package org.spongepowered.cookbook.myhomes;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;
import org.spongepowered.cookbook.myhomes.data.friends.FriendsData;
import org.spongepowered.cookbook.myhomes.data.friends.ImmutableFriendsData;
import org.spongepowered.cookbook.myhomes.data.friends.impl.FriendsDataImpl;
import org.spongepowered.cookbook.myhomes.data.friends.impl.ImmutableFriendsDataImpl;
import org.spongepowered.cookbook.myhomes.data.home.Home;
import org.spongepowered.cookbook.myhomes.data.home.HomeData;
import org.spongepowered.cookbook.myhomes.data.home.ImmutableHomeData;
import org.spongepowered.cookbook.myhomes.data.home.impl.HomeBuilder;
import org.spongepowered.cookbook.myhomes.data.home.impl.HomeDataBuilder;
import org.spongepowered.cookbook.myhomes.data.home.impl.HomeDataImpl;
import org.spongepowered.cookbook.myhomes.data.home.impl.ImmutableHomeDataImpl;

import java.util.UUID;

@Plugin(id = "myhomes", name = "MyHomes", version = "1.2", description = "An example of the data API.")
public class MyHomes {

    public static Key<Value<Home>> DEFAULT_HOME = DummyObjectProvider.createExtendedFor(Key.class, "DEFAULT_HOME");
    public static Key<MapValue<String, Home>> HOMES = DummyObjectProvider.createExtendedFor(Key.class, "HOMES");
    public static Key<ListValue<UUID>> FRIENDS = DummyObjectProvider.createExtendedFor(Key.class, "FRIENDS");

    @Inject
    private PluginContainer container;
    private DataRegistration<FriendsData, ImmutableFriendsData> FRIENDS_DATA_REGISTRATION;
    private DataRegistration<HomeData, ImmutableHomeData> HOME_DATA_REGISTRATION;

    @Listener
    public void onKeyRegistration(GameRegistryEvent.Register<Key<?>> evnet) {
        DEFAULT_HOME = Key.builder()
                .type(new TypeToken<Value<Home>>() {
                })
                .id("myhomes:default_home")
                .name("Default Home")
                .query(DataQuery.of("DefaultHome"))
                .build();
        HOMES = Key.builder()
                .type(new TypeToken<MapValue<String, Home>>() {
                })
                .id("myhomes:homes")
                .name("Homes")
                .query(DataQuery.of("Homes"))
                .build();
        FRIENDS = Key.builder()
                .type(new TypeToken<ListValue<UUID>>() {
                })
                .id("myhomes:friends")
                .name("Friends")
                .query(DataQuery.of("Friends"))
                .build();
    }

    @Listener
    public void onGameInit(GameInitializationEvent event) {
        System.err.println("derp");
    }

    @Listener
    public void onDataRegistration(GameRegistryEvent.Register<DataRegistration<?, ?>> event) {
        final DataManager dataManager = Sponge.getDataManager();
        // Home stuff
        dataManager.registerBuilder(Home.class, new HomeBuilder());
        dataManager.registerContentUpdater(Home.class, new HomeBuilder.NameUpdater());
        dataManager.registerContentUpdater(HomeData.class, new HomeDataBuilder.HomesUpdater());

        this.HOME_DATA_REGISTRATION = DataRegistration.builder()
                .dataClass(HomeData.class)
                .immutableClass(ImmutableHomeData.class)
                .dataImplementation(HomeDataImpl.class)
                .immutableImplementation(ImmutableHomeDataImpl.class)
                .dataName("Home Data")
                .manipulatorId("myhomes:home")
                .buildAndRegister(this.container);

        // Friends stuff
        this.FRIENDS_DATA_REGISTRATION = DataRegistration.builder()
                .dataClass(FriendsData.class)
                .immutableClass(ImmutableFriendsData.class)
                .dataImplementation(FriendsDataImpl.class)
                .immutableImplementation(ImmutableFriendsDataImpl.class)
                .dataName("Friends Data")
                .manipulatorId("myhomes:friends")
                .buildAndRegister(this.container);
    }

    @Listener
    public void onClientConnectionJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        player.get(DEFAULT_HOME).ifPresent(home -> {
            player.setTransform(home.getTransform());
            player.sendMessage(ChatTypes.ACTION_BAR,
                    Text.of("Teleported to home - ", TextStyles.BOLD, home.getName()));
        });
    }
}
