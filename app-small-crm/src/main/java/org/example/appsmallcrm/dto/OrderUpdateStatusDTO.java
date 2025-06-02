package org.example.appsmallcrm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.appsmallcrm.entity.embeddable.OrderStatus;

@Data
public class OrderUpdateStatusDTO {
    @NotNull(message = "New status is required")
    private OrderStatus newStatus;
}