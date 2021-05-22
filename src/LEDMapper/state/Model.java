package LEDMapper.state;

import LEDMapper.components.LED_ws2812b;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Model {
    private static Model m;
    private String pcbFilename;
    private ArrayList<Frame> frames;
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
        frames = new ArrayList<>();
    }

    public static Model getInstance() {
        if (m == null) m = new Model();
        return m;
    }

    public Frame getCurrentFrame(){
        if (currentFrame == null) {
            this.currentFrame = new Frame(); // Initialize the frame if needed.
            frames.add(this.currentFrame);
        }
        return currentFrame;
    }

    public List<Frame> getFrames(){
        return Collections.unmodifiableList(frames);
    }

    public Frame newFrame(){
        Frame f = new Frame();
        frames.add(f);
        if (this.currentFrame == null) currentFrame = f;
        return f;
    }

    public void setCurrentFrame(int index){
        currentFrame = frames.get(index);
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

