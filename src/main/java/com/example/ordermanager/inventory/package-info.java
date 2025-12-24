/**
 * Inventory module - Manages inventory and stock-related functionality. This
 * module is responsible
 * for: - Inventory item management - Stock level tracking - Processing
 * order-related inventory
 * events - Updating stock levels based on order events
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = { "events", "api" })
package com.example.ordermanager.inventory;
