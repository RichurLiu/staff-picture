import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author: liu lei
 * @Date: 2019/6/18
 */
public class Test {
    private static Logger log = LogManager.getLogger("COMMON");
    public static void main(String[] args) {
        String str = "1;;";
        String[] split = str.split(";");
        System.out.println("size:" + split.length);
        for (String s : split) {
            System.out.println("hello:" + s);
            log.info("info");
            log.error("error");
        }
    }
}
