package com.samlol12.toolsmithsharper.registry;

import java.util.UUID;

public class ModComponents {
    public static final String SHARPER_USES = "sharper_uses";
    public static final String SHARPER_COATING = "sharper_coating";
    public static final String SHARPER_COATING_TIER = "sharper_coating_tier";

    public static final UUID SHARPER_DAMAGE_ID = UUID.fromString("f421f1d1-6b45-42f2-895b-017e88703a55");
    public static final UUID SHARPER_SPEED_ID = UUID.fromString("b33d06eb-11cb-464a-9ef8-e1de18f76159");

    public static void register() {
        // Appelé dans onInitialize pour charger la classe
    }
}