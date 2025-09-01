package MaterialCount;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class CurrencyCost {
    @SerializedName("refine_cost_material")
    private int refineCostMaterial;

    @SerializedName("refine_cost_currency")
    private Map<String, Integer> refineCostCurrency;

    // Getters and setters
    public int getRefineCostMaterial() { return refineCostMaterial; }
    public void setRefineCostMaterial(int refineCostMaterial) { this.refineCostMaterial = refineCostMaterial; }
    public Map<String, Integer> getRefineCostCurrency() { return refineCostCurrency; }
    public void setRefineCostCurrency(Map<String, Integer> refineCostCurrency) { this.refineCostCurrency = refineCostCurrency; }
}