// A region is a collection of LEDs, potentially non-contiguous, for which it would be useful to have a

package LEDSceneMaker.state;

public class Region {
    private String name;
    private static int id = 1;

    public Region(String name){
        this.name = name;
    }

    public Region(){
        this("Region " + id++);
    }

    public String toString(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }
}
