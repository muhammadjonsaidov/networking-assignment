package org.example.appsmallcrm.repo;

import org.example.appsmallcrm.entity.Order;
import org.example.appsmallcrm.entity.User;
import org.example.appsmallcrm.entity.embeddable.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findByCreatedById(Long userId, Pageable pageable);
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    List<Order> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);
    List<Order> findByStatus(OrderStatus status);
}