package com.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.domain.order.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByCustomerId(String customerId);
    
    // As currentState is mapped with Converter, we can query by the DB string or let JPA handle it.
    // However, if the old method took a string, we might need a custom query or handle it in service.
    // We will define it normally; JPA will try to map the String to OrderState using converter.
    // Wait, the converter takes OrderState. If we want to query by string name:
    // We can just rely on findAll().stream() or use JPQL.
    
    @Query("SELECT o FROM Order o WHERE o.currentState = :state")
    List<Order> findByState(@Param("state") String stateName);
}
