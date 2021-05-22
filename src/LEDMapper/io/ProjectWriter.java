package LEDMapper.io;

import LEDMapper.state.Model;
import LEDMapper.state.Frame;
import LEDMapper.components.LED_ws2812b;
import LEDMapper.ui.LEDCircle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class ProjectWriter {

    public static void writeFile(String fname) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(new FileOutputStream(fname));
        printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        printWriter.println("<project pcbfile=\"" + Model.getInstance().getPcbFilename() + "\">");

        printWriter.println("<leds>");
        writeLEDs(Model.getInstance().getLEDs(), printWriter);
        printWriter.println("</leds>");

        List<Frame> frames = Model.getInstance().getFrames();
        for (int i = 0; i < frames.size(); i++){
            writeFrame(frames.get(i), i, printWriter);
        }
        printWriter.println("</project>");
        printWriter.close();
    }

    private static void writeLEDs(List<LED_ws2812b> leds, PrintWriter printWriter){
        for (LED_ws2812b led : leds){
            printWriter.print("<led ");
            printWriter.print("reference=\"" + led.getReference()
                    + "\" x=\"" + led.getX().toString() + "\" y=\"" + led.getY().toString()
                    + "\" layer=\"" + led.getSide().toString() + "\"");
            if (led.getNextLED() != null) printWriter.print(" next=\"" + led.getNextLED().getReference() + "\"");
            if (led.getPreviousLED() != null) printWriter.print(" previous=\"" + led.getPreviousLED().getReference() + "\"");
            printWriter.println(" />");
        }
    }

    private static void writeFrame(Frame frame, int idx, PrintWriter printWriter){
        printWriter.println("<frame id=\"" + idx + "\">");
        for (Map.Entry<LED_ws2812b, LEDCircle> e : frame.getCircleMap().entrySet()){
            writeFrameLED(e.getKey(), e.getValue(), printWriter);
        }
        printWriter.println("</frame>");
    }

    private static void writeFrameLED(LED_ws2812b led, LEDCircle circle, PrintWriter printWriter){
        // LED line
        printWriter.print("<led reference=\"" + led.getReference() + "\" cleared=\"" + circle.isCleared() + "\">");

        // LED color
        if (!circle.isCleared()) {
            Color c = ((Color) circle.getFill());
            printWriter.println("<color r=\"" + (int) (c.getRed() * 255) + "\" g=\""
                    + (int) (c.getGreen() * 255) + "\" b=\"" + (int) (c.getBlue() * 255) + "\" />");
        }

        // Terminate
        printWriter.println("</led>");

    }

}
