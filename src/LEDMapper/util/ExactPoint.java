package LEDMapper.util;

import java.math.BigDecimal;

public class ExactPoint {
    BigDecimal x;
    BigDecimal y;

    public ExactPoint(BigDecimal x, BigDecimal y){
        this.x = x;
        this.y = y;
    }

    public String toString(){
        return "ExactPoint [x = " + x + ", y = " + y + "]";
    }

    public boolean equals(Object other){
        if (! (other instanceof ExactPoint e)) return false;
        return x.compareTo(e.x)==0 && y.compareTo(e.y)==0;
    }

    public int hashCode(){
        return x.add(y).intValue(); // Not optimal, but it's fine...
    }
}
