package com.ultramega.emirecipesharing;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
    public static final String MOD_ID = "emirecipesharing";
    public static final String MOD_NAME = "EMI Recipe Sharing";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private Constants() {
    }

    public static ResourceLocation modLoc(final String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
