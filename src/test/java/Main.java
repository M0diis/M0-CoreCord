import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Main
{
    public static void main(String[] args)
    {
        System.out.println("test");
    
        List<String> cycleList = new ArrayList<>();
        
        cycleList.add("Value 1");
        cycleList.add("Value 2");
        cycleList.add("Value 3");
        cycleList.add("Value 4");
        cycleList.add("Value 5");
    
        cycleTest(false, cycleList);
        System.out.println();
        cycleTest(true, cycleList);
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
