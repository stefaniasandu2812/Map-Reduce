import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

public class MapTask implements Callable<ResultTaskMap> {
    private final String docName;
    private final int offset;
    private final int dimFrag;

    // locals
    private HashMap<Integer, Integer> hashMap = new HashMap<>();
    private List<String> wordsList = new ArrayList<>();

    public MapTask(String docName, int offset, int dimFrag) {
        this.docName = docName;
        this.offset = offset;
        this.dimFrag = dimFrag;
    }

    @Override
    public ResultTaskMap call() throws Exception {
        // open file to read
        RandomAccessFile f = new RandomAccessFile("../" + docName, "r");

        // var to store the read fragment
        StringBuilder content = new StringBuilder();

        // size of file
        long dimFile = new File("../" + docName).length();

        // vars used for offsetting "half words"
        int skipOffset = 0;
        int plusDim = 0;

        // keep max length of a word
        int maxLen = 0;

        // keep number of words from a read fragment
        int noWords = 0;

        // verify if there is a half word at the beginning of my chunk
        if (offset > 0) {
            // read the prev char and the current one
            f.seek(offset - 1);
            int c1 = f.read();
            int c2 = f.read();

            // verify if chars are not a symbol
            if (Character.isLetterOrDigit((char) c1) && Character.isLetterOrDigit((char) c2)) {
                ++skipOffset;

                // read until finish word
                while (Character.isLetterOrDigit((char) f.read())) {
                    ++skipOffset;
                }
            }
        }

        // verify if there is a half word at the end of my chunk
        if (offset + dimFrag < dimFile) {
            // read the last char and the next one
            f.seek(offset + dimFrag - 1);
            int c3 = f.read();
            int c4 = f.read();

            // verify if chars are not a symbol
            if (Character.isLetterOrDigit((char) c3) && Character.isLetterOrDigit((char) c4)) {
                ++plusDim;

                // read until finish word
                while (Character.isLetterOrDigit((char) f.read())) {
                    ++plusDim;
                }
            }
        }

        // read the correct chunk of data
        f.seek(offset + skipOffset);
        for (int i = 0; i < dimFrag + plusDim - skipOffset; ++i) {
            int c = f.read();
            content.append((char) c);
        }

//        if(docName.equals("tests/files/alls_well_full")) {
//            System.out.println(content.toString());
//            System.out.println("---------------------------------------");
//        }

        // get the words separated from fragment
        String separators = ";:/?~\\.,><`[]{}()!@#$%^&-_+'=*\"| \t\r\n";
        StringTokenizer st = new StringTokenizer(content.toString(), separators);

        while (st.hasMoreTokens())
        {
            String word = st.nextToken();
            noWords++;

            // compute hashmap
            if (hashMap.containsKey(word.length())) {
                // increment if there is a word of that length
                Integer value = hashMap.get(word.length());
                hashMap.replace(word.length(), value + 1);
            } else {
                // put the pair in the hashmap
                hashMap.put(word.length(), 1);
            }

            // compute list
            if (wordsList.isEmpty()) {
                // add the word to the list
                wordsList.add(word);
                maxLen = word.length();
            } else {
                if (word.length() == maxLen) {
                    // add the word if its len is max
                    wordsList.add(word);
                } else if (word.length() > maxLen) {
                    // update maxLen and clear list
                    wordsList.clear();
                    wordsList.add(word);
                    maxLen = word.length();
                }
            }
        }

        if (docName.equals("tests/files/alls_well_full")) {
            System.out.println(docName + " " + "offset " + offset + " " + noWords);
        }

        return new ResultTaskMap(docName, hashMap, wordsList, maxLen, noWords);
    }
}
