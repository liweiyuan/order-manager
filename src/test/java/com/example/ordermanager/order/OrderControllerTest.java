package com.example.ordermanager.order;

import com.example.ordermanager.customer.Customer;
import com.example.ordermanager.customer.CustomerManagement;
import com.example.ordermanager.inventory.InventoryItem;
import com.example.ordermanager.inventory.InventoryManagement;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper; // To convert objects to JSON

        @MockitoBean
        private OrderRepository orderRepository;

        @MockitoBean
        private CustomerManagement customerManagement;

        @MockitoBean
        private InventoryManagement inventoryManagement;

        @MockitoBean
        private OrderEventPublisher orderEventPublisher; // Mock the new event publisher

        @Test
        void getAllOrdersShouldReturnOrders() throws Exception {
                Customer customer = new Customer("Test Customer", "test@example.com");
                customer.setId(1L);

                Order order1 = new Order(customer);
                order1.setId(10L);
                order1.setStatus("PENDING");
                order1.addLineItem(1L, 2, new BigDecimal("10.00"));

                Order order2 = new Order(customer);
                order2.setId(11L);
                order2.setStatus("COMPLETED");
                order2.addLineItem(2L, 1, new BigDecimal("25.00"));

                List<Order> allOrders = Arrays.asList(order1, order2);

                when(orderRepository.findAll()).thenReturn(allOrders);

                mockMvc.perform(get("/orders")).andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].id").value(10L))
                                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                                .andExpect(jsonPath("$.data[1].id").value(11L))
                                .andExpect(jsonPath("$.data[1].status").value("COMPLETED"));
        }

        @Test
        void getOrderByIdShouldReturnOrderWhenFound() throws Exception {
                Customer customer = new Customer("Test Customer", "test@example.com");
                customer.setId(1L);

                Order order = new Order(customer);
                order.setId(10L);
                order.setStatus("PENDING");
                order.addLineItem(1L, 2, new BigDecimal("10.00"));

                when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

                mockMvc.perform(get("/orders/{id}", 10L)).andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(10L))
                                .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        void getOrderByIdShouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
                when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

                mockMvc.perform(get("/orders/{id}", 99L)).andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        void createOrderShouldReturnBadRequestWhenCustomerNotFound() throws Exception {
                OrderRequest orderRequest = new OrderRequest(1L, Collections.emptyList());
                when(customerManagement.findById(1L)).thenReturn(Optional.empty());
                assertNotNull(orderRequest);
                mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        void createOrderShouldReturnBadRequestWhenInventoryItemNotFound() throws Exception {
                Customer customer = new Customer("Test Customer", "test@example.com");
                customer.setId(1L);

                LineItemRequest lineItemRequest = new LineItemRequest(100L, 1);
                OrderRequest orderRequest =
                                new OrderRequest(1L, Collections.singletonList(lineItemRequest));

                when(customerManagement.findById(1L)).thenReturn(Optional.of(customer));
                when(inventoryManagement.findById(100L)).thenReturn(Optional.empty());

                mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        void createOrderShouldCreateOrderAndPublishEvent() throws Exception {
                Customer customer = new Customer("Test Customer", "test@example.com");
                customer.setId(1L);

                InventoryItem inventoryItem =
                                new InventoryItem("Item1", "Description for Item1", 10);
                inventoryItem.setId(100L);

                LineItemRequest lineItemRequest = new LineItemRequest(100L, 2);
                OrderRequest orderRequest =
                                new OrderRequest(1L, Collections.singletonList(lineItemRequest));

                Order savedOrder = new Order(customer);
                savedOrder.setId(10L);
                savedOrder.setStatus("PENDING");
                savedOrder.addLineItem(100L, 2, new BigDecimal("10.00"));

                when(customerManagement.findById(1L)).thenReturn(Optional.of(customer));
                when(inventoryManagement.findById(100L)).thenReturn(Optional.of(inventoryItem));
                when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

                mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(10L))
                                .andExpect(jsonPath("$.data.status").value("PENDING"));

                verify(orderRepository, times(1)).save(any(Order.class));
                verify(orderEventPublisher, times(1)).publishOrderCreated(eq(10L), anyList()); // Verify
                                                                                               // new
                                                                                               // publisher
        }

        @Test
        void updateOrderStatusShouldUpdateStatusWhenOrderFound() throws Exception {
                Customer customer = new Customer("Test Customer", "test@example.com");
                customer.setId(1L);

                Order existingOrder = new Order(customer);
                existingOrder.setId(10L);
                existingOrder.setStatus("PENDING");

                Order updatedOrder = new Order(customer);
                updatedOrder.setId(10L);
                updatedOrder.setStatus("SHIPPED");

                StatusUpdateRequest statusUpdate = new StatusUpdateRequest("SHIPPED");

                when(orderRepository.findById(10L)).thenReturn(Optional.of(existingOrder));
                when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

                mockMvc.perform(put("/orders/{id}/status", 10L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(statusUpdate)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("SHIPPED"));

                verify(orderRepository, times(1)).save(any(Order.class));
                verify(orderEventPublisher, times(1)).publishOrderStatusUpdated(eq(10L),
                                eq("PENDING"), eq("SHIPPED"));
                verify(orderEventPublisher, times(1)).publishOrderShipped(eq(10L));
        }

        @Test
        void updateOrderStatusShouldPublishDeliveredEvent() throws Exception {
                Customer customer = new Customer("Test Customer", "test@example.com");
                customer.setId(1L);

                Order existingOrder = new Order(customer);
                existingOrder.setId(10L);
                existingOrder.setStatus("SHIPPED");

                Order updatedOrder = new Order(customer);
                updatedOrder.setId(10L);
                updatedOrder.setStatus("DELIVERED");

                StatusUpdateRequest statusUpdate = new StatusUpdateRequest("DELIVERED");

                when(orderRepository.findById(10L)).thenReturn(Optional.of(existingOrder));
                when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

                mockMvc.perform(put("/orders/{id}/status", 10L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(statusUpdate)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("DELIVERED"));

                verify(orderRepository, times(1)).save(any(Order.class));
                verify(orderEventPublisher, times(1)).publishOrderStatusUpdated(eq(10L),
                                eq("SHIPPED"), eq("DELIVERED"));
                verify(orderEventPublisher, times(1)).publishOrderDelivered(eq(10L));
        }

        @Test
        void updateOrderStatusShouldPublishCancelledEvent() throws Exception {
                Customer customer = new Customer("Test Customer", "test@example.com");
                customer.setId(1L);

                Order existingOrder = new Order(customer);
                existingOrder.setId(10L);
                existingOrder.setStatus("PENDING");

                Order updatedOrder = new Order(customer);
                updatedOrder.setId(10L);
                updatedOrder.setStatus("CANCELLED");

                StatusUpdateRequest statusUpdate = new StatusUpdateRequest("CANCELLED");

                when(orderRepository.findById(10L)).thenReturn(Optional.of(existingOrder));
                when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

                mockMvc.perform(put("/orders/{id}/status", 10L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(statusUpdate)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

                verify(orderRepository, times(1)).save(any(Order.class));
                verify(orderEventPublisher, times(1)).publishOrderStatusUpdated(eq(10L),
                                eq("PENDING"), eq("CANCELLED"));
                verify(orderEventPublisher, times(1)).publishOrderCancelled(eq(10L),
                                eq("Order status updated to cancelled"));
        }

        @Test
        void updateOrderStatusShouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
                StatusUpdateRequest statusUpdate = new StatusUpdateRequest("SHIPPED");
                when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

                mockMvc.perform(put("/orders/{id}/status", 99L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(statusUpdate)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        void deleteOrderShouldDeleteOrderWhenFound() throws Exception {
                when(orderRepository.existsById(10L)).thenReturn(true);
                doNothing().when(orderRepository).deleteById(10L);

                mockMvc.perform(delete("/orders/{id}", 10L)).andExpect(status().isOk());

                verify(orderRepository, times(1)).deleteById(10L);
        }

        @Test
        void deleteOrderShouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
                when(orderRepository.existsById(anyLong())).thenReturn(false);

                mockMvc.perform(delete("/orders/{id}", 99L)).andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.code").value(404));
        }
}
