public class Main {
    public static void main(String[] args) {
        if ("" instanceof /*<source>*/Target/*<source/>*/ target) {
            System.out.println(target);
        }
    }
}
/*<target>*/class Target {}/*<target/>*/