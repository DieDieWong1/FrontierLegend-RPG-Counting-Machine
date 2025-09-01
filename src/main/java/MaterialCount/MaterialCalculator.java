package MaterialCount;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MaterialCalculator {
    private final Map<String, Realm> realms;

    public MaterialCalculator(Map<String, Realm> realms) {
        this.realms = realms;
    }

    // 获取所有可用世界名称
    public List<String> getRealmNames() {
        return new ArrayList<>(realms.keySet());
    }

    // 获取指定世界的所有层级（格式化显示名）
    public Map<String, String> getLayersForRealm(String realmName) {
        Realm realm = realms.get(realmName);
        if (realm == null) return Collections.emptyMap();
        return realm.getLayers().keySet().stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> key.replace("layer_", "") + "c"
                ));
    }

    // 获取指定世界层级的所有可用物品
    public List<String> getItemsInLayer(String realmName, String layerId) {
        Realm realm = realms.get(realmName);
        if (realm == null) return Collections.emptyList();
        Layer layer = realm.getLayers().get(layerId);
        if (layer == null) return Collections.emptyList();

        Set<String> items = new HashSet<>();
        addItemsFromMonsters(layer.getSmallMonsters(), items);
        addItemsFromMonsters(layer.getEliteSmallMonsters(), items);
        addItemsFromMonsters(layer.getEliteMonsters(), items);
        addItemsFromMonsters(layer.getBoss(), items);
        List<String> filteredItems = new ArrayList<>(items); // 移除特殊类型过滤，保留所有等级
        return filteredItems;
    }

    private void addItemsFromMonsters(Map<String, Monster> monsters, Set<String> items) {
        if (monsters == null) return;
        for (Monster monster : monsters.values()) {
            if (monster.getDrops() != null) {
                items.addAll(monster.getDrops().keySet());
            }
        }
    }

    // 核心计算方法
    public CalculationResult calculate(String realmName, String layerId, String itemKey, int quantity) {
        Realm realm = realms.get(realmName);
        Layer layer = realm.getLayers().get(layerId);
        if (layer == null) throw new IllegalArgumentException("无效层级: " + layerId);

        // 收集所有掉落目标物品的怪物
        List<MonsterInfo> sources = collectMonsterSources(layer, itemKey);
        if (sources.isEmpty()) throw new IllegalArgumentException("未找到掉落物品的怪物: " + itemKey);

        // 解析物品类型和等级
        ItemInfo itemInfo = parseItemKey(itemKey);
        // 解析所有来源的掉落信息并汇总
        int totalDropQuantity = 0;
        boolean needsRefinement = false;
        for (MonsterInfo source : sources) {
            DropInfo di = parseDropInfo(source.getMonster().getDrops().get(itemKey));
            totalDropQuantity += di.quantity;
            needsRefinement |= di.needsRefinement;
        }
        DropInfo dropInfo = new DropInfo(totalDropQuantity, needsRefinement);
        // 计算总未精炼材料
        int totalUnrefined = quantity * dropInfo.quantity;
        // 获取堆叠规则（16g标记）
        boolean is16g = sources.stream().anyMatch(m -> m.getMonster().is16g());
        // 计算堆叠信息
        StackInfo stackInfo = calculateStacks(totalUnrefined, is16g);

        // 构建计算结果
        CalculationResult result = new CalculationResult();
        result.setRealmName(realm.getRealmName());
        result.setLayerName(layerId.replace("layer_", "") + "c");
        result.setItemName(itemInfo.type + " " + itemInfo.level);
        result.setQuantity(quantity);
        result.setSources(sources.stream()
                .map(m -> {
                    DropInfo di = parseDropInfo(m.getMonster().getDrops().get(itemKey));
                    return new MaterialSource(m.getMonsterName(), m.getMonster().getPosition(), di.quantity);
                })
                .collect(Collectors.toList()));
        result.setUnrefinedTotal(stackInfo.toString() + (dropInfo.needsRefinement ? " (需精炼)" : " (不用精炼)"));

        // 处理精炼成本
        if (dropInfo.needsRefinement) {
            handleRefinementCost(result, sources, totalUnrefined, dropInfo.quantity);
        }

        return result;
    }

    // 收集掉落目标物品的怪物信息
    private List<MonsterInfo> collectMonsterSources(Layer layer, String itemKey) {
        List<MonsterInfo> sources = new ArrayList<>();
        addMonstersFromMap(layer.getSmallMonsters(), "small_monsters", itemKey, sources);
        addMonstersFromMap(layer.getEliteSmallMonsters(), "elite_small_monsters", itemKey, sources);
        addMonstersFromMap(layer.getEliteMonsters(), "elite_monsters", itemKey, sources);
        addMonstersFromMap(layer.getBoss(), "boss", itemKey, sources);
        return sources;
    }

    private void addMonstersFromMap(Map<String, Monster> monsters, String category, String itemKey, List<MonsterInfo> sources) {
        if (monsters == null) return;
        for (Map.Entry<String, Monster> entry : monsters.entrySet()) {
            Monster monster = entry.getValue();
            if (monster.getDrops() != null && monster.getDrops().containsKey(itemKey)) {
                sources.add(new MonsterInfo(entry.getKey(), monster));
            }
        }
    }

    // 解析物品键（如weapon_low -> 武器 下）
    private ItemInfo parseItemKey(String itemKey) {
        String[] parts = itemKey.split("_");
        String type = convertItemType(parts[0]);
        String level = parts.length > 1 ? convertLevel(parts[1]) : "";
        // 特殊物品处理（宝石/手套/项链/徽章默认显示等级）
        if (Arrays.asList("寶石", "手套", "項鏈", "徽章").contains(type)) {
            return new ItemInfo(type, "");
        }
        return new ItemInfo(type, level);
    }

    // 物品类型转换
    public String convertItemType(String typeCode) {
        return switch (typeCode) {
            case "weapon" -> "武器";
            case "equipment" -> "裝備";
            case "gem" -> "寶石";
            case "glove" -> "手套";
            case "necklace" -> "項鏈";
            case "badge" -> "徽章";
            default -> typeCode;
        };
    }

    // 货币名称转换为繁体中文
    public String convertCurrencyName(String currencyCode) {
        return switch (currencyCode) {
            case "silver" -> "銀元";
            case "copper" -> "銅幣";
            case "gold" -> "金元";
            case "dream_coin" -> "夢元";
            default -> currencyCode;
        };
    }

    // 等级转换
    public String convertLevel(String levelCode) {
        return switch (levelCode) {
            case "low" -> "下";
            case "mid" -> "中";
            case "high" -> "上";
            case "premium" -> "極";
            case "extreme_plus" -> "極+";
            default -> levelCode;
        };
    }

    // 解析掉落信息（数量和精炼需求）
    private DropInfo parseDropInfo(String dropValue) {
        boolean needsRefinement = !dropValue.endsWith("N");
        int quantity = Integer.parseInt(dropValue.replace("N", ""));
        return new DropInfo(quantity, needsRefinement);
    }

    // 计算堆叠信息（组和剩余数量）
    private StackInfo calculateStacks(int totalQuantity, boolean is16g) {
        int groupSize = is16g ? 16 : 64;
        int groups = totalQuantity / groupSize;
        int remaining = totalQuantity % groupSize;
        return new StackInfo(groups, remaining);
    }

    // 处理精炼成本计算
    private void handleRefinementCost(CalculationResult result, List<MonsterInfo> sources, int totalUnrefined, int perItemQuantity) {
        // 汇总所有怪物的精炼成本
        int totalRefineMaterial = 0;
        Map<String, Integer> totalCurrency = new HashMap<>();
        for (MonsterInfo source : sources) {
            CurrencyCost cost = source.getMonster().getCurrencyCost();
            if (cost == null) continue;
            totalRefineMaterial += cost.getRefineCostMaterial();
            cost.getRefineCostCurrency().forEach((k, v) -> 
                totalCurrency.put(k, totalCurrency.getOrDefault(k, 0) + v)
            );
        }
        if (totalRefineMaterial == 0 && totalCurrency.isEmpty()) return;

        int refineCount = totalUnrefined / totalRefineMaterial;
        Map<String, Integer> currency = totalCurrency;

        // 计算精炼材料需求
        boolean is16g = sources.stream().anyMatch(m -> m.getMonster().is16g());
        StackInfo refineStack = calculateStacks(refineCount, is16g);
        result.setRefineMaterialPerTime(refineStack.toString());

        // 计算单次精炼货币成本
        StringBuilder perRefinementCurrencySummary = new StringBuilder();
        totalCurrency.forEach((type, amount) -> {
            if (perRefinementCurrencySummary.length() > 0) {
                perRefinementCurrencySummary.append(", ");
            }
            perRefinementCurrencySummary.append(convertCurrencyName(type)).append(": " ).append(amount);
        });
        result.setPerRefinementCurrencyCost(perRefinementCurrencySummary.toString());
        
        // 计算总货币成本
        StringBuilder currencySummary = new StringBuilder();
        currency.forEach((type, amount) -> {
            int total = amount * refineCount; // 乘以精炼次数，随数量变化
            if (currencySummary.length() > 0) {
                currencySummary.append(", ");
            }
            currencySummary.append(convertCurrencyName(type)).append(": " ).append(total);
        });
        result.setCurrencyCost(currencySummary.toString());
    }

    // 内部数据类：物品信息
    private static class ItemInfo {
        String type;
        String level;

        ItemInfo(String type, String level) {
            this.type = type;
            this.level = level;
        }
    }

    // 内部数据类：掉落信息
    private static class DropInfo {
        int quantity;
        boolean needsRefinement;

        DropInfo(int quantity, boolean needsRefinement) {
            this.quantity = quantity;
            this.needsRefinement = needsRefinement;
        }
    }

    // 内部数据类：堆叠信息
    private static class StackInfo {
        int groups;
        int remaining;

        StackInfo(int groups, int remaining) {
            this.groups = groups;
            this.remaining = remaining;
        }

        @Override
        public String toString() {
            return groups + "组+" + remaining + "个";
        }
    }

    // 内部数据类：怪物信息
    private static class MonsterInfo {
        String monsterName;
        Monster monster;

        MonsterInfo(String monsterName, Monster monster) {
            this.monsterName = monsterName;
            this.monster = monster;
        }

        String getMonsterName() { return monsterName; }
        Monster getMonster() { return monster; }
    }
}