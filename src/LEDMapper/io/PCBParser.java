package LEDMapper.io;

import LEDMapper.components.LED_ws2812b;
import LEDMapper.components.PCBSide;
import LEDMapper.util.ExactPoint;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 This class does a full parse of the file into a format we can more easily use, then just looks for a few things.
 */
public class PCBParser {
    private static final Pattern parsepattern = Pattern.compile("([\\)\\(])|[A-Za-z0-9_:\\.]+|\".*?\"");

    private static String slurp(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath));
    }

    private static String slurp(File file) throws IOException{
        return Files.readString(file.toPath());
    }

    private static LispyList parseList(Matcher matcher){
        LispyList l = new LispyList();

        while(matcher.find()){
            String found = matcher.group();
            if (found.equals(")")) return l;
            if (found.equals("(")) {
                l.add(parseList(matcher));
            }
            else {
                l.add(new LispyString(found));
            }
        }
        return l; // Should never get here anyway...
    }

    public static LispyList parseFileString(String pcbFileContents){
        Matcher matcher = parsepattern.matcher(pcbFileContents);

        boolean b = matcher.find();
        if (matcher.group().equals("(")){
            return parseList(matcher);
        }
        else {
            System.out.println("Invalid file format!");
            return new LispyList();
        }
    }

    public static List<LispyList> ledModules(List<LispyList> allModules){
        List<LispyList> leds = new ArrayList<>();
        for (LispyList module : allModules){
            if (module.nth(1).toString().startsWith("LED_SMD:LED_WS2812B"))
                leds.add(module);
        }
        return leds;
    }

    public static LED_ws2812b buildLED(LispyList ledModule){
        PCBSide layer = (ledModule.nth(2).toString().contains("F.Cu") ? PCBSide.FRONT : PCBSide.BACK);
        LispyList location = ledModule.getSublistsNamed("at").get(0);
        String x = location.nth(1).toString();
        String y = location.nth(2).toString();

        ArrayList<LispyList> textitems = ledModule.getSublistsNamed("fp_text");
        for (LispyList item : textitems){
            if (item.nth(1).toString().equals("reference")){
                String reference = item.nth(2).toString();
                return new LED_ws2812b(reference, new BigDecimal(x), new BigDecimal(y), layer);
            }
        }
        return null;
    }


    private static List<LED_ws2812b> parseHelper(String pcbFileContents){
        LispyList parsedFile = parseFileString(pcbFileContents);

        /*** Get LED Modules ***/
        List<LispyList> modules = parsedFile.getSublistsNamed("module");
        List<LispyList> ledModules = ledModules(modules);

        List<LED_ws2812b> leds = new ArrayList<>();

        Map<ExactPoint, LED_ws2812b> pad4map = new HashMap<>();

        for (LispyList led : ledModules){
            LED_ws2812b newled = buildLED(led);
            leds.add(newled);
            pad4map.put(newled.getPad4Location(), newled);
        }

        /*** Set up track graph ***/
        TrackGraph tg = new TrackGraph();

        ArrayList<LispyList> trackSegments = parsedFile.getSublistsNamed("segment");
        for (LispyList segment : trackSegments){
            // Example segment: (segment (start 115.1625 162.9675) (end 115.57 162.56) (width 0.25) (layer B.Cu) (net 0))
            LispyList startList = (LispyList) segment.nth(1);
            LispyList endList = (LispyList) segment.nth(2);
            LispyList layerList = (LispyList) segment.nth(4);
            ExactPoint start = new ExactPoint(new BigDecimal(startList.nth(1).toString()),
                    new BigDecimal(startList.nth(2).toString()));
            ExactPoint end = new ExactPoint(new BigDecimal(endList.nth(1).toString()),
                    new BigDecimal(endList.nth(2).toString()));
            PCBSide layer = (layerList.nth(1).toString().contains("F.Cu") ? PCBSide.FRONT : PCBSide.BACK);

            tg.addSegment(start, end, layer);
        }

        ArrayList<LispyList> vias = parsedFile.getSublistsNamed("via");
        for (LispyList via : vias){
            LispyList atList = (LispyList) via.nth(1);
            LispyList layerList = (LispyList) via.nth(4);
            ExactPoint loc = new ExactPoint(new BigDecimal(atList.nth(1).toString()),
                    new BigDecimal(atList.nth(2).toString()));
            PCBSide layer1 = (layerList.nth(1).toString().contains("F.Cu") ? PCBSide.FRONT : PCBSide.BACK);
            PCBSide layer2 = (layerList.nth(2).toString().contains("F.Cu") ? PCBSide.FRONT : PCBSide.BACK);
            tg.addVia(loc, layer1, layer2);
        }

        for (LED_ws2812b led : leds){
            // Output of led
            ExactPoint outputPadLoc = led.getPad2Location();

            // Connected points to the pad
            List<ExactPoint> connectedPoints = tg.connectionPoints(outputPadLoc, led.getSide());
            LED_ws2812b next = null;
            for (ExactPoint p : connectedPoints){
                if(pad4map.containsKey(p)) {
                    next = pad4map.get(p);
                    break;
                }
            }

            if (next == null){
                System.out.println(led + " " + "has no next");
            }
            else {
                led.setNextLED(next);
                next.setPreviousLED(led);
            }
        }

        return leds;
    }

    public static List<LED_ws2812b> parse(String filePath) throws IOException{
        return parseHelper(slurp(filePath));
    }

    public static List<LED_ws2812b> parse(File file) throws IOException{
        return parseHelper(slurp(file));
    }


    public static void main(String[] args) throws IOException{
        // Testing!
        String testFile= "F:\\Dropbox\\Hobbies\\Electronics\\LightExperiment-Stream\\BigTree\\bigtree\\bigtree.kicad_pcb";

        for (LED_ws2812b led : parse(testFile)){
            System.out.println(led);
        }
    }




}
