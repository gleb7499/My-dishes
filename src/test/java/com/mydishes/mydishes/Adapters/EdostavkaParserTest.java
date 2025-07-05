package com.mydishes.mydishes.Adapters;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import com.mydishes.mydishes.Models.DishesManager;
import com.mydishes.mydishes.Parser.EdostavkaParser;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class EdostavkaParserTest {

    @Before
    public void setUp() {
        DishesManager.clear(); // Чистим список перед каждым тестом
    }

    @Test
    public void testFindReturnsResults() throws IOException {
        String query = "колбаса мортаделла";

        long start = System.nanoTime();
        EdostavkaParser.find(query);
        long end = System.nanoTime();

        double seconds = (end - start) / 1_000_000_000.0;
        System.out.printf("Время запроса -> %.3f секунд%n", seconds);

        int size = DishesManager.size();

        // Проверяем, что список не пустой
        assertNotNull(size);
        assertNotEquals("Список пуст — либо сайт не вернул данные, либо парсинг не сработал", 0, size);

        System.out.println("Найдено объектов: " + size);

        // Выводим примеры для визуальной проверки
        for (int i = 0; i < Math.min(5, size); i++) {
            DishesManager.Dish dish = DishesManager.get(i);
            System.out.println("Название: " + dish.getName());
            System.out.println("Ссылка: " + dish.getUrl());
            System.out.println("Изображение: " + dish.getImage());
        }
    }
}