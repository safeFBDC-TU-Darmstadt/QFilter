import user.queryprocessing.ResultCollector;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResultCollectorTests {

    @Test
    void testSlideExample() {
        List<BigInteger> results = Arrays.asList(new BigInteger("776"), new BigInteger("8562"), new BigInteger("38072"), new BigInteger("112634"), new BigInteger("264192"));
        assertEquals(BigInteger.valueOf(2), ResultCollector.getCountResult(results));
    }

}
