package common;

import user.exceptions.QueryProcessingException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnaryTranslator {

    private final Map<String, Integer> unaryTranslationMeta;
    private static UnaryTranslator unaryTranslatorSingleton;

    private UnaryTranslator(Map<String, Integer> unaryTranslationMeta) {
        this.unaryTranslationMeta = unaryTranslationMeta;
    }

    public static UnaryTranslator initUnaryTranslatorSingleton(Map<String, Integer> unaryTranslationMeta) {
        if (unaryTranslatorSingleton == null)
            unaryTranslatorSingleton = new UnaryTranslator(unaryTranslationMeta);
        return unaryTranslatorSingleton;
    }

    public static @NotNull UnaryTranslator getUnaryTranslatorSingleton() {
        return unaryTranslatorSingleton;
    }

    /**
     * Translates a {@link String} of a positive integer value to a list of binary values. Each digit in the string will be
     * translated into a list of 10 binary values, where only the index of the corresponding digit is set to one.
     *
     * @param table       the name of the table to translate the value {@link String} for (needed to determine the zero-padding of all values).
     * @param valueString the value {@link String} of a positive integer to translate. Will be zero-padded to length stored in the map of unary translation metadata.
     * @return a list of binary values containing the unary translation of all digits of the given value {@link String}.
     * @throws NumberFormatException    if the given value {@link String} is not an integer.
     * @throws QueryProcessingException if the given value {@link String} is not positive.
     */
    public List<Integer> translatePositiveIntegerString(String table, String valueString) throws NumberFormatException, QueryProcessingException {
        try {
            int val = Integer.parseInt(valueString);
            if (val < 0) throw new QueryProcessingException("Value string in conditions is not a positive integer.");
        } catch (NumberFormatException e) {
            throw new QueryProcessingException("Value string in conditions is not a positive integer.");
        }

        int unaryTranslationLength = unaryTranslationMeta.get(table.toLowerCase());

        if (valueString.length() < unaryTranslationLength) {
            String zeroPadding = new String(new char[unaryTranslationLength - valueString.length()]).replace("\0", "0");
            valueString = zeroPadding + valueString;
        } else if (valueString.length() > unaryTranslationLength) {
            // TODO: probably query-dependent (leave attribute condition out if disjunctive conditions; return 0 if conjunctive?)
        }

        List<Integer> translation = new ArrayList<>();
        for (char c : valueString.toCharArray())
            translation.addAll(translateDigit(c));

        return translation;
    }

    /**
     * Translates a {@link String} of a positive float value to a list of binary values. The float value ({@link String})
     * will be transformed to an integer by removing the decimal point. The resulting {@link String} will be translated
     * using the method {@link #translatePositiveIntegerString(String, String)}.
     *
     * @param table       the name of the table to translate the value {@link String} for (needed to determine the zero-padding of all values).
     * @param valueString the value {@link String} of a positive float value to translate. Will be zero-padded to length stored in the map of unary translation metadata.
     * @return a list of binary values containing the unary translation of all digits of the given value {@link String}.
     * @throws NumberFormatException    if the given value {@link String} is not a float.
     * @throws QueryProcessingException if the given value {@link String} is not positive.
     */
    public List<Integer> translatePositiveFloatString(String table, String valueString) throws NumberFormatException, QueryProcessingException {
        try {
            float val = Float.parseFloat(valueString);
            if (val < 0) throw new QueryProcessingException("Value string in conditions is not a positive float value.");
        } catch (NumberFormatException e) {
            throw new QueryProcessingException("Value string in conditions is not a positive float value.");
        }

        return translatePositiveIntegerString(table, valueString.replace(".", ""));
    }

    /**
     * Performs the unary translation of a digit into a list of binary values (containing only a single '1' at the
     * index of the digit).
     */
    private List<Integer> translateDigit(char digit) {
        int digitVal = Integer.parseInt(String.valueOf(digit));
        List<Integer> translation = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            translation.add(i == digitVal ? 1 : 0);
        }
        return translation;
    }

    public Map<String, Integer> getUnaryTranslationMeta() {
        return unaryTranslationMeta;
    }
}
