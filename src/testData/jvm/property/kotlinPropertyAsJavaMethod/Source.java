public class Source {
    public static void main(String[] args) {
        Target target = new Target(1);
        /*<source>*/target.getX/*<source/>*/();
    }
}