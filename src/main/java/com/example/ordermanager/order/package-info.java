/**
 * Order module - Core module for managing orders in the system.
 * This module is responsible for:
 * - Creating and managing order entities
 * - Processing order lifecycle events
 * - Coordinating with customer and inventory modules
 * - Publishing order-related events to other modules
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = { "customer", "inventory", "events", "api" })
package com.example.ordermanager.order;