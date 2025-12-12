package com.fiisadev.vs_logistics.registry;

import com.fiisadev.vs_logistics.VSLogistics;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

public class LogisticsItems {
    private static final CreateRegistrate REGISTRATE = VSLogistics.registrate();

    public static ItemEntry<Item> NOZZLE = REGISTRATE.item("nozzle", Item::new)
            .model((c, p) -> p.getExistingFile(VSLogistics.asResource("item/nozzle")))
            .register();

    public static void register() {}
}
