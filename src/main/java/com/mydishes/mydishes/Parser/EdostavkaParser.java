package com.mydishes.mydishes.Parser;

import static com.mydishes.mydishes.Utils.ViewUtils.parseFloatSafe;

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

/**
 * Класс-парсер для сайта <a href="https://edostavka.by">https://edostavka.by</a>.
 * <p>
 * Реализует методы для поиска продуктов и извлечения информации о них (включая КБЖУ)
 * с указанного сайта.
 * </p>
 */
public class EdostavkaParser extends Parser {

    private static final String BASE_URL = "https://edostavka.by";
    private static final String SEARCH_URL = BASE_URL + "/search?query=";

    /**
     * Осуществляет поиск списка продуктов на сайте <a href="https://edostavka.by">https://edostavka.by</a> по заданному запросу.
     * <p>
     * Извлекает название продукта, URL изображения и URL страницы продукта.
     * Ограничивает количество результатов до {@link Parser#MAX_RESULTS}.
     * </p>
     *
     * @param query Поисковый запрос.
     * @return Список объектов {@link Product}, найденных на сайте.
     * @throws IOException Если возникает ошибка при подключении к сайту или обработке данных.
     */
    @Override
    public List<Product> findProducts(String query) throws IOException {
        List<Product> products = new ArrayList<>(MAX_RESULTS);

        String url = SEARCH_URL + URLEncoder.encode(query, "UTF-8");

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(20000)
                .get();

        Elements productsSel = doc.select(".adult-wrapper_adult__eCCJW.vertical_product__Q8mUI");

        int count = 0;
        for (Element item : productsSel) {
            if (count >= MAX_RESULTS) {
                break;
            }

            String productUrl = "";
            Element linkElement = item.selectFirst(".vertical_information__p_K39 a");
            if (linkElement != null) {
                productUrl = BASE_URL + linkElement.attr("href");
            }

            String imageUrl = "";
            String productName = "";

            Element imageContainer = item.selectFirst(".card-image_adult__gbuJW img");
            if (imageContainer != null) {
                imageUrl = imageContainer.attr("src");
                productName = Jsoup.parse(imageContainer.attr("alt")).text().replaceAll("\\u00AD", "").trim();
            }

            Product product = new Product();
            product.setProductURL(productUrl);
            product.setImageURL(imageUrl);
            product.setName(productName);

            products.add(product);
            count++;
        }
        return products;
    }

    /**
     * Извлекает детальную информацию о продукте (КБЖУ) со страницы продукта на сайте <a href="https://edostavka.by">https://edostavka.by</a>.
     * <p>
     *     Принимает объект {@link Product} (предполагается, что у него установлен URL страницы продукта),
     *     переходит по этому URL, парсит информацию о калориях, белках, жирах и углеводах,
     *     и обновляет переданный объект {@link Product}, добавляя в него объект {@link Nutrition}.
     * </p>
     *
     * @param product Объект {@link Product}, для которого необходимо получить КБЖУ. Должен содержать URL страницы продукта.
     * @return Копия входного объекта {@link Product} с заполненной информацией о КБЖУ ({@link Nutrition}).
     * @throws Exception Если возникает ошибка при подключении к сайту, обработке данных или если информация о КБЖУ не найдена.
     */
    @Override
    public Product parseProductDetails(@NonNull Product product) throws Exception {
        String url = product.getProductURL();

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(20000)
                .get();

        Elements containers = doc.select(".preview_short__item__yJ1oI");

        if (containers.isEmpty())
            throw new Exception("КБЖУ продукта не найдены на странице: " + url);

        Nutrition nutrition = new Nutrition();

        for (Element item : containers) {
            Element nameBlock = item.selectFirst(".preview_short__value__onntx");
            Element valueBlock = item.selectFirst(".preview_short__key__A6ql0");

            if (nameBlock == null || valueBlock == null) {
                // Можно добавить более специфичную обработку ошибки или логирование
                continue; // Пропустить этот элемент, если структура неожиданная
            }

            String nameElem = nameBlock.text();
            String valueElem = valueBlock.text();

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

        product.setNutrition(nutrition);
        return product;
    }
}