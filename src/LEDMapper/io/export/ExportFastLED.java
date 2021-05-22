package LEDMapper.io.export;

import LEDMapper.state.Model;
import LEDMapper.state.Frame;
import LEDMapper.components.LED_ws2812b;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

public class ExportFastLED implements Export{

    public String generateOutput(){
        Model m = Model.getInstance();

        LED_ws2812b firstLED = m.firstLED();

        List<Frame> frames = m.getFrames();
        String output = "";

        for (int i = 0; i < frames.size(); i++) {
            Frame f = frames.get(i);
            output += "// Frame " + i + "\n";

            LED_ws2812b currentLED = firstLED;
            int j = 0;
            while(currentLED.getNextLED() != null){
                Circle c = f.getCircleMap().get(currentLED);
                Color fillColor = (Color) c.getFill();
                output += "leds[" + j + "] = CRGB(" + (int)(fillColor.getRed()*255) + "," + (int)(fillColor.getGreen()*255)
                        + "," + (int)(fillColor.getBlue()*255) + ");\n";
                currentLED = currentLED.getNextLED();
                j++;
            }
        }

        return output;
    }
}
