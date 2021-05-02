import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main
{
    public static void main(String[] args)
    {
        List<Integer> cycleList = new ArrayList<>();
    
        for(int i = 0; i < 10000000; i++)
        {
            cycleList.add(i);
        }
    
        //cycleTest(false, cycleList);
        //System.out.println();
        //cycleTest(true, cycleList);
    }
    
    private static void cycleTest(boolean reverse, List<String> list)
    {
        for(int i = reverse ? list.size() - 1 : 0; reverse ?  i >= 0 : i < list.size();)
        {
            print(list.get(i), " ");
        
            if(reverse)
                i--;
            else i++;
        }
    }
    
    private static void print(String str, String spacer)
    {
        System.out.print(str + spacer);
    }
}
