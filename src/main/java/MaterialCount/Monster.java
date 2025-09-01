package MaterialCount;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Monster {
    private Map<String, String> drops;
    private String position;

    @SerializedName("currency_cost")
    private CurrencyCost currencyCost;

    @SerializedName("16g")
    private boolean is16g;

    // Getters and setters
    public Map<String, String> getDrops() { return drops; }
    public void setDrops(Map<String, String> drops) { this.drops = drops; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public CurrencyCost getCurrencyCost() { return currencyCost; }
    public void setCurrencyCost(CurrencyCost currencyCost) { this.currencyCost = currencyCost; }
    public boolean is16g() { return is16g; }
    public void set16g(boolean is16g) { this.is16g = is16g; }
}