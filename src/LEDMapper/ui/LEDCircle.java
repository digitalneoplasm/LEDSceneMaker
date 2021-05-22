package LEDMapper.ui;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class LEDCircle extends Circle {
    private boolean cleared;
    private static final Color clearedColor = Color.valueOf("F1F1F1");

    public LEDCircle(double v) {
        this(v, clearedColor);
        cleared = true;
    }

    public LEDCircle(double v, Paint p) {
        super(v, p);
        setStrokeWidth(0.5);
        setStroke(clearedColor);
        cleared = false;
    }

    public boolean isCleared() {
        return cleared;
    }

    public void clear() {
        setFill(clearedColor);
        cleared = true;
    }

    // It's unfortunate that Circle.setFill is final, otherwise we would just override that.
    public void setLEDColor(Paint p){
        setFill(p);
        cleared = false;
    }
}
