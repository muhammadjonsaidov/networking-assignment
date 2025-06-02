package org.example.appsmallcrm.mapper;

import org.example.appsmallcrm.dto.OrderCreateDTO;
import org.example.appsmallcrm.dto.OrderDTO;
import org.example.appsmallcrm.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
// UserMapper, ProductMapper, CustomerMapper will be injected by Spring if they are components
@Mapper(componentModel = "spring", uses = {UserMapper.class, ProductMapper.class, CustomerMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(source = "product", target = "product")
    @Mapping(source = "customer", target = "customer")
    @Mapping(source = "createdBy", target = "createdBy")
    OrderDTO toDto(Order order);

    // No direct mapping from OrderCreateDTO to Order as it requires fetching entities.
    // This will be handled in the service.
}