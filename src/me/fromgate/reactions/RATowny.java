/*  
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2013, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *   * 
 *  This file is part of ReActions.
 *  
 *  ReActions is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ReActions is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReActions.  If not, see <http://www.gnorg/licenses/>.
 * 
 */
package me.fromgate.reactions;

import static com.palmergames.bukkit.towny.object.TownyObservableType.TOWN_ADD_RESIDENT;
import static com.palmergames.bukkit.towny.object.TownyObservableType.TOWN_REMOVE_RESIDENT;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.exceptions.EmptyTownException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class RATowny {

    ReActions plg;
    private Towny towny = null;

    boolean connected = false;


    public RATowny (ReActions plugin){
        this.plg = plugin;
        this.connected = connectToTowny();
    }


    private boolean connectToTowny(){
        Plugin twn = plg.getServer().getPluginManager().getPlugin("Towny");
        if ((twn != null)&&(twn instanceof Towny)){
            this.towny = (Towny) twn;
            return true;
        }
        return false;
    }


    public void kickFromTown (Player p){
        if (connected){
            Resident rsd = towny.getTownyUniverse().getResidentMap().get(p.getName());

            if (rsd.hasTown()){
                Town town;
                try {
                    town = rsd.getTown();
                    if (!rsd.isMayor())	townRemoveResident (town,rsd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void townRemoveResident (Town town, Resident resident) throws NotRegisteredException, EmptyTownException {
        if (connected){
            town.removeResident(resident);
            towny.deleteCache(resident.getName());
            TownyUniverse.getDataSource().saveResident(resident);
            TownyUniverse.getDataSource().saveTown(town);
            towny.getTownyUniverse().setChangedNotify(TOWN_REMOVE_RESIDENT);
            Bukkit.getPluginManager().callEvent(new TownRemoveResidentEvent (resident, town));
        }
    }


    public void addToTown (Player p, String town){
        if (connected){
            Town newtown = towny.getTownyUniverse().getTownsMap().get(town.toLowerCase());
            if (newtown != null){
                Resident rsd = towny.getTownyUniverse().getResidentMap().get(p.getName()); 
                if (rsd.hasTown()){
                    if (rsd.isMayor()) return;

                    Town twn = null;
                    try {
                        twn = rsd.getTown();
                        townRemoveResident  (twn, rsd);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if (!rsd.hasTown()){
                    try {
                        newtown.addResident(rsd);
                        towny.deleteCache(rsd.getName());
                        TownyUniverse.getDataSource().saveResident(rsd);
                        TownyUniverse.getDataSource().saveTown(newtown);
                        towny.getTownyUniverse().setChangedNotify(TOWN_ADD_RESIDENT);
                        Bukkit.getPluginManager().callEvent(new TownAddResidentEvent (rsd, newtown));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }					
                }
            }
        }
    }

    public boolean playerInTown(Player p, String townname){
        if (!connected) return false;
        Resident rsd = towny.getTownyUniverse().getResidentMap().get(p.getName());
        if (!rsd.hasTown()||townname.isEmpty()) return false;
        try {
            return (townname.equalsIgnoreCase(rsd.getTown().getName()));
        } catch (NotRegisteredException e) {
            return false;
        }
    }
}