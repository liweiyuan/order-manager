package com.example.ordermanager.events;

import java.math.BigDecimal;

public record LineItemData(Long inventoryItemId, int quantity, BigDecimal price) {

}
