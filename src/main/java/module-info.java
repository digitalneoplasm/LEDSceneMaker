module LEDSceneMaker {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.xml;

    opens LEDSceneMaker to javafx.fxml;

    exports LEDSceneMaker;
    exports LEDSceneMaker.state;
    opens LEDSceneMaker.state to javafx.fxml;
}