package com.brianlindsey.SlackNewsletter.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class GiphyService {

    public static final String GIPHY_BASE = "http://api.giphy.com/v1/stickers/search?q=";
    public static final String KEY = "&api_key=zFszggpH9v53DnAC5fgmNBd2T9MW6ozC";
    private static RestTemplate rest = new RestTemplate();

    public static String getGiphyUrl(String searchTerm) {
        Gson gson = new Gson();
        String giphy = GIPHY_BASE + searchTerm + KEY;
        try {
            URI uri = UriComponentsBuilder.fromUriString(giphy).build().encode().toUri();
            MultiValueMap<String, String> mvm = new LinkedMultiValueMap<String, String>();
            ResponseEntity<String> res = rest.exchange(uri, HttpMethod.GET, new HttpEntity(mvm, null), String.class);

            JsonObject json = gson.fromJson(res.getBody(), JsonObject.class);
            JsonArray data = json.getAsJsonArray("data");
            if (data == null || data.size() == 0) {
                return "";
            }
            JsonPrimitive bitly = ((JsonObject) data.get(0)).getAsJsonPrimitive("bitly_gif_url");
           return bitly.getAsString();

        } catch (Throwable e) {
            return "";
        }

    }
}
