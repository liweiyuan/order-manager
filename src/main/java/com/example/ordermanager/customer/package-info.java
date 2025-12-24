/**
 * Customer module - Manages customer-related functionality. This module is responsible for: -
 * Customer entity management - Customer data persistence - Listening to and processing
 * order-related events
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"events", "api"})
package com.example.ordermanager.customer;
