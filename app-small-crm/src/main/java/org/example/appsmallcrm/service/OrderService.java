package org.example.appsmallcrm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.appsmallcrm.dto.OrderCreateDTO;
import org.example.appsmallcrm.dto.OrderDTO;
import org.example.appsmallcrm.dto.OrderUpdateStatusDTO;
import org.example.appsmallcrm.entity.*;
import org.example.appsmallcrm.entity.embeddable.OrderStatus;
import org.example.appsmallcrm.exception.BadRequestException;
import org.example.appsmallcrm.exception.ResourceNotFoundException;
import org.example.appsmallcrm.mapper.OrderMapper;
import org.example.appsmallcrm.repo.CustomerRepository;
import org.example.appsmallcrm.repo.OrderRepository;
import org.example.appsmallcrm.repo.ProductRepository;
import org.example.appsmallcrm.repo.SaleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final OrderMapper orderMapper;
    private final ActivityService activityService;
    private final UserService userService;

    public OrderDTO createOrder(OrderCreateDTO createDTO) {
        User currentUser = userService.getCurrentAuthenticatedUserEntity();
        if (currentUser == null) {
            throw new BadRequestException("User must be authenticated to create an order.");
        }

        Product product = productRepository.findById(createDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", createDTO.getProductId()));

        Customer customer = customerRepository.findById(createDTO.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", createDTO.getCustomerId()));

        if (product.getStock() < createDTO.getQuantity()) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName() + ". Available: " + product.getStock());
        }

        Order order = Order.builder()
                .product(product)
                .customer(customer)
                .createdBy(currentUser)
                .quantity(createDTO.getQuantity())
                .unitPrice(product.getPrice()) // Price at the time of order
                .totalAmount(product.getPrice() * createDTO.getQuantity())
                .orderDate(LocalDate.now())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        product.setStock(product.getStock() - createDTO.getQuantity());
        productRepository.save(product);

        Order savedOrder = orderRepository.save(order);

        // Create Sale record
        Sale sale = Sale.builder()
                .product(product)
                .soldDate(savedOrder.getOrderDate())
                .revenue(savedOrder.getTotalAmount())
                .quantity(savedOrder.getQuantity())
                .build();
        saleRepository.save(sale);

        log.info("Order created: ID {}, for Customer ID: {}", savedOrder.getId(), customer.getId());
        activityService.recordActivity(currentUser.getUsername(), "ORDER_CREATED", "Order ID " + savedOrder.getId() + " created for customer " + customer.getFirstName() + " " + customer.getLastName());
        return orderMapper.toDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toDto);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByCurrentUser(Pageable pageable) {
        User currentUser = userService.getCurrentAuthenticatedUserEntity();
        if (currentUser == null) {
            throw new BadRequestException("User must be authenticated to view their orders.");
        }
        return orderRepository.findByCreatedById(currentUser.getId(), pageable).map(orderMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable).map(orderMapper::toDto);
    }

    public OrderDTO updateOrderStatus(Long orderId, OrderUpdateStatusDTO statusDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Add logic for valid status transitions if needed
        // e.g., cannot go from SHIPPED back to PENDING
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(statusDTO.getNewStatus());
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        log.info("Order ID {} status updated from {} to {}", orderId, oldStatus, statusDTO.getNewStatus());
        activityService.recordActivity(userService.getCurrentUsernameOrSystem(), "ORDER_STATUS_UPDATED", "Order ID " + orderId + " status changed from " + oldStatus + " to " + statusDTO.getNewStatus());
        return orderMapper.toDto(updatedOrder);
    }

    // No delete order functionality for now, usually orders are cancelled or archived.
}