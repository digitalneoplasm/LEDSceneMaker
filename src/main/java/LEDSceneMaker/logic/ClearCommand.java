package LEDSceneMaker.logic;

import LEDSceneMaker.ui.LEDCircle;
import javafx.scene.paint.Paint;

public class ClearCommand extends Command{
    LEDCircle circle;
    Paint oldColor;

    public ClearCommand(LEDCircle circle){
        this.circle = circle;
    }

    @Override
    public void execute() {
        oldColor = circle.getFill();
        circle.clear();
    }

    @Override
    public void unExecute() {
        circle.setLEDColor(oldColor);
    }
}
