import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ResultTaskMap {
    public final String docName;
    public final HashMap<Integer, Integer> hashMap;
    public final List<String> wordsList;
    public int maxLen;
    public final int noWords;

    public ResultTaskMap(String docName, HashMap<Integer, Integer> hashMap,
                         List<String> wordsList, int maxLen, int noWords) {
        this.docName = docName;
        this.hashMap = hashMap;
        this.wordsList = wordsList;
        this.maxLen = maxLen;
        this.noWords = noWords;
    }

}
