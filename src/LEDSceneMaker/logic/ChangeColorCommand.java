package LEDSceneMaker.logic;

import LEDSceneMaker.ui.LEDCircle;
import javafx.scene.paint.Paint;

public class ChangeColorCommand extends Command{
    LEDCircle circle;
    Paint oldColor;
    boolean wasCleared;
    Paint newColor;

    public ChangeColorCommand(LEDCircle circle, Paint newColor){
        this.circle = circle;
        this.newColor = newColor;
    }

    @Override
    public void execute() {
        oldColor = circle.getFill();
        wasCleared = circle.isCleared();
        circle.setLEDColor(newColor);
    }

    @Override
    public void unExecute() {
        if (wasCleared) circle.clear();
        else circle.setLEDColor(oldColor);
    }
}
