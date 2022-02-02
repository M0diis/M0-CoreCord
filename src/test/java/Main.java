import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class Main
{
    public static void main(String[] args)
    {
//        List<Integer> cycleList = new ArrayList<>();
//
//        for(int i = 0; i < 100; i++)
//        {
//            cycleList.add(i);
//        }
    
        //cycleTest(false, cycleList);
        //System.out.println();
        //cycleTest(true, cycleList);
        
        //argTest();
        
        sbTest();
    }
    
    private static void sbTest()
    {
        List<String> userIDs = new ArrayList<>();
        
        userIDs.add("1");
        userIDs.add("3");
        userIDs.add("5");
        userIDs.add("10");
        
        StringBuilder inb = new StringBuilder();
    
        for(String id : userIDs)
            inb.append("'").append(id).append("',");
    
        inb.deleteCharAt(inb.length() - 1);
        
        System.out.println(inb);
        
        String streamTest = userIDs.stream().map(str -> "'" + str + "'").collect(Collectors.joining(", "));
        
        System.out.println(streamTest);
    }
    
    private static void argTest()
    {
        String msg = "co!lookup u:M0dii a:command t:7d";
        
        List<String> tempArgs = new ArrayList<>(Arrays.asList(msg.split(" ")));
        
        String cmd = tempArgs.get(0);
    
        tempArgs.remove(0);
        
        String[] args = tempArgs.toArray(new String[0]);
        
        print(cmd, "\n");
        
        print(args[0], "\n");
        print(args[1], "\n");
        print(args[2], "\n");
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
