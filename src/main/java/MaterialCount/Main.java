package MaterialCount;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Map<String, Realm> realms = new HashMap<>();
    private static MaterialCalculator calculator;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 加载所有世界数据
        loadRealmData();
        calculator = new MaterialCalculator(realms);

        // 加载FXML界面
        FXMLLoader loader = new FXMLLoader(getClass().getResource("calculator.fxml"));
        Parent root = loader.load();

        // 设置控制器并传递计算器实例
        CalculatorController controller = loader.getController();
        controller.setCalculator(calculator);

        // 配置舞台
        primaryStage.setTitle("材料计算器");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    // 加载所有世界数据
    private void loadRealmData() {
        try {
            // 加载所有世界数据
            String[] realmFiles = {"/dream.json", "/abyss.json", "/fairy.json", "/main_world.json", "/P&H.json"};
            for (String file : realmFiles) {
                Realm realm = JsonLoader.loadRealm(file);
                realms.put(realm.getRealmName(), realm);
            }
        } catch (Exception e) {
            System.err.println("数据加载失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}