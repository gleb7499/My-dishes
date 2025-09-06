package com.mydishes.mydishes.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ProductTest {

    @Test
    public void calculate_calculatesCorrectly() {
        // Arrange
        Product product1 = new Product();
        product1.setNutrition(new Nutrition(340, 7, 0.5, 77));
        product1.setMass(718);

        Product product2 = new Product();
        product2.setNutrition(new Nutrition(147, 19, 4, 0.4));
        product2.setMass(774);

        List<Product> products = new ArrayList<>();
        products.add(product1);
        products.add(product2);

        // Act
        Nutrition result = Product.calculate(products);

        // Assert
        assertEquals(239.88, result.getCalories(), 0.01);
        assertEquals(13.23, result.getProtein(), 0.01);
        assertEquals(2.32, result.getFat(), 0.01);
        assertEquals(37.26, result.getCarb(), 0.01);
    }
}
