package reports;

import java.util.HashSet;

/**
 * Created by gurramvinay on 8/14/15.
 */
public class Test {
    public static void main(String[] args){
        HashSet<String> a = new HashSet<String>();
        a.add("2");
        a.add("1");
        a.add("3");
        a.add("4");
        a.add("2");
        HashSet<String> b = new HashSet<String>();
        b.add("12");
        b.add("1");
        b.add("3");
        b.add("4");

        HashSet<String> inte = new HashSet<String>(a);
        inte.retainAll(b);
        System.out.println(inte.toString());
    }
}
