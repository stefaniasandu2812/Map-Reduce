import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Tema2 {
    public static void main(String[] args) {
//        long startTime = System.currentTimeMillis();
        if (args.length < 3) {
            System.err.println("Usage: Tema2 <workers> <in_file> <out_file>");
            return;
        }

        // get input info
        int workers = Integer.parseInt(args[0]);

        // read from file
        BufferedReader reader;

        // write to file
        BufferedWriter writer = null;

        // length of the fragment to read
        int dimFragm = 0;

        // number of files
        int noDocs = 0;

        // list of the processed files
        List<String> files = null;

        // reading info from input file
        try {
            reader = new BufferedReader(new FileReader(args[1]));
            dimFragm = Integer.parseInt(reader.readLine());
            noDocs = Integer.parseInt(reader.readLine());

            files = new ArrayList<>(noDocs);
            for (int i = 0; i < noDocs; ++i) {
                files.add(reader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // using an ExecutorService to share map tasks to workers
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        List<Future<ResultTaskMap>> futuresList = new ArrayList<>();

        // hashmap to store results, for every file, from map tasks
        LinkedHashMap<String, ReduceObject> map = new LinkedHashMap<>();

        // share map tasks
        for (int i = 0; i < noDocs; ++i) {
            int offset = 0;
            File f = new File("../" + files.get(i));
            long dimFile = f.length();

            while (offset < dimFile) {
                if ((dimFile - offset) < dimFragm) {
                    int newDim = (int) (dimFile - offset);
                    MapTask task = new MapTask(files.get(i), offset, newDim);
                    Future<ResultTaskMap> future = executor.submit(task);
                    futuresList.add(future);
                } else {
                    MapTask task = new MapTask(files.get(i), offset, dimFragm);
                    Future<ResultTaskMap> future = executor.submit(task);
                    futuresList.add(future);
                }
                offset += dimFragm;
            }
        }

        // get results from map tasks and put them in the hashmap
        for (Future<ResultTaskMap> future : futuresList) {
            ResultTaskMap res;
            try {
                res = future.get();
                if (map.containsKey(res.docName)) {
                    ReduceObject list = map.get(res.docName);
                    list.hashmaps.add(res.hashMap);
                    list.lists.add(res.wordsList);
                    if (res.maxLen > list.maxLen) {
                        list.maxLen = res.maxLen;
                    }
                    list.noWords += res.noWords;
                } else {
                    ReduceObject list = new ReduceObject();
                    list.hashmaps.add(res.hashMap);
                    list.lists.add(res.wordsList);
                    map.put(res.docName, list);
                    list.maxLen = res.maxLen;
                    list.noWords += res.noWords;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        // using an ExecutorService to share reduce tasks to workers
        ExecutorService executor2 = Executors.newFixedThreadPool(noDocs);
        List<Future<ResultTaskReduce>> futuresReduce = new ArrayList<>();

        // create reduce tasks for workers
        for (String key : map.keySet()) {
            ReduceTask reduceTask = new ReduceTask(key, map.get(key).hashmaps, map.get(key).lists, map.get(key).maxLen, map.get(key).noWords);
            Future<ResultTaskReduce> futureReduce = executor2.submit(reduceTask);
            futuresReduce.add(futureReduce);
        }


        try {
            writer = new BufferedWriter(new FileWriter(args[2]));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // results from reduce tasks
        List<ResultTaskReduce> results = new ArrayList<>();

        // get results
        for (Future<ResultTaskReduce> futureRed : futuresReduce) {
            ResultTaskReduce resReduce = null;
            try {
                resReduce = futureRed.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            results.add(resReduce);
        }

        // sort in decreasing order results by rank
        Collections.sort(results, (o1, o2) -> Float.compare(o1.rank, o2.rank));

        Collections.reverse(results);

        // write results to output file
        for (ResultTaskReduce res : results) {
            try {
                writer.write(res.docName + "," + String.format("%.2f", res.rank) + "," + res.maxLen + "," + res.noMaxLenWords + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // close output file
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor2.shutdown();
    }

}
