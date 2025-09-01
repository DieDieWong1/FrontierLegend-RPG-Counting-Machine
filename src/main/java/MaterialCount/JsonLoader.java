package MaterialCount;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.net.URL;

public class JsonLoader {
    private static final Gson gson = new Gson();

    public static Realm loadRealm(String fileName) {
        try {
            // 尝试多种路径格式加载资源
            InputStream inputStream = null;
            URL resourceUrl = null;
            
            // 尝试直接使用文件名
            resourceUrl = JsonLoader.class.getResource(fileName);
            if (resourceUrl != null) {
                inputStream = resourceUrl.openStream();
                System.out.println("成功找到资源: " + resourceUrl.getPath());
            }
            
            // 尝试在MaterialCount包中查找
            if (inputStream == null) {
                resourceUrl = JsonLoader.class.getResource("/MaterialCount" + (fileName.startsWith("/") ? "" : "/") + fileName);
                if (resourceUrl != null) {
                    inputStream = resourceUrl.openStream();
                    System.out.println("成功找到资源: " + resourceUrl.getPath());
                }
            }
            
            // 尝试在根目录查找
            if (inputStream == null) {
                resourceUrl = JsonLoader.class.getResource("/" + fileName);
                if (resourceUrl != null) {
                    inputStream = resourceUrl.openStream();
                    System.out.println("成功找到资源: " + resourceUrl.getPath());
                }
            }
            
            if (inputStream == null) {
                throw new RuntimeException("无法找到资源文件: " + fileName + ". 请检查文件是否存在于正确位置。");
            }
            
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                return gson.fromJson(reader, Realm.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load realm data from " + fileName + ": " + e.getMessage());
        }
    }
}