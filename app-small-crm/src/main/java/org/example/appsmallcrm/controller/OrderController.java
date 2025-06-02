package org.example.appsmallcrm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.appsmallcrm.dto.ApiResponse;
import org.example.appsmallcrm.dto.OrderCreateDTO;
import org.example.appsmallcrm.dto.OrderDTO;
import org.example.appsmallcrm.dto.OrderUpdateStatusDTO;
import org.example.appsmallcrm.service.OrderService;
import org.example.appsmallcrm.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService; // To get current user for "my-orders"

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody OrderCreateDTO createDTO) {
        OrderDTO createdOrder = orderService.createOrder(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdOrder, "Order created."));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrders(
            @PageableDefault(size = 10, sort = "orderDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<OrderDTO> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isOrderOwnerOrRelated(authentication, #id)") // Custom security check
    // Simpler: @PreAuthorize("isAuthenticated()") if users can see their own orders by ID and order service handles logic
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        // Add logic in service or here to check if current user is allowed to see this order if not ADMIN
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getMyOrders(
            @PageableDefault(size = 10, sort = "orderDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<OrderDTO> orders = orderService.getOrdersByCurrentUser(pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or @customerSecurityService.isRelatedToCustomer(authentication, #customerId)") // Custom
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getOrdersByCustomerId(@PathVariable Long customerId,
            @PageableDefault(size = 10, sort = "orderDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<OrderDTO> orders = orderService.getOrdersByCustomerId(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }


    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Assuming users can update status of orders they created, or admin can update any.
                                              // Service layer should enforce further logic if USER can only update their own orders.
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderUpdateStatusDTO statusDTO) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, statusDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedOrder, "Order status updated."));
    }
}