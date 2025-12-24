#!/bin/bash

# Order Manager API Test Script
# This script tests all endpoints of the Order Manager application

set -e  # Exit on any error

# Configuration
BASE_URL="http://localhost:9000"
TIMEOUT=10

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1" >&2
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" >&2
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" >&2
}

# Function to check if the server is running
check_server() {
    print_status "Checking if server is running at $BASE_URL..."
    
    if curl -s --connect-timeout $TIMEOUT $BASE_URL/ok > /dev/null 2>&1; then
        print_success "Server is running"
        return 0
    else
        print_error "Server is not running at $BASE_URL"
        print_warning "Please start the application with: mvn spring-boot:run"
        exit 1
    fi
}

# Function to create a customer
create_customer() {
    print_status "Creating a test customer..."
    
    local response
    response=$(curl -s -X POST "$BASE_URL/customers" \
        -H "Content-Type: application/json" \
        -d '{
            "name": "Test Customer",
            "email": "test@example.com"
        }')
    
    local id
    id=$(echo $response | grep -o '"id":[0-9]*' | cut -d':' -f2)
    
    if [ -n "$id" ]; then
        print_success "Created customer with ID: $id"
        echo $id
    else
        print_error "Failed to create customer"
        echo "Response: $response"
        return 1
    fi
}

# Function to create an inventory item
create_inventory() {
    print_status "Creating a test inventory item..."
    
    local response
    response=$(curl -s -X POST "$BASE_URL/inventory" \
        -H "Content-Type: application/json" \
        -d '{
            "name": "Test Product",
            "description": "A test product for order testing",
            "stock": 100
        }')
    
    local id
    id=$(echo $response | grep -o '"id":[0-9]*' | cut -d':' -f2)
    
    if [ -n "$id" ]; then
        print_success "Created inventory item with ID: $id"
        echo $id
    else
        print_error "Failed to create inventory item"
        echo "Response: $response"
        return 1
    fi
}

# Function to create an order
create_order() {
    local customer_id=$1
    local inventory_id=$2
    
    print_status "Creating a test order for customer $customer_id with inventory item $inventory_id..."
    
    local response
    response=$(curl -s -X POST "$BASE_URL/orders" \
        -H "Content-Type: application/json" \
        -d "{
            \"customerId\": $customer_id,
            \"items\": [
                {
                    \"inventoryItemId\": $inventory_id,
                    \"quantity\": 2
                }
            ]
        }")
    
    local id
    id=$(echo $response | grep -o '"id":[0-9]*' | cut -d':' -f2)
    
    if [ -n "$id" ]; then
        print_success "Created order with ID: $id"
        echo $id
    else
        print_error "Failed to create order"
        echo "Response: $response"
        return 1
    fi
}

# Function to get all orders
get_all_orders() {
    print_status "Getting all orders..."
    
    local response
    response=$(curl -s -X GET "$BASE_URL/orders")
    local count
    count=$(echo $response | grep -o '"id"' | wc -l)
    
    print_success "Retrieved $count orders"
    echo $response | python3 -m json.tool 2>/dev/null || echo $response >&2
}

# Function to get order by ID
get_order_by_id() {
    local order_id=$1
    
    print_status "Getting order by ID: $order_id..."
    
    local response
    response=$(curl -s -X GET "$BASE_URL/orders/$order_id")
    
    if echo $response | grep -q "id.*$order_id"; then
        print_success "Successfully retrieved order $order_id"
        echo $response | python3 -m json.tool 2>/dev/null || echo $response >&2
    else
        print_error "Failed to retrieve order $order_id"
        echo "Response: $response" >&2
        return 1
    fi
}

# Function to update order status
update_order_status() {
    local order_id=$1
    local new_status=$2
    
    print_status "Updating order $order_id status to: $new_status..."
    
    local response
    response=$(curl -s -X PUT "$BASE_URL/orders/$order_id/status" \
        -H "Content-Type: application/json" \
        -d "{\"status\": \"$new_status\"}")
    
    if echo $response | grep -q "status.*$new_status"; then
        print_success "Successfully updated order $order_id status to $new_status"
        echo $response | python3 -m json.tool 2>/dev/null || echo $response >&2
    else
        print_error "Failed to update order $order_id status"
        echo "Response: $response" >&2
        return 1
    fi
}

# Function to delete an order
delete_order() {
    local order_id=$1
    
    print_status "Deleting order with ID: $order_id..."
    
    local response
    response=$(curl -s -X DELETE "$BASE_URL/orders/$order_id")
    local code
    code=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/orders/$order_id")
    
    if [ "$code" -eq 204 ] || [ "$code" -eq 200 ]; then
        print_success "Successfully deleted order $order_id (HTTP $code)"
    else
        print_error "Failed to delete order $order_id (HTTP $code)"
        echo "Response: $response" >&2
        return 1
    fi
}

# Main test sequence
main() {
    print_status "Starting Order Manager API tests..."
    echo "=========================================="
    
    # Check if server is running
    check_server

    print_status "Execute..."
    
    # Create test data
    CUSTOMER_ID=$(create_customer)
    if [ $? -ne 0 ]; then
        exit 1
    fi
    
    INVENTORY_ID=$(create_inventory)
    if [ $? -ne 0 ]; then
        exit 1
    fi
    
    # Test order creation
    ORDER_ID=$(create_order $CUSTOMER_ID $INVENTORY_ID)
    if [ $? -ne 0 ]; then
        exit 1
    fi
    
    # Test getting all orders
    get_all_orders
    
    # Test getting specific order
    get_order_by_id $ORDER_ID
    
    # Test updating order status to SHIPPED
    update_order_status $ORDER_ID "SHIPPED"
    
    # Test updating order status to DELIVERED
    update_order_status $ORDER_ID "DELIVERED"
    
    # Test updating order status to CANCELLED
    update_order_status $ORDER_ID "CANCELLED"
    
    # Test deleting the order
    delete_order $ORDER_ID
    
    print_success "All tests completed successfully!"
    echo "=========================================="
    print_status "Test Summary:"
    print_status "- Customer creation: ✓"
    print_status "- Inventory creation: ✓" 
    print_status "- Order creation: ✓"
    print_status "- Get all orders: ✓"
    print_status "- Get order by ID: ✓"
    print_status "- Update order status (SHIPPED): ✓"
    print_status "- Update order status (DELIVERED): ✓"
    print_status "- Update order status (CANCELLED): ✓"
    print_status "- Delete order: ✓"
}

# Run the main function
main "$@"