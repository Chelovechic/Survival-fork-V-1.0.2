package com.fiisadev.vs_logistics.content.fluid_pump;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;
import net.createmod.catnip.lang.Lang;

public enum PumpMode implements INamedIconOptions {
    PUSH(AllIcons.I_ROTATE_PLACE),
    PULL(AllIcons.I_ROTATE_PLACE_RETURNED);

    private final String translationKey;
    private final AllIcons icon;

    PumpMode(AllIcons icon) {
        this.icon = icon;
        translationKey = "block.vs_logistics.pump_mode." + Lang.asId(name());
    }

    @Override
    public AllIcons getIcon() {
        return icon;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

}