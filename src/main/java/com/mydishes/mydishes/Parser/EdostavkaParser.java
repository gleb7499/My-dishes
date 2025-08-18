package com.mydishes.mydishes.Parser;

import static com.mydishes.mydishes.Utils.NutritionCalculator.parseFloatSafe;

import androidx.annotation.NonNull;

import com.mydishes.mydishes.Models.Nutrition;
import com.mydishes.mydishes.Models.Product;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

// Класс-парсер сайта https://edostavka.by.
public class EdostavkaParser extends Parser {

    // Ссылка на сайт
    private static final String BASE_URL = "https://edostavka.by";
    // Постфикс поиска сайта
    private static final String SEARCH_URL = BASE_URL + "/search?query=";


    // Поиск списка продуктов на сайте по запросу String query
    @Override
    public List<Product> findProducts(String query) throws IOException {
        List<Product> products = new ArrayList<>(MAX_RESULTS);

        // Устанавливаем запрос к странице
        String url = SEARCH_URL + URLEncoder.encode(query, "UTF-8");

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(20000)
                .get();

        // Выбираем список блоков продуктов, где каждый элемент - отдельный продукт
        Elements productsSel = doc.select(".adult-wrapper_adult__eCCJW.vertical_product__Q8mUI");

        int count = 0;

        // берем каждый продукт
        for (Element item : productsSel) {

            if (count >= MAX_RESULTS) {
                break; // Достигли лимита, выходим из цикла
            }

            // Сохраняем ссылку на продукт из системы сайта
            String productUrl = "";
            Element linkElement = item.selectFirst(".vertical_information__p_K39 a");
            if (linkElement != null) {
                productUrl = BASE_URL + linkElement.attr("href");
            }

            // Сохраняем ссылку на фото продукта и его наименование
            String imageUrl = "";
            String productName = "";

            Element imageContainer = item.selectFirst(".card-image_adult__gbuJW img");
            if (imageContainer != null) {
                imageUrl = imageContainer.attr("src");
                productName = Jsoup.parse(imageContainer.attr("alt")).text().replaceAll("\\u00AD", "").trim();
            }

            // Создаем объект продукт, записываем поля и кладем в список
            Product product = new Product();
            product.setProductURL(productUrl);
            product.setImageURL(imageUrl);
            product.setName(productName);

            products.add(product);

            count++;
        }

        // Результат -> список продуктов с сайта по запросу query
        return products;
    }


    // Считывание КБЖУ со страницы конкретного продукта.
    // Примает Product -> считывает из него ссылку на страницу продукта -> получает КБЖУ со страницы ->
    // -> возвращает копию принимаемого Product, но с КБЖУ
    @Override
    public Product parseProductDetails(@NonNull Product product) throws Exception {
        // Устанавливаем запрос к странице
        String url = product.getProductURL();

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(20000)
                .get();

        // Контейнер с КБЖУ
        Elements containers = doc.select(".preview_short__item__yJ1oI");

        if (containers.isEmpty()) throw new Exception("КБЖУ продукта не найдены!");

        // Объект Nutrition с КБЖУ
        Nutrition nutrition = new Nutrition();

        // ! Все четыре минерала имеют на сайте одинаковые классы и теги !
        for (var item : containers) {
            // Считываем блоки с названием и параметром
            Element nameBlock = item.selectFirst(".preview_short__value__onntx");
            Element valueBlock = item.selectFirst(".preview_short__key__A6ql0");

            if (nameBlock == null || valueBlock == null)
                throw new Exception("Ошибка! Попробуйте еще раз");

            // Получаем их в текстовом виде
            String nameElem = nameBlock.text();
            String valueElem = valueBlock.text();

            // Сравниваем, какой это элемент, и устанавливаем соответствующий в Nutrition
            switch (nameElem) {
                case "Энергетическая ценность":
                    nutrition.setCalories(parseFloatSafe(valueElem.split(" ")[0]));
                    break;
                case "Белки":
                    nutrition.setProtein(parseFloatSafe(valueElem));
                    break;
                case "Жиры":
                    nutrition.setFat(parseFloatSafe(valueElem));
                    break;
                case "Углеводы":
                    nutrition.setCarb(parseFloatSafe(valueElem));
                    break;
            }
        }

        // Устанавливаем Nutrition в Product
        product.setNutrition(nutrition);

        //  Результат -> копия входного Product, только + Nutrition внутри
        return product;
    }
}