package com.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.project.domain.product.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByName(String name);
    
    @Query("SELECT p FROM Product p WHERE p.stock <= p.stockThreshold")
    List<Product> findBelowThreshold();
    
    @Query("SELECT sp FROM SimpleProduct sp WHERE sp.sku = :sku")
    Optional<com.project.domain.product.SimpleProduct> findBySku(@org.springframework.data.repository.query.Param("sku") String sku);
}
