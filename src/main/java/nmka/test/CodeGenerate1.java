package nmka.test;

import java.util.concurrent.atomic.AtomicInteger;

public class CodeGenerate1 {
    private static AtomicInteger integer = new AtomicInteger(0);

    public static void setInt(int id) {
        integer = new AtomicInteger(id);
    }

    public String generate() {
        return integer.incrementAndGet() + "";
    }
}
