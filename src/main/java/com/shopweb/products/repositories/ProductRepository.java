package com.shopweb.products.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopweb.products.models.Product;
import com.shopweb.products.models.ProductDto;

public interface ProductRepository extends JpaRepository<Product, Integer>{

}
