package org.example.appsmallcrm.mapper;

import org.example.appsmallcrm.dto.CustomerDTO;
import org.example.appsmallcrm.entity.Customer;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    CustomerDTO toDto(Customer customer);

    List<CustomerDTO> toDto(List<Customer> customers);

    Customer toEntity(CustomerDTO customerDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCustomerFromDto(CustomerDTO customerDTO, @MappingTarget Customer customer);
}