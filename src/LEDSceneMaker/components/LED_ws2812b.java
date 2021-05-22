package LEDSceneMaker.components;

import LEDSceneMaker.util.ExactPoint;

import java.math.BigDecimal;

public class LED_ws2812b {
    private final String reference;
    private final BigDecimal x;
    private final BigDecimal y;
    private final PCBSide side;
    private LED_ws2812b previousLED;
    private LED_ws2812b nextLED;

    public LED_ws2812b(String reference, BigDecimal x, BigDecimal y, PCBSide side){
        this.reference = reference;
        this.x = x;
        this.y = y;
        this.side = side;
    }

    public String getReference() {
        return reference;
    }

    public BigDecimal getX() {
        return x;
    }

    public BigDecimal getY() {
        return y;
    }

    public PCBSide getSide() { return side; }

    public String toString(){
        return "ws2812b LED (" + (side == PCBSide.FRONT ? "F.Cu" : "B.Cu") + "): "
                + reference + " (x: " + x + ", y: " + y + ")";
    }

    // Pad 2 is data output
    public ExactPoint getPad2Location(){
        return new ExactPoint(x.subtract(new BigDecimal("2.45")), y.add(new BigDecimal("1.6")));
    }

    // Pad 4 is data input
    public ExactPoint getPad4Location(){
        return new ExactPoint(x.add(new BigDecimal("2.45")), y.subtract(new BigDecimal("1.6")));
    }

    public void setPreviousLED(LED_ws2812b previous){
        previousLED = previous;
    }

    public void setNextLED(LED_ws2812b next){
        nextLED = next;
    }

    public LED_ws2812b getPreviousLED(){
        return previousLED;
    }

    public LED_ws2812b getNextLED(){
        return nextLED;
    }
}
