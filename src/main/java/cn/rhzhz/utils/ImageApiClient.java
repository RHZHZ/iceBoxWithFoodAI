package cn.rhzhz.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
@Component
public class ImageApiClient {

    private static String baseUrl = "https://cn.apihz.cn/api/img/apihzimgbaidu.php?id=88888888&key=88888888&limit=1&page=1&words=";

//    @Test
//    public void main() {
//        String apiUrl = "https://cn.apihz.cn/api/img/apihzimgbaidu.php?"
//                + "id=88888888&key=88888888&limit=10&page=1&words=milk";
//        System.out.println(baseUrl);
//        try {
//            String imageUrls = getImageUrls("milk");
//            System.out.println(imageUrls);
//        } catch (Exception e) {
//
//            e.printStackTrace();
//        }
//    }

    public String getImageUrls(String text) throws Exception {
        List<String> result = new ArrayList<>();

        String apiUrl = baseUrl;
        apiUrl+=text;
        // 1. 创建HTTP连接
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // 2. 读取响应
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // 3. 解析JSON
            JSONObject json = new JSONObject(response.toString());
            if (json.getInt("code") == 200) {
                JSONArray resArray = json.getJSONArray("res");
                for (int i = 0; i < resArray.length(); i++) {
                    result.add(resArray.getString(i));
                }
            }
        } finally {
            conn.disconnect();
        }
        String results = result.get(0);

        return results;
    }
}