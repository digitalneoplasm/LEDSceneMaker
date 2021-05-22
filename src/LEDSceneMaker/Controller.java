package LEDSceneMaker;

import LEDSceneMaker.state.Frame;
import LEDSceneMaker.components.LED_ws2812b;
import LEDSceneMaker.io.ProjectReader;
import LEDSceneMaker.io.ProjectWriter;
import LEDSceneMaker.io.export.Export;
import LEDSceneMaker.io.export.ExportAdafruit;
import LEDSceneMaker.io.export.ExportFastLED;
import LEDSceneMaker.state.Model;
import LEDSceneMaker.ui.LEDCircle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static LEDSceneMaker.io.PCBParser.parse;

public class Controller {
    public ScrollPane frameListScollPane;
    public ToggleButton clearButton;
    public ImageView clearButtonImage;
    public MenuItem duplicateFrameMenuOption;
    public MenuItem deleteFrameMenuOption;
    @FXML
    MenuBar mainMenu;
    @FXML
    Pane drawingPane;
    @FXML
    Group zoomGroup;
    @FXML
    ColorPicker colorPicker;
    @FXML
    ListView<Frame> frameList;
    @FXML
    TreeView<String> regionsTree;

    Scale scaleTransform;
    Robot robot = new Robot();
    ObservableList<Frame> frames = Model.getInstance().getFrames();
    FileChooser fileChooser = new FileChooser();

    void setupControls(){
        drawingPane.setStyle("-fx-background-color: white;");
        scaleTransform = new Scale(2, 2, 0, 0);
        zoomGroup.getTransforms().add(scaleTransform);
        drawingPane.setOnDragDetected(e -> drawingPane.startFullDrag());

        try {
            clearButtonImage.setImage(new Image(getClass().getResource("/icons8-eraser-80.png").openStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        frameListScollPane.setFitToHeight(true);
        frameListScollPane.setFitToWidth(true);
        frameList.setCellFactory(p -> new ListCell<Frame>() {
            @Override
            public void updateItem(Frame frame, boolean empty) {
                super.updateItem(frame, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    setText(frame.toString() + " " + (1 + getIndex()));
                }
            }
        });
        frameList.setItems(frames);
    }


    /// Canvas Drawing ///
    public void initializeLEDCircle(LEDCircle c) {
        c.setOnMouseDragEntered(e -> {
            if (clearButton.isSelected())
                Model.getInstance().getCurrentFrame().clearCommand(c);
            else
                Model.getInstance().getCurrentFrame().changeColorCommand(c, colorPicker.getValue());
        });
        c.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (clearButton.isSelected())
                Model.getInstance().getCurrentFrame().clearCommand(c);
            else
                Model.getInstance().getCurrentFrame().changeColorCommand(c, colorPicker.getValue());
        });
    }

    public LEDCircle createLEDCircle(LED_ws2812b led){
        LEDCircle c = new LEDCircle(2.5);
        initializeLEDCircle(c);
        c.relocate(led.getX().doubleValue(), led.getY().doubleValue());
        return c;
    }

    public LEDCircle createLEDCircle(LED_ws2812b led, Paint paint){
        LEDCircle c = new LEDCircle(2.5, paint);
        initializeLEDCircle(c);
        c.relocate(led.getX().doubleValue(), led.getY().doubleValue());
        return c;
    }

    public Frame createNewFrame(boolean keepColors){
        Map<LED_ws2812b, LEDCircle> circleMap = Model.getInstance().getCurrentFrame().getCircleMap();
        Frame newFrame = Model.getInstance().newFrame();

        for (LED_ws2812b led : circleMap.keySet()){
            if (keepColors)
                newFrame.addCircleMapping(createLEDCircle(led, circleMap.get(led).getFill()), led);
            else
                newFrame.addCircleMapping(createLEDCircle(led), led);
        }

        return newFrame;
    }

    public Frame createNewFrame() {
        return createNewFrame(false);
    }

    private void drawLEDs(List<LED_ws2812b> leds){
        for(LED_ws2812b led : leds){
            LEDCircle c = createLEDCircle(led);
            Model.getInstance().getCurrentFrame().addCircleMapping(c, led);
            drawingPane.getChildren().add(c);
        }
    }

    /// Frames ///

    private void displayFrame(Frame frame){
        if (frame == null) return;
        drawingPane.getChildren().clear();
        for (Circle c : frame.getCircleMap().values()){
            drawingPane.getChildren().add(c);
        }
    }

    private void generateFrameImage(){

        //WritableImage imgReturn = robot.getScreenCapture(imgOut, new Rectangle2D());
        // (you can use Platform.runLater() to ensure this if it’s invoked from another thread).
    }

    /// Handlers ///
    @FXML
    private void openPCBFile(ActionEvent event){
        fileChooser.setTitle("Open PCB File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Kicad PCB", "*.kicad_pcb"));
        File pcbfile = fileChooser.showOpenDialog(mainMenu.getScene().getWindow());

        if (pcbfile != null) {
            try {
                List<LED_ws2812b> leds = parse(pcbfile);
                Model.getInstance().setPcbFilename(pcbfile.getAbsolutePath());
                Model.getInstance().setLEDs(leds);
                drawLEDs(leds);
            } catch (IOException e) {
            }
        }

        frameList.getSelectionModel().selectFirst();
    }

