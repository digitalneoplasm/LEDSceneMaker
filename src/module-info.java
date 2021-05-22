module LEDSceneMaker {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.xml;

    opens LEDMapper to javafx.fxml;

    exports LEDMapper;
    exports LEDMapper.state;
    opens LEDMapper.state to javafx.fxml;
}