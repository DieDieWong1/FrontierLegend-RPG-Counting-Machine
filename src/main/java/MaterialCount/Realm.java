package MaterialCount;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

import java.util.HashMap;

public class Realm {
    @SerializedName("realm_name")
    private String realmName;
    private Map<String, Layer> layers = new HashMap<>();

    // Default constructor for Gson
    public Realm() {}

    // Getters and setters with validation
    public String getRealmName() { return realmName; }
    public void setRealmName(String realmName) {
        if (realmName == null || realmName.trim().isEmpty()) {
            throw new IllegalArgumentException("Realm name cannot be empty");
        }
        this.realmName = realmName;
    }
    public Map<String, Layer> getLayers() { return layers; }
    public void setLayers(Map<String, Layer> layers) {
        this.layers = layers != null ? layers : new HashMap<>();
    }
}