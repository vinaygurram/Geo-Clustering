package gridbase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by gurramvinay on 6/30/15.
 */
public class Test {

    public List<List<String>> permute(String[] ids) {
        List<List<String>> permutations = new ArrayList<List<String>>();
        //empty list to continue the loop
        permutations.add(new ArrayList<String>());

        for ( int i = 0; i < ids.length; i++ ) {
            // create a temporary container to hold the new permutations
            // while we iterate over the old ones
            List<List<String>> current = new ArrayList<List<String>>();
            for ( List<String> permutation : permutations ) {
                for ( int j = 0, n = permutation.size() + 1; j < n; j++ ) {
                    List<String> temp = new ArrayList<String>(permutation);
                    temp.add(j, ids[i]);
                    current.add(temp);
                }
            }
            permutations = new ArrayList<List<String>>(current);
        }

        return permutations;
    }


    public static void main(String[] args){
        Test dd = new Test();
        String[] numbers = {"1","2","3"};
        List<List<String>> permutes= dd.permute(numbers);
        System.out.println(Arrays.deepToString(permutes.toArray()));;

    }
}
