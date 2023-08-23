package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Models.Product;
import com.example.demo.Repository.ProductRepository;

import jakarta.validation.Valid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
// @RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/products")
    public ResponseEntity<String> createProduct(@Valid @RequestParam String name,
            @Valid @RequestParam double price,
            @Valid @RequestParam("image") MultipartFile image) throws IOException {
        String imagePath = saveImage(image);

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setImage(imagePath);

        productRepository.save(product);

        return ResponseEntity.status(HttpStatus.CREATED).body("Product created successfully");
    }

    @GetMapping({ "/products", "products/" })
    public ResponseEntity<?> getAllProducts() {

        List<Product> products = productRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(products);

    }

    @PutMapping("/products/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable Long id,
            @Valid @RequestParam(required = false) String name,
            @Valid @RequestParam(required = false) Double price,
            @Valid @RequestParam(required = false) MultipartFile image) throws IOException {
        Optional<Product> productToUpdate = productRepository.findById(id);

        if (productToUpdate.isPresent()) {
            Product product = productToUpdate.get();

            if (name != null) {
                product.setName(name);
            }
            if (price != null) {
                product.setPrice(price);
            }
            if (image != null) {
                String imagePath = saveImage(image);
                deleteImage(product.getImage());

                product.setImage(imagePath);
            }

            productRepository.save(product);

            return ResponseEntity.ok("Product updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) throws IOException {
        Optional<Product> productToDelete = productRepository.findById(id);

        if (productToDelete.isPresent()) {
            deleteImage(productToDelete.get().getImage());

            productRepository.delete(productToDelete.get());
            return ResponseEntity.ok("Product deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
    }

    private String saveImage(MultipartFile image) throws IOException {
        String imageName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        Path imagePath = Path.of("uploads", imageName); // Change this to your desired image storage location

        Files.createDirectories(imagePath.getParent());
        Files.write(imagePath, image.getBytes());

        return imageName;
    }

    private void deleteImage(String imagePath) throws IOException {
        Path oldImagePath = Path.of("uploads", imagePath);
        Files.deleteIfExists(oldImagePath);
    }

    @GetMapping("/products/search")
    public Page<Product> getAllProducts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sort", defaultValue = "id") String sortField,
            @RequestParam(name = "order", defaultValue = "asc") String sortOrder,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Sort sort = Sort.by(sortOrder.equalsIgnoreCase("asc") ? Sort.Order.asc(sortField) : Sort.Order.desc(sortField));
        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword != null) {
            return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            return productRepository.findAll(pageable);
        }
    }
}
