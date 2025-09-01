package MaterialCount;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Layer {
    @SerializedName("small_monsters")
    private Map<String, Monster> smallMonsters;

    @SerializedName("elite_small_monsters")
    private Map<String, Monster> eliteSmallMonsters;

    @SerializedName("elite_monsters")
    private Map<String, Monster> eliteMonsters;

    private Map<String, Monster> boss;

    // Getters and setters
    public Map<String, Monster> getSmallMonsters() { return smallMonsters; }
    public void setSmallMonsters(Map<String, Monster> smallMonsters) { this.smallMonsters = smallMonsters; }
    public Map<String, Monster> getEliteSmallMonsters() { return eliteSmallMonsters; }
    public void setEliteSmallMonsters(Map<String, Monster> eliteSmallMonsters) { this.eliteSmallMonsters = eliteSmallMonsters; }
    public Map<String, Monster> getEliteMonsters() { return eliteMonsters; }
    public void setEliteMonsters(Map<String, Monster> eliteMonsters) { this.eliteMonsters = eliteMonsters; }
    public Map<String, Monster> getBoss() { return boss; }
    public void setBoss(Map<String, Monster> boss) { this.boss = boss; }
}