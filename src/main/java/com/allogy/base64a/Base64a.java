package com.allogy.base64a;

/**
 * Based off a class to convert numbers to Base62. Default charset: 0..9a..zA..Z Alternate
 * character sets can be specified when constructing the object.
 *
 * Original author: Andreas Holley
 *
 * User: robert
 * Date: 2013/10/21
 * Time: 10:38 AM
 */
public class Base64a
{
    private final String characters;

    public
    Base64a()
    {
        this("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.:");
    }

    /**
     * Constructs a Base64a object with a custom charset.
     *
     * @param characters
     * the charset to use. Must be 64 characters.
     * @throws <code>IllegalArgumentException<code> if the supplied charset is not 64 characters long.
     */
    public
    Base64a(String characters)
    {
        if (characters.length() != 64)
        {
            throw new IllegalArgumentException("Invalid string length, must be 64.");
        }
        this.characters = characters;
    }

    private static final int SIX_BIT_MASK=0x3F;

    /**
     * Encodes a 64-bit long value to a Base64a <code>String</code>.
     *
     * @param b10 the long value to encode
     * @return the number encoded as a Base64a <code>String</code>.
     */
    public
    String encodeLong(long b10)
    {
        StringBuilder ret = new StringBuilder(11);

        do
        {
            char c = characters.charAt((int) (b10 & SIX_BIT_MASK));
            //ret = c + ret;
            //b10 /= 64;
            ret.insert(0, c);

            //NB: "unsigned" right-shift is important, will eventually zero-out the input (even if negative).
            b10 = b10 >>> 6;
        }
        while (b10 != 0);

        return ret.toString();
    }

    /**
     * Encodes a 32-bit integer value to a Base64a <code>String</code>.
     *
     * @param b10 the integer value to encode
     * @return the number encoded as a Base64a <code>String</code>.
     */
    public
    String encodeInt(int b10)
    {
        StringBuilder ret = new StringBuilder(6);

        do
        {
            char c = characters.charAt((int) (b10 & SIX_BIT_MASK));
            ret.insert(0, c);

            //NB: "unsigned" right-shift is important, will eventually zero-out the input (even if negative).
            b10 = b10 >>> 6;
        }
        while (b10 != 0);

        return ret.toString();
    }


    /**
     * Decodes a Base64a <code>String</code> returning a <code>long</code>.
     *
     * @param b64
     * the Base64a <code>String</code> to decodeLong.
     * @return the decoded number as a <code>long</code>.
     * @throws IllegalArgumentException
     * if the given <code>String</code> contains characters not
     * specified in the constructor.
     */
    public
    long decodeLong(String b64)
    {
        long ret = 0;
        b64 = new StringBuilder(b64).reverse().toString();

        long magnitude = 1;
        for (char character : b64.toCharArray())
        {
            int i = characters.indexOf(character);
            if (i<0)
            {
                throw new IllegalArgumentException("Invalid character(s) in string: " + character);
            }

            ret += i * magnitude;
            //magnitude *= 64;
            magnitude = magnitude << 6;
        }
        return ret;
    }

    /**
     * Decodes a Base64a <code>String</code> returning a <code>long</code>.
     *
     * @param b64
     * the Base64a <code>String</code> to decodeLong.
     * @return the decoded number as a <code>long</code>.
     * @throws IllegalArgumentException
     * if the given <code>String</code> contains characters not
     * specified in the constructor.
     */
    public
    int decodeInt(String b64)
    {
        int ret = 0;
        b64 = new StringBuilder(b64).reverse().toString();

        int magnitude = 1;
        for (char character : b64.toCharArray())
        {
            int i = characters.indexOf(character);
            if (i<0)
            {
                throw new IllegalArgumentException("Invalid character(s) in string: " + character);
            }

            ret += i * magnitude;
            //magnitude *= 64;
            magnitude = magnitude << 6;
        }
        return ret;
    }


    private static final String PRINT_FORMAT="%16s: %20d -> %11s -> %20d";

    private static
    void test(Base64a b64a, String name, int input)
    {
        String encoded=b64a.encodeInt(input);
        int output=b64a.decodeInt(encoded);
        System.out.println(String.format(PRINT_FORMAT, name, input, encoded, output));

        if (input != output)
        {
            throw new AssertionError(name+" does not survive encoding: "+input);
        }
    }

    private static
    void test(Base64a b64a, String name, long input)
    {
        String encoded=b64a.encodeLong(input);
        long output=b64a.decodeLong(encoded);
        System.out.println(String.format(PRINT_FORMAT, name, input, encoded, output));

        if (input != output)
        {
            throw new AssertionError(name+" does not survive encoding: "+input);
        }
    }

    // Examples
    public static
    void main(String[] args) throws InterruptedException
    {
        // Create a standard object using the default charset.
        Base64a standard = new Base64a();
        System.gc();
        // Create a standard object with an alternate charset.
        Base64a alternate = new Base64a("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+=");

        // Convert 1673 to standard (qZ).
        test(standard, "small-int" , 1673);
        test(standard, "small-int" , 167300);

        test(standard, "small-long", 1673l);
        test(standard, "small-long", 167300l);

        test(standard, "ZERO_INT"  , 0);
        test(standard, "MAX_INT-1" , Integer.MAX_VALUE-1);
        test(standard, "MAX_INT"   , Integer.MAX_VALUE);

        test(standard, "ZERO_LONG" , 0l);
        test(standard, "MAX_LONG-1", Long.MAX_VALUE-1);
        test(standard, "MAX_LONG"  , Long.MAX_VALUE);

        long fullCount=standard.decodeLong("12345678901");
        test(standard, "FULL", fullCount);

        long doubleClickable=standard.decodeLong("1.3:5.7:9.1");
        test(standard, "DOUBLE_CLICK", doubleClickable);

        test(standard, "NOW_MILLI", System.currentTimeMillis());
        test(standard, "NOW_NANO", System.nanoTime());

        System.out.println("\n--negative numbers--");
        test(standard, "neg-int" , -1673);
        test(standard, "neg-int" , -167300);
        test(standard, "neg-long", -1673l);
        test(standard, "neg-long", -167300l);
        test(standard, "MIN_INT" , Integer.MIN_VALUE);
        test(standard, "MIN_INT+1", Integer.MIN_VALUE+1);
        test(standard, "MIN_LONG", Long.MIN_VALUE);
        test(standard, "MIN_LONG+1", Long.MIN_VALUE+1);

        System.out.println();

        // Convert 1673 to standard with the alternate character set (A9).
        System.out.println("1673 encoded with alternate charset: " + alternate.encodeLong(1673));

        // Convert nHkl3S4B to decimal (83,458,179,955,437).
        System.out.println("nHkl3S4B decoded from standard: " + standard.decodeLong("nHkl3S4B"));

        // Encoding and decoding a number returns the original result.
        System.out.println("32442342 encoded to standard and back again: "
                + standard.decodeLong(standard.encodeLong(32442342)));

        // Using invalid characters throws a runtime exception.
        // Output was out of order with ant, adding this short sleep fixes
        // things:
        // The problem seems to be with the way ant's output handles system.err
        Thread.sleep(100);
        try {
            // Doesn't work
            System.out.println(standard.decodeLong("_j+j%"));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }
}

