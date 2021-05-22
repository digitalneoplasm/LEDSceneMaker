package LEDMapper.io;

import LEDMapper.state.Model;
import LEDMapper.state.Frame;
import LEDMapper.components.LED_ws2812b;
import LEDMapper.components.PCBSide;
import LEDMapper.ui.LEDCircle;
import javafx.scene.paint.Color;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProjectReader extends DefaultHandler {

    boolean definingLEDs = false;
    List<LED_ws2812b> leds;
    HashMap<String, LED_ws2812b> ledReferenceMap;
    HashMap<String, String> nextLedReference;
    Frame frame;
    LED_ws2812b frameLED;


    public void startDocument() throws SAXException {
        Model.getInstance().reinitializeModel();
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException {
        switch (qName) {
            case "leds":
                definingLEDs = true;
                leds = new ArrayList<>();
                ledReferenceMap = new HashMap<>();
                nextLedReference = new HashMap<>();
                break;
            case "led":
                if (definingLEDs) { // We're in the leds portion of the file.
                    String reference = atts.getValue("reference");
                    BigDecimal x = new BigDecimal(atts.getValue("x"));
                    BigDecimal y = new BigDecimal(atts.getValue("y"));
                    PCBSide layer = PCBSide.valueOf(atts.getValue("layer"));
                    LED_ws2812b l = new LED_ws2812b(reference, x, y, layer);
                    leds.add(l);
                    ledReferenceMap.put(reference, l);
                    if (atts.getValue("next") != null)
                        nextLedReference.put(reference, atts.getValue("next"));
                }
                else { // We're in the frame potion of the file.
                    String reference = atts.getValue("reference");
                    boolean cleared = Boolean.parseBoolean(atts.getValue("cleared"));
                    frameLED = ledReferenceMap.get(reference);
                    if (cleared) {
                        LEDCircle c = new LEDCircle(2.5);
                        c.relocate(frameLED.getX().doubleValue(), frameLED.getY().doubleValue());
                        frame.addCircleMapping(c, frameLED);
                    }
                }
                break;
            case "frame":
                frame = Model.getInstance().newFrame();
                break;
            case "color":
                // There is only a color if it isn't cleared.
                double red = Integer.parseInt(atts.getValue("r"));
                double green = Integer.parseInt(atts.getValue("g"));
                double blue = Integer.parseInt(atts.getValue("b"));
                LEDCircle c = new LEDCircle(2.5, new Color(red/255, green/255, blue/255, 1.0));
                c.relocate(frameLED.getX().doubleValue(), frameLED.getY().doubleValue());
                frame.addCircleMapping(c, frameLED);
                break;
        }

    }

    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {
        if ("leds".equals(qName)) {
            definingLEDs = false;
            for (LED_ws2812b led : leds) {
                if (nextLedReference.containsKey(led.getReference())) {
                    LED_ws2812b next = ledReferenceMap.get(nextLedReference.get(led.getReference()));
                    led.setNextLED(next);
                    next.setPreviousLED(led);
                }
                else{
                    System.out.println(led + " has no next.");
                }
            }
            Model.getInstance().setLEDs(leds);
        }
    }

    public void endDocument() throws SAXException {

    }


}
