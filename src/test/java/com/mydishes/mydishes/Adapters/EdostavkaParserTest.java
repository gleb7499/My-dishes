package com.mydishes.mydishes.Adapters;

import static org.junit.Assert.assertTrue;

import com.mydishes.mydishes.Models.ProductsManager;
import com.mydishes.mydishes.Parser.EdostavkaParser;
import com.mydishes.mydishes.Parser.Parser;

import org.junit.Before;
import org.junit.Test;

public class EdostavkaParserTest {

    private Parser parser;
    private ProductsManager.Product testProduct;

    @Before
    public void setUp() {
        ProductsManager.clear();
        parser = new EdostavkaParser();

        testProduct = new ProductsManager.Product();
        testProduct.setProductURL("https://edostavka.by/product/11086");
    }

    @Test
    public void testFindProductsReturnsResults() throws Exception {
        String query = "Молоко";

        parser.findProducts(query);

        int size = ProductsManager.size();
        assertTrue("Нет результатов", size > 0);

        System.out.println("Найдено объектов: " + size);

        // Выводим первые 5 результатов
        for (int i = 0; i < Math.min(size, 5); i++) {
            ProductsManager.Product product = ProductsManager.get(i);
            System.out.println("[" + (i + 1) + "]");
            System.out.println("Название: " + product.getName());
            System.out.println("Ссылка: " + product.getProductURL());
            System.out.println("Изображение: " + product.getImageURL());
            System.out.println();
        }
    }

    @Test
    public void testParseProductDetails_realSite() throws Exception {
        ProductsManager.Product result = parser.parseProductDetails(testProduct);

        System.out.println("Калории: " + result.getCalories());
        System.out.println("Белки: " + result.getProtein());
        System.out.println("Жиры: " + result.getFat());
        System.out.println("Углеводы: " + result.getCarb());

        assertTrue(result.getCalories() > 0);
        assertTrue(result.getProtein() > 0);
        assertTrue(result.getFat() > 0);
        assertTrue(result.getCarb() > 0);
    }
}
