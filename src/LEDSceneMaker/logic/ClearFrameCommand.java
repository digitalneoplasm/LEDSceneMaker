package LEDSceneMaker.logic;

import LEDSceneMaker.state.Model;
import LEDSceneMaker.ui.LEDCircle;
import javafx.scene.paint.Paint;

import java.util.HashMap;
import java.util.Map;

public class ClearFrameCommand extends Command{
    Map<LEDCircle, Paint> oldColorMap;
    Map<LEDCircle, Boolean> wasClearedMap;

    public ClearFrameCommand(){
        oldColorMap = new HashMap<>();
        wasClearedMap = new HashMap<>();
    }

    @Override
    public void execute() {
        // Could be more efficient - we know it's a clear op!
        for (LEDCircle c : Model.getInstance().getCurrentFrame().getCircleMap().values()) {
            Paint oldColor = c.getFill();
            oldColorMap.put(c, oldColor);
            wasClearedMap.put(c, c.isCleared());
            c.clear();
        }
    }

    @Override
    public void unExecute() {
        for (LEDCircle c : oldColorMap.keySet()) {
            if (!wasClearedMap.get(c))
                c.setLEDColor(oldColorMap.get(c));
        }
    }
}
