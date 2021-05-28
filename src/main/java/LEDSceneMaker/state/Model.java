package LEDSceneMaker.state;

import LEDSceneMaker.components.LED_ws2812b;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Model {
    private static Model m;
    private String pcbFilename;
    private ObservableList<Frame> frames;
    private Frame currentFrame;
    private List<LED_ws2812b> leds;

    private Model() {
        reinitializeModel();
    }

    /**
     * Reinitializes the model state. All existing data in the model is lost.
     */
    public void reinitializeModel(){
        pcbFilename = "";
        leds = new ArrayList<>();
        frames = FXCollections.observableArrayList();
    }

    public static Model getInstance() {
        if (m == null) m = new Model();
        return m;
    }

    public Frame getCurrentFrame() {
        if (currentFrame == null) {
            this.currentFrame = new Frame(); // Initialize the frame if needed.
            frames.add(this.currentFrame);
        }
        return currentFrame;
    }

    public ObservableList<Frame> getFrames(){
        return frames;
    }

    public Frame newFrame(){
        Frame f = new Frame();
        frames.add(f);
        if (this.currentFrame == null) currentFrame = f;
        return f;
    }

    // We don't want to leave the model in a bad state and trust the controller to fix it, so select another frame.
    // By default the one after the one being removed. If there is not one there, go to before it.
    public void deleteFrame(Frame frame){
        int idx = frames.indexOf(frame);
        frames.remove(frame);
        if (idx == frames.size()) currentFrame = frames.get(idx-1);
        else currentFrame = frames.get(idx);
    }

    public void setCurrentFrame(Frame frame){
        currentFrame = frame;
    }

    public void setLEDs (List<LED_ws2812b> leds){
        this.leds = leds;
    }

    public List<LED_ws2812b> getLEDs(){
        return Collections.unmodifiableList(leds);
    }

    public void setPcbFilename(String pcbFilename) {
        this.pcbFilename = pcbFilename;
    }

    public String getPcbFilename(){
        return pcbFilename;
    }

    public LED_ws2812b firstLED(){
        LED_ws2812b firstLED = leds.get(0);
        while (firstLED.getPreviousLED() != null) {
            firstLED = firstLED.getPreviousLED();
        }

        return firstLED;
    }
}

