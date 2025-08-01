package com.mydishes.mydishes.Adapters;

import static org.junit.Assert.assertTrue;

import com.mydishes.mydishes.Models.Nutrition;
import com.mydishes.mydishes.Models.Product;
import com.mydishes.mydishes.Parser.EdostavkaParser;
import com.mydishes.mydishes.Parser.Parser;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class EdostavkaParserTest {

    private Parser parser;
    private Product testProduct;

    @Before
    public void setUp() {
        parser = new EdostavkaParser();

        testProduct = new Product();
        testProduct.setProductURL("https://edostavka.by/product/11086");
    }

    @Test
    public void testFindProductsReturnsResults() throws Exception {
        String query = "Молоко";

        List<Product> products = parser.findProducts(query);

        int size = products.size();
        assertTrue("Нет результатов", size > 0);

        System.out.println("Найдено объектов: " + size);

        // Выводим первые 5 результатов
        for (int i = 0; i < Math.min(size, 5); i++) {
            Product product = products.get(i);
            System.out.println("[" + (i + 1) + "]");
            System.out.println("Название: " + product.getName());
            System.out.println("Ссылка: " + product.getProductURL());
            System.out.println("Изображение: " + product.getImageURL());
            System.out.println();
        }
    }

    @Test
    public void testParseProductDetails_realSite() throws Exception {
        Product result = parser.parseProductDetails(testProduct);

        Nutrition nutrition = result.getNutrition();

        System.out.println("Калории: " + nutrition.getCalories());
        System.out.println("Белки: " + nutrition.getProtein());
        System.out.println("Жиры: " + nutrition.getFat());
        System.out.println("Углеводы: " + nutrition.getCarb());

        assertTrue(nutrition.getCalories() > 0);
        assertTrue(nutrition.getProtein() > 0);
        assertTrue(nutrition.getFat() > 0);
        assertTrue(nutrition.getCarb() > 0);
    }
}
