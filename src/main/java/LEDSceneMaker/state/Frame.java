/*
 A frame is simply an application of colors to the LEDs. A frame can be diffed against another frame to expose only
 what the differences are between the frames.
 */

package LEDSceneMaker.state;

import LEDSceneMaker.components.LED_ws2812b;
import LEDSceneMaker.logic.ChangeColorCommand;
import LEDSceneMaker.logic.ClearCommand;
import LEDSceneMaker.logic.ClearFrameCommand;
import LEDSceneMaker.logic.Command;
import LEDSceneMaker.ui.LEDCircle;
import javafx.scene.paint.Color;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Frame {
    Map<LED_ws2812b, LEDCircle> circleMap;

    // For undo/redo
    private int undoRedoPointer = -1;
    private final Stack<Command> commandStack = new Stack<>();

    public Frame(){
        circleMap = new HashMap<>();
    }

    public void addCircleMapping(LEDCircle circle, LED_ws2812b led){
        circleMap.put(led,circle);
    }

    public Map<LED_ws2812b, LEDCircle> getCircleMap(){
        return Collections.unmodifiableMap(circleMap);
    }

    public String toString(){
        return "Frame";
    }

    /// Undo / Redo Logic ///
    public void changeColorCommand(LEDCircle circle, Color newColor) {
        if (!circle.isCleared() && circle.getFill().equals(newColor)) return;

        deleteElementsAfterPointer(undoRedoPointer);
        Command command =  new ChangeColorCommand(circle, newColor);
        command.execute();
        commandStack.push(command);
        undoRedoPointer++;
    }

    public void clearCommand(LEDCircle circle) {
        if (circle.isCleared()) return;

        deleteElementsAfterPointer(undoRedoPointer);
        Command command = new ClearCommand(circle);
        command.execute();
        commandStack.push(command);
        undoRedoPointer++;
    }

    public void clearFrameCommand() {
        deleteElementsAfterPointer(undoRedoPointer);
        Command command = new ClearFrameCommand();
        command.execute();
        commandStack.push(command);
        undoRedoPointer++;
    }

    private void deleteElementsAfterPointer(int undoRedoPointer) {
        if(commandStack.size()<1)
            return;

        for(int i = commandStack.size()-1; i > undoRedoPointer; i--) {
            commandStack.remove(i);
        }
    }

    public void undo() {
        if(undoRedoPointer < 0)
            return;

        Command command = commandStack.get(undoRedoPointer);
        command.unExecute();
        undoRedoPointer--;
    }

    public void redo() {
        if(undoRedoPointer == commandStack.size() - 1)
            return;

        undoRedoPointer++;
        Command command = commandStack.get(undoRedoPointer);
        command.execute();
    }
}
