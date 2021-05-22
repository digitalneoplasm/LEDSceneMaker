package LEDMapper.io;

import java.util.ArrayList;

public class LispyList implements ListItem {
    ArrayList<ListItem> list;

    public LispyList(){
        list = new ArrayList<>();
    }

    public void add(ListItem l){
        list.add(l);
    }

    private String getName(){
        return list.get(0).toString();
    }

    public String toString(){
        String res = "(";
        for(ListItem s : list){
            res += s + " ";
        }
        res += ")";
        return res;
    }

    public ArrayList<LispyList> getSublistsNamed(String name){
        ArrayList<LispyList> sublists = new ArrayList<>();

        for (ListItem li : list){
            if ((li instanceof LispyList ll) && (ll.getName().equals(name))){
                sublists.add(ll);
            }
        }

        return sublists;
    }

    public ListItem nth(int n){
        return list.get(n);
    }
}
