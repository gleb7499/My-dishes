package com.mydishes.mydishes.Parser;

import static com.mydishes.mydishes.utils.ViewUtils.parseFloatSafe;

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

public class EdostavkaParser extends Parser {

    private static final String BASE_URL = "https://edostavka.by";
    private static final String SEARCH_URL = BASE_URL + "/search?query=";


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

            Product product = new Product();
            product.setProductURL(productUrl);
            product.setImageURL(imageUrl);
            product.setName(productName);

            products.add(product);

            count++;
        }

        return products;
    }


    @Override
    public Product parseProductDetails(Product product) throws IOException {
        String url = product.getProductURL();

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(20000)
                .get();

        Elements containers = doc.select(".preview_short__item__yJ1oI");

        if (containers.isEmpty()) throw new IOException("КБЖУ продукта не найдены!");

        Nutrition nutrition = new Nutrition();

        for (var item : containers) {
            Element nameBlock = item.selectFirst(".preview_short__value__onntx");
            Element valueBlock = item.selectFirst(".preview_short__key__A6ql0");

            if (nameBlock == null || valueBlock == null)
                throw new IOException("Ошибка! Попробуйте еще раз");

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