package org.example.appsmallcrm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.appsmallcrm.dto.ProductDTO;
import org.example.appsmallcrm.entity.Product;
import org.example.appsmallcrm.exception.ResourceNotFoundException;
import org.example.appsmallcrm.mapper.ProductMapper;
import org.example.appsmallcrm.repo.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ActivityService activityService;
    private final UserService userService; // To get current user for logging

    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable /*, Specification<Product> spec */) {
        // return productRepository.findAll(spec, pageable).map(productMapper::toDto);
        return productRepository.findAll(pageable).map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);
        // Set default status if not provided, or validate status
        if (product.getStatus() == null) product.setStatus("Available");

        Product savedProduct = productRepository.save(product);
        log.info("Product created: {} (ID: {})", savedProduct.getName(), savedProduct.getId());
        activityService.recordActivity(userService.getCurrentUsernameOrSystem(), "PRODUCT_CREATED", "Product '" + savedProduct.getName() + "' created.");
        return productMapper.toDto(savedProduct);
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productMapper.updateProductFromDto(productDTO, existingProduct);
        // existingProduct.setUpdatedAt(LocalDateTime.now()); // If Product has updatedAt field

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated: {} (ID: {})", updatedProduct.getName(), updatedProduct.getId());
        activityService.recordActivity(userService.getCurrentUsernameOrSystem(), "PRODUCT_UPDATED", "Product '" + updatedProduct.getName() + "' (ID: " + id + ") updated.");
        return productMapper.toDto(updatedProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        // Add checks here if product is part of any active orders, etc.
        productRepository.delete(product);
        log.info("Product deleted: {} (ID: {})", product.getName(), product.getId());
        activityService.recordActivity(userService.getCurrentUsernameOrSystem(), "PRODUCT_DELETED", "Product '" + product.getName() + "' (ID: " + id + ") deleted.");
    }
}