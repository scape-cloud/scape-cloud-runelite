package com.osrslog.runelite.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemComposition;

@Data
@RequiredArgsConstructor
public class ItemInfo {

    private final int id;
    private final int quantity;
    private final String name;

    public ItemInfo(ItemComposition item, int quantity) {
        this.id = item.getId();
        this.name = item.getName();
        this.quantity = quantity;
    }
}
