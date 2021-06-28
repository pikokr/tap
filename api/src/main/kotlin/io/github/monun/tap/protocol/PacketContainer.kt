/*
 * Copyright 2021 Monun
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/gpl-3.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.monun.tap.protocol

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

interface PacketContainer {
    fun sendTo(player: Player)

    fun sendTo(players: Iterable<Player>) {
        players.forEach(::sendTo)
    }

    fun sendAll()

    fun sendNearBy(world: World, x: Double, y: Double, z: Double, radius: Double)

    fun sendNearBy(loc: Location, radius: Double) {
        sendNearBy(loc.world, loc.x, loc.y, loc.z, radius)
    }
}