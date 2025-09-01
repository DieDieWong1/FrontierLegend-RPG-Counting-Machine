package MaterialCount;

public class MaterialSource {
    private String monsterName;
    private String position;
    private int quantity;

    public MaterialSource(String monsterName, String position, int quantity) {
        this.monsterName = monsterName;
        this.position = position;
        this.quantity = quantity;
    }

    // Getters
    public String getMonsterName() { return monsterName; }
    public String getPosition() { return position; }
    public int getQuantity() { return quantity; }
}