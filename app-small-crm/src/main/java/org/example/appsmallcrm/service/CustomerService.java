package org.example.appsmallcrm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.appsmallcrm.dto.CustomerDTO;
import org.example.appsmallcrm.entity.Customer;
import org.example.appsmallcrm.exception.BadRequestException;
import org.example.appsmallcrm.exception.ResourceNotFoundException;
import org.example.appsmallcrm.mapper.CustomerMapper;
import org.example.appsmallcrm.repo.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final ActivityService activityService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(customerMapper::toDto);
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        return customerRepository.findById(id)
                .map(customerMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        if (customerDTO.getEmail() != null && customerRepository.findByEmail(customerDTO.getEmail()).isPresent()) {
            throw new BadRequestException("Customer with email '" + customerDTO.getEmail() + "' already exists.");
        }
        Customer customer = customerMapper.toEntity(customerDTO);
        customer.setCreatedAt(LocalDateTime.now());

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created: {} {} (ID: {})", savedCustomer.getFirstName(), savedCustomer.getLastName(), savedCustomer.getId());
        activityService.recordActivity(userService.getCurrentUsernameOrSystem(), "CUSTOMER_CREATED", "Customer '" + savedCustomer.getFirstName() + " " + savedCustomer.getLastName() + "' created.");
        return customerMapper.toDto(savedCustomer);
    }

    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (customerDTO.getEmail() != null && !customerDTO.getEmail().equals(existingCustomer.getEmail())) {
            if (customerRepository.findByEmail(customerDTO.getEmail()).filter(c -> !c.getId().equals(id)).isPresent()) {
                throw new BadRequestException("Email '" + customerDTO.getEmail() + "' is already used by another customer.");
            }
        }

        customerMapper.updateCustomerFromDto(customerDTO, existingCustomer);
        existingCustomer.setUpdatedAt(LocalDateTime.now());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Customer updated: {} {} (ID: {})", updatedCustomer.getFirstName(), updatedCustomer.getLastName(), updatedCustomer.getId());
        activityService.recordActivity(userService.getCurrentUsernameOrSystem(), "CUSTOMER_UPDATED", "Customer '" + updatedCustomer.getFirstName() + " " + updatedCustomer.getLastName() + "' (ID: " + id + ") updated.");
        return customerMapper.toDto(updatedCustomer);
    }

    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        // Add check if customer has open orders before deletion
        customerRepository.delete(customer);
        log.info("Customer deleted: {} {} (ID: {})", customer.getFirstName(), customer.getLastName(), customer.getId());
        activityService.recordActivity(userService.getCurrentUsernameOrSystem(), "CUSTOMER_DELETED", "Customer '" + customer.getFirstName() + " " + customer.getLastName() + "' (ID: " + id + ") deleted.");
    }
}