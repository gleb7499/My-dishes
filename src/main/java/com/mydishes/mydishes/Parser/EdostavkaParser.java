package com.mydishes.mydishes.Parser;

import com.mydishes.mydishes.Models.Dishes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EdostavkaParser {

    private static final String BASE_URL = "https://edostavka.by";
    private static final String SEARCH_URL = BASE_URL + "/search?query=";

    private static final List<Dishes> dishesList = new ArrayList<>();
    public static final int MAX_RESULTS = 25; // Лимит на количество объектов

    /**
     * Основной метод парсинга
     *
     * @param query поисковый запрос
     */
    public static void find(String query) {

        dishesList.clear();

        try {
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

                Dishes dish = new Dishes();
                dish.setUrl(productUrl);
                dish.setImage(imageUrl);
                dish.setName(productName);

                dishesList.add(dish);

                count++;
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }


    /**
     * Геттер для получения списка после завершения парсинга
     */
    public static List<Dishes> getDishesList() {
        return new ArrayList<>(dishesList); // Защищаем от внешнего вмешательства
    }
}

