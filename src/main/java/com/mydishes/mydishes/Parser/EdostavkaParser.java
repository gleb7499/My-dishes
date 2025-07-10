package com.mydishes.mydishes.Parser;

import static com.mydishes.mydishes.utils.ViewUtils.parseFloatSafe;

import com.mydishes.mydishes.Models.ProductsManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;

public class EdostavkaParser extends Parser {

    private static final String BASE_URL = "https://edostavka.by";
    private static final String SEARCH_URL = BASE_URL + "/search?query=";

    /**
     * Метод парсинга для получения списка продуктов путем поиска
     *
     * @param query поисковый запрос
     */
    @Override
    public void findProducts(String query) throws IOException {

        ProductsManager.clear();


        String url = SEARCH_URL + URLEncoder.encode(query, "UTF-8");

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(20000)
                .get();

        Elements products = doc.select(".adult-wrapper_adult__eCCJW.vertical_product__Q8mUI");

        int count = 0;

        for (Element item : products) {

            if (count >= MAX_RESULTS) {
                break; // Достигли лимита, выходим из цикла
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

            ProductsManager.Product product = new ProductsManager.Product();
            product.setProductURL(productUrl);
            product.setImageURL(imageUrl);
            product.setName(productName);

            ProductsManager.add(product);

            count++;
        }
    }

    /**
     * Метод для парсинга страницы конкретного продукта и заполнения его КБЖУ.
     *
     * @param product объект продукта, содержащий URL
     * @return обновлённый продукт с заполненными значениями КБЖУ
     */
    @Override
    public ProductsManager.Product parseProductDetails(ProductsManager.Product product) throws IOException {
        String url = product.getProductURL();

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(20000)
                .get();

        Elements containers = doc.select(".preview_short__item__yJ1oI");

        if (containers.isEmpty()) throw new IOException("КБЖУ продукта не найдены!");

        for (var item : containers) {
            Element nameBlock = item.selectFirst(".preview_short__value__onntx");
            Element valueBlock = item.selectFirst(".preview_short__key__A6ql0");

            if (nameBlock == null || valueBlock == null)
                throw new IOException("Ошибка! Попробуйте еще раз");

            String nameElem = nameBlock.text();
            String valueElem = valueBlock.text();
            switch (nameElem) {
                case "Энергетическая ценность":
                    product.setCalories(parseFloatSafe(valueElem.split(" ")[0]));
                    break;
                case "Белки":
                    product.setProtein(parseFloatSafe(valueElem));
                    break;
                case "Жиры":
                    product.setFat(parseFloatSafe(valueElem));
                    break;
                case "Углеводы":
                    product.setCarb(parseFloatSafe(valueElem));
                    break;
            }
        }

        return product;
    }
}