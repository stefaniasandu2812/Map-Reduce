public class ResultTaskReduce {
    public String docName;
    public float rank;
    public int maxLen;
    public int noMaxLenWords;

    public ResultTaskReduce(String docName, float rank, int maxLen, int noMaxLenWords) {
        this.docName = docName;
        this.rank = rank;
        this.maxLen = maxLen;
        this.noMaxLenWords = noMaxLenWords;
    }
}
