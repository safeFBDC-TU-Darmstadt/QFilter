import user.exceptions.QueryProcessingException;
import common.UnaryTranslator;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnaryTranslatorTests {

    @Test
    void testUnaryTranslation1() throws QueryProcessingException {
        Map<String, Integer> unaryTranslationMetaMockup = new HashMap<>();
        unaryTranslationMetaMockup.put("lineitem", 5);
        UnaryTranslator.initUnaryTranslatorSingleton(unaryTranslationMetaMockup);

        UnaryTranslator unaryTranslator = UnaryTranslator.getUnaryTranslatorSingleton();
        List<Integer> translation = unaryTranslator.translatePositiveIntegerString("lineitem", "01234");
        List<Integer> digit1 = Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        List<Integer> digit2 = Arrays.asList(0, 1, 0, 0, 0, 0, 0, 0, 0, 0);
        List<Integer> digit3 = Arrays.asList(0, 0, 1, 0, 0, 0, 0, 0, 0, 0);
        List<Integer> digit4 = Arrays.asList(0, 0, 0, 1, 0, 0, 0, 0, 0, 0);
        List<Integer> digit5 = Arrays.asList(0, 0, 0, 0, 1, 0, 0, 0, 0, 0);

        List<Integer> expected = new ArrayList<>();
        expected.addAll(digit1);
        expected.addAll(digit2);
        expected.addAll(digit3);
        expected.addAll(digit4);
        expected.addAll(digit5);

        for (int i = 0; i < expected.size(); i++)
            assertEquals(expected.get(i), translation.get(i));
    }

    @Test
    void testUnaryTranslation2() throws QueryProcessingException {
        Map<String, Integer> unaryTranslationMetaMockup = new HashMap<>();
        unaryTranslationMetaMockup.put("lineitem", 5);
        UnaryTranslator.initUnaryTranslatorSingleton(unaryTranslationMetaMockup);

        UnaryTranslator unaryTranslator = UnaryTranslator.getUnaryTranslatorSingleton();
        List<Integer> translation = unaryTranslator.translatePositiveIntegerString("lineitem", "56789");
        List<Integer> digit1 = Arrays.asList(0, 0, 0, 0, 0, 1, 0, 0, 0, 0);
        List<Integer> digit2 = Arrays.asList(0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
        List<Integer> digit3 = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 1, 0, 0);
        List<Integer> digit4 = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 1, 0);
        List<Integer> digit5 = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 1);

        List<Integer> expected = new ArrayList<>();
        expected.addAll(digit1);
        expected.addAll(digit2);
        expected.addAll(digit3);
        expected.addAll(digit4);
        expected.addAll(digit5);

        for (int i = 0; i < expected.size(); i++)
            assertEquals(expected.get(i), translation.get(i));
    }

    @Test
    void testUnaryTranslation3() throws QueryProcessingException {
        Map<String, Integer> unaryTranslationMetaMockup = new HashMap<>();
        unaryTranslationMetaMockup.put("lineitem", 5);
        UnaryTranslator.initUnaryTranslatorSingleton(unaryTranslationMetaMockup);

        UnaryTranslator unaryTranslator = UnaryTranslator.getUnaryTranslatorSingleton();
        List<Integer> translation = unaryTranslator.translatePositiveIntegerString("lineitem", "1");
        // number string should be padded with zeros
        List<Integer> digit1 = Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        List<Integer> digit2 = Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        List<Integer> digit3 = Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        List<Integer> digit4 = Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        List<Integer> digit5 = Arrays.asList(0, 1, 0, 0, 0, 0, 0, 0, 0, 0);

        List<Integer> expected = new ArrayList<>();
        expected.addAll(digit1);
        expected.addAll(digit2);
        expected.addAll(digit3);
        expected.addAll(digit4);
        expected.addAll(digit5);

        for (int i = 0; i < expected.size(); i++)
            assertEquals(expected.get(i), translation.get(i));
    }
}
