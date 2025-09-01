package MaterialCount;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CalculatorController {
    @FXML private ComboBox<String> realmComboBox;
    @FXML private ComboBox<String> layerComboBox;
    @FXML private ComboBox<String> itemTypeComboBox;
    @FXML private ComboBox<String> itemLevelComboBox;
    @FXML private TextField quantityField;
    @FXML private TextArea resultArea;

    private MaterialCalculator calculator;
    private Map<String, String> layersMap;

    public void setCalculator(MaterialCalculator calculator) {
        this.calculator = calculator;
        initializeRealms();
    }

    // 初始化世界选择
    private void initializeRealms() {
        ObservableList<String> realmNames = FXCollections.observableArrayList(calculator.getRealmNames());
        realmComboBox.setItems(realmNames);
        realmComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                initializeLayers(newVal);
            }
        });
    }

    // 初始化层级选择
    private void initializeLayers(String realmName) {
        layersMap = calculator.getLayersForRealm(realmName);
        ObservableList<String> layerNames = FXCollections.observableArrayList(layersMap.values());
        layerComboBox.setItems(layerNames);
        layerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String layerId = layersMap.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(newVal))
                        .map(Map.Entry::getKey)
                        .findFirst().orElse(null);
                if (layerId != null) {
                    initializeItems(realmName, layerId);
                }
            }
        });
    }

    // 初始化物品选择
    private Map<String, String> typeCodeMap = new HashMap<>();
    private Map<String, String> levelCodeMap = new HashMap<>();

    private void initializeItems(String realmName, String layerId) {
        // 构建英文类型码到中文名称的映射
        typeCodeMap = calculator.getItemsInLayer(realmName, layerId).stream()
            .map(item -> item.split("_")[0])
            .distinct()
            .collect(Collectors.toMap(
                code -> code,
                code -> calculator.convertItemType(code)
            ));
        // 添加缺失的特殊类型映射
        if (!typeCodeMap.containsKey("gem")) typeCodeMap.put("gem", "宝石");
        if (!typeCodeMap.containsKey("badge")) typeCodeMap.put("badge", "徽章");
        if (!typeCodeMap.containsKey("glove")) typeCodeMap.put("glove", "手套");
        if (!typeCodeMap.containsKey("necklace")) typeCodeMap.put("necklace", "项链");

        // 显示中文名称
        ObservableList<String> itemTypes = FXCollections.observableArrayList(typeCodeMap.values());
        itemTypeComboBox.setItems(itemTypes);
        itemTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // 查找对应的英文类型码
                String englishType = typeCodeMap.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(newVal))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);
                if (englishType != null) {
                    initializeLevels(realmName, layerId, englishType);
                }
            }
        });
    }

    private void initializeLevels(String realmName, String layerId, String itemType) {
        levelCodeMap.clear(); // 清空之前的映射
        
        // 获取当前层级中实际存在的等级
        Set<String> existingLevels = calculator.getItemsInLayer(realmName, layerId).stream()
                .filter(item -> item.startsWith(itemType))
                .map(item -> {
                    if (item.contains("_")) {
                        return item.split("_", 2)[1];
                    }
                    return "";
                })
                .filter(level -> !level.isEmpty())
                .collect(Collectors.toSet());
        
        // 创建ObservableList存储所有等级选项
        ObservableList<String> levels = FXCollections.observableArrayList();
        
        // 根据物品类型添加所有可能的等级选项
        if ("weapon".equals(itemType)) {
            // 武器类型应包含所有等级选项：下、中、上、極、極+
            addLevelIfAvailable(levels, "low", "下", existingLevels);
            addLevelIfAvailable(levels, "mid", "中", existingLevels);
            addLevelIfAvailable(levels, "high", "上", existingLevels);
            addLevelIfAvailable(levels, "premium", "極", existingLevels);
            addLevelIfAvailable(levels, "extreme_plus", "極+", existingLevels);
        } else if ("equipment".equals(itemType)) {
            // 装备类型应包含所有等级选项：下、中、上、極、極+
            addLevelIfAvailable(levels, "low", "下", existingLevels);
            addLevelIfAvailable(levels, "mid", "中", existingLevels);
            addLevelIfAvailable(levels, "high", "上", existingLevels);
            addLevelIfAvailable(levels, "premium", "極", existingLevels);
            addLevelIfAvailable(levels, "extreme_plus", "極+", existingLevels);
        } else {
            // 其他物品类型保持原有逻辑
            calculator.getItemsInLayer(realmName, layerId).stream()
                .filter(item -> item.startsWith(itemType))
                .map(item -> {
                    String levelPart = "";
                    if (item.contains("_")) {
                        levelPart = item.split("_", 2)[1];
                    }
                    
                    String converted;
                    if (levelPart.isEmpty()) {
                        converted = "";
                    } else {
                        converted = calculator.convertLevel(levelPart);
                        levelCodeMap.put(converted, levelPart);
                    }
                    
                    if (converted == null || converted.isEmpty()) {
                        converted = levelPart;
                    }
                    
                    if (converted == null || converted.isEmpty()) {
                        System.err.println("無法轉換等級: 物品='" + item + "', 提取部分='" + levelPart + "'");
                    }
                    return converted;
                })
                .filter(level -> level != null && !level.isEmpty())
                .distinct()
                .forEach(levels::add);
        }
        
        itemLevelComboBox.setItems(levels);

        // 为宝石、徽章、手套和项链添加默认等级显示
        if (Arrays.asList("gem", "badge", "glove", "necklace").contains(itemType)) {
            if (levels.isEmpty()) {
                levels.add("默認");
                levelCodeMap.put("默認", ""); // 将"默認"映射为空字符串以生成基础类型键
            }
            itemLevelComboBox.getSelectionModel().select(0);
        }
        if (Arrays.asList("gem", "glove", "necklace", "badge").contains(itemType) && !levels.isEmpty()) {
            itemLevelComboBox.getSelectionModel().select(0);
        }

        // 为武器和装备保留默认等级选择
        if (("weapon".equals(itemType) || "equipment".equals(itemType)) && !levels.isEmpty()) {
            itemLevelComboBox.getSelectionModel().select(0);
        }
    }
    
    // 辅助方法：添加等级到列表并维护映射
    private void addLevelIfAvailable(ObservableList<String> levels, String code, String displayName, Set<String> existingLevels) {
        if (existingLevels.contains(code)) {
            levelCodeMap.put(displayName, code);
            levels.add(displayName);
        }
    }

    // 计算按钮点击事件
    @FXML
    private void calculate() {
        try {
            String realmName = realmComboBox.getValue();
            String layerName = layerComboBox.getValue();
            // 获取原始物品类型编码
            String chineseType = itemTypeComboBox.getValue();
            String originalType = typeCodeMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(chineseType))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);

            String levelDisplayName = itemLevelComboBox.getValue();
            String originalLevel = levelCodeMap.get(levelDisplayName);
            if (originalLevel == null) {
                originalLevel = levelDisplayName;
            }

            if (originalType == null || originalLevel == null) {
                resultArea.setText("请选择物品类型和等级");
                return;
            }
            // 当等级为空时生成基础类型键，否则添加等级后缀
            String itemKey = originalLevel.isEmpty() ? originalType : originalType + "_" + originalLevel;
            int quantity = Integer.parseInt(quantityField.getText());

            if (realmName == null || layerName == null || itemKey == null || quantity <= 0) {
                resultArea.setText("请填写所有必要信息并确保数量为正数");
                return;
            }

            // 获取选中的层级ID
            String layerId = layersMap.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(layerName))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);
            System.err.println("生成的物品键: " + itemKey + ", 层级ID: " + layerId);

            if (layerId == null) {
                resultArea.setText("无法找到选中的层级");
                return;
            }

            // 执行计算
            CalculationResult result = calculator.calculate(realmName, layerId, itemKey, quantity);
            displayResult(result);

        } catch (NumberFormatException e) {
            resultArea.setText("请输入有效的数量");
        } catch (Exception e) {
            resultArea.setText("计算出错: " + e.getMessage());
        }
    }

    // 显示计算结果
    private void displayResult(CalculationResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("===== 計算結果 =====\n");
        sb.append("世界: ").append(result.getRealmName()).append("\n");
        sb.append("層級: ").append(result.getLayerName()).append("\n");
        sb.append("物品: ").append(result.getItemName()).append("\n");
        sb.append("製作數量: ").append(result.getQuantity()).append("\n\n");

        sb.append("----- 材料來源 -----\n");
        result.getSources().forEach(source -> {
            sb.append("怪物: " ).append(source.getMonsterName()).append("\n");
            sb.append("怪物位置: ").append(source.getPosition()).append("(小怪和精英小怪出現的位置有時候不止一個，請注意)\n所需數量: " ).append(source.getQuantity()).append("\n");
            // 如果需要精炼，在材料来源下方显示精炼成本
            if (result.getRefineMaterialPerTime() != null && !result.getRefineMaterialPerTime().isEmpty()) {
                sb.append("精煉貨幣成本: ").append(result.getPerRefinementCurrencyCost()).append("\n");
            }
        });

        sb.append("\n----- 最終共計需要材料 -----\n");
        sb.append("所需未精煉材料總量: ").append(result.getUnrefinedTotal()).append("\n");
        if (result.getRefineMaterialPerTime() != null) {
            sb.append("已精煉材料: ").append(result.getRefineMaterialPerTime()).append("\n");
            sb.append("精煉貨幣成本: ").append(result.getCurrencyCost()).append("\n");
        }

        resultArea.setText(sb.toString());
    }
}