    @FXML
    private void saveProject(ActionEvent event){
        fileChooser.setTitle("Save Project");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("LED Project", "*.ledproj"));
        File projectfile = fileChooser.showSaveDialog(mainMenu.getScene().getWindow());

        if (projectfile != null) {
            try {
                ProjectWriter.writeFile(projectfile.getAbsolutePath());
            } catch (IOException ignored) {
            }
        }
    }

    @FXML
    private void loadProject(ActionEvent event){
        fileChooser.setTitle("Load Project");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project File", "*.ledproj"));
        File pcbfile = fileChooser.showOpenDialog(mainMenu.getScene().getWindow());

        if (pcbfile != null) {
            frames.clear();

            try {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser saxParser = spf.newSAXParser();
                XMLReader xmlReader = saxParser.getXMLReader();
                xmlReader.setContentHandler(new ProjectReader());
                xmlReader.parse(pcbfile.getAbsolutePath());
            } catch (IOException | SAXException | ParserConfigurationException e) {
                e.printStackTrace();
                return;
            }

            // The loader can't initialize the events, so we need to do that now.
            for (Frame f : Model.getInstance().getFrames()) {
                frames.add(f);
                for (LEDCircle c : f.getCircleMap().values()) {
                    initializeLEDCircle(c);
                }
            }
            displayFrame(Model.getInstance().getCurrentFrame());
            frameList.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void clearFrame(ActionEvent event){
//        for (LEDCircle c : Model.getInstance().getCurrentFrame().getCircleMap().values()) {
//            c.clear();
//        }
        Model.getInstance().getCurrentFrame().clearFrameCommand();
    }

    @FXML
    public void exitProgram(ActionEvent actionEvent) {
        System.exit(0);
    }

    @FXML
    public void addFrame(ActionEvent e){
        Frame nf = createNewFrame();
        frames.add(nf);
    }

    public void export(String name, String ftype, Export exporter){
        fileChooser.setTitle("Export");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(name, ftype));
        File exportfile = fileChooser.showSaveDialog(mainMenu.getScene().getWindow());

        if (exportfile != null) {
            try {
                FileWriter fw = new FileWriter(exportfile);
                PrintWriter pw = new PrintWriter(fw);
                pw.print(exporter.generateOutput());
                pw.close();
            } catch (IOException ignored) {
            }
        }
    }

    @FXML
    public void clickFrame(MouseEvent e){
        Frame selectedFrame = frameList.getSelectionModel().getSelectedItem();
        displayFrame(selectedFrame);
        Model.getInstance().setCurrentFrame(selectedFrame);
    }


    public void exportFastLED(ActionEvent actionEvent) {
        export("FastLED C++ File", "*.cpp", new ExportFastLED());
    }

    public void exportAdafruit(ActionEvent actionEvent) {
        export("Adafruit Neopixel C++ File", "*.cpp", new ExportAdafruit());
    }


    public void undo(ActionEvent actionEvent) {
        Model.getInstance().getCurrentFrame().undo();
    }

    public void redo(ActionEvent actionEvent) {
        Model.getInstance().getCurrentFrame().redo();
    }

    public void showAbout(ActionEvent actionEvent) {
        //https://icons8.com
        ButtonType okButtonType = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("About");
        dialog.setContentText("LED Scene Maker by Daniel R. Schlegel\nCopyright 2021\n\nSome icons by https://icons8.com");
        dialog.getDialogPane().getButtonTypes().add(okButtonType);
        dialog.getDialogPane().lookupButton(okButtonType).setDisable(false);
        dialog.showAndWait();
    }

    public void duplicateFrame(ActionEvent actionEvent) {
        Frame nf = createNewFrame(true);
        frames.add(nf);
    }

    public void deleteFrame(ActionEvent actionEvent) {
        if (Model.getInstance().getFrames().size() == 0) return;
        if (Model.getInstance().getFrames().size() == 1) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Cannot delete the only frame.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        Model.getInstance().deleteFrame(frameList.getSelectionModel().getSelectedItem());
    }
}
