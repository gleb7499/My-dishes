package com.mydishes.mydishes.Parser;

import com.mydishes.mydishes.Models.ProductsManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;

public class EdostavkaParser {

    private static final String BASE_URL = "https://edostavka.by";
    private static final String SEARCH_URL = BASE_URL + "/search?query=";
    public static final int MAX_RESULTS = 25; // Лимит на количество объектов

    /**
     * Основной метод парсинга
     *
     * @param query поисковый запрос
     */
    public static void find(String query) throws IOException {

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
}