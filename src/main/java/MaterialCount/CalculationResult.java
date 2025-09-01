package MaterialCount;

import java.util.List;

public class CalculationResult {
    private String realmName;
    private String layerName;
    private String itemName;
    private int quantity;
    private List<MaterialSource> sources;
    private String unrefinedTotal;
    private String refineMaterialPerTime;
    private String currencyCost;
    private String perRefinementCurrencyCost;

    // Getters and setters
    public String getRealmName() { return realmName; }
    public void setRealmName(String realmName) { this.realmName = realmName; }
    public String getLayerName() { return layerName; }
    public void setLayerName(String layerName) { this.layerName = layerName; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public List<MaterialSource> getSources() { return sources; }
    public void setSources(List<MaterialSource> sources) { this.sources = sources; }
    public String getUnrefinedTotal() { return unrefinedTotal; }
    public void setUnrefinedTotal(String unrefinedTotal) { this.unrefinedTotal = unrefinedTotal; }
    public String getRefineMaterialPerTime() { return refineMaterialPerTime; }
    public void setRefineMaterialPerTime(String refineMaterialPerTime) { this.refineMaterialPerTime = refineMaterialPerTime; }
    public String getCurrencyCost() {
        return currencyCost;
    }

    public void setCurrencyCost(String currencyCost) {
        this.currencyCost = currencyCost;
    }

    public String getPerRefinementCurrencyCost() {
        return perRefinementCurrencyCost;
    }

    public void setPerRefinementCurrencyCost(String perRefinementCurrencyCost) {
        this.perRefinementCurrencyCost = perRefinementCurrencyCost;
    }
}