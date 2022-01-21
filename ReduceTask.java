import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class ReduceTask implements Callable<ResultTaskReduce> {
    private final String docName;
    private final List<HashMap<Integer, Integer>> mapList;
    private final List<List<String>> maxList;
    private final int maxLen;
    private final int noWords;

    // local
    private HashMap<Integer, Integer> finalMap;

    public ReduceTask(String docName, List<HashMap<Integer, Integer>> mapList,
                      List<List<String>> maxList, int maxLen, int noWords) {
        this.docName = docName;
        this.mapList = mapList;
        this.maxList = maxList;
        this.maxLen = maxLen;
        this.noWords = noWords;
    }

    // Fibonacci function
    public int Fibo(int number) {
        if (number == 0) {
            return 0;
        } else if (number == 1) {
            return 1;
        }
        return Fibo(number - 1) + Fibo(number - 2);
    }

    @Override
    public ResultTaskReduce call() throws Exception {
        finalMap = new HashMap<>();

        // combine maps
        for (HashMap<Integer, Integer> map : mapList) {
            map.forEach(
                    (key, value) -> finalMap.merge(key, value, Integer::sum)
            );
        }

        // combine lists of words
        List<String> finalList = new ArrayList<>();
        for (List<String> wordList : maxList) {
            for (String word : wordList) {
                if (word.length() == maxLen) {
                    finalList.add(word);
                }
            }
        }

        // compute sum for the rank formula
        int S = 0;
        for (Integer key : finalMap.keySet()) {
            S += Fibo(key + 1) * finalMap.get(key);
        }

//        System.out.println("sum " + S + " " + noWords + " " + docName);

        // compute rank
        float rank = (float) S / noWords;

        // get file name
        String fileName = new File(docName).getName();

        // return result
        return new ResultTaskReduce(fileName, rank, maxLen, finalList.size());
    }
}
