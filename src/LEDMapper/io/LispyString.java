package LEDMapper.io;

public class LispyString implements ListItem {
    private String str;

    public LispyString(String str){
        this.str = str;
    }

    public String toString(){
        return str;
    }
}
