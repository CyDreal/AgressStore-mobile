package com.example.agress.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class for formatting currency values.
 * Provides consistent currency formatting throughout the application.
 */
public class CurrencyFormatter {

    private static final Locale INDONESIA = new Locale("in", "ID");
    private static final String CURRENCY_SYMBOL = "Rp";
    private static final String THOUSAND_SEPARATOR = ".";
    private static final String DECIMAL_SEPARATOR = ",";

    /**
     * Format a numeric value to Indonesian Rupiah (IDR) currency format.
     * Example: 1000000 -> "Rp1.000.000"
     *
     * @param amount The amount to format
     * @return Formatted currency string with "Rp" prefix and thousand separators
     */
    public static String formatRupiah(double amount) {
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(INDONESIA);
            symbols.setCurrencySymbol(CURRENCY_SYMBOL);
            symbols.setMonetaryDecimalSeparator(DECIMAL_SEPARATOR.charAt(0));
            symbols.setGroupingSeparator(THOUSAND_SEPARATOR.charAt(0));

            // Format without decimal places for IDR
            DecimalFormat formatter = new DecimalFormat(CURRENCY_SYMBOL + "#,##0", symbols);
            return formatter.format(amount);
        } catch (Exception e) {
            // Fallback to simple formatting if something goes wrong
            return CURRENCY_SYMBOL + String.format("%,.0f", amount).replace(",", ".");
        }
    }

    /**
     * Format a numeric value to Indonesian Rupiah (IDR) currency format.
     * Overloaded method that accepts long values.
     *
     * @param amount The amount to format as long
     * @return Formatted currency string
     */
    public static String formatRupiah(long amount) {
        return formatRupiah((double) amount);
    }

    /**
     * Format a numeric value to Indonesian Rupiah (IDR) currency format.
     * Overloaded method that accepts integer values.
     *
     * @param amount The amount to format as int
     * @return Formatted currency string
     */
    public static String formatRupiah(int amount) {
        return formatRupiah((double) amount);
    }

    /**
     * Parse a formatted Rupiah string back to a numeric value.
     * Example: "Rp1.000.000" -> 1000000.0
     *
     * @param formattedAmount The formatted currency string
     * @return Numeric value as double
     */
    public static double parseRupiah(String formattedAmount) {
        try {
            if (formattedAmount == null || formattedAmount.isEmpty()) {
                return 0.0;
            }

            // Remove currency symbol and thousand separators
            String cleanString = formattedAmount
                    .replace(CURRENCY_SYMBOL, "")
                    .replaceAll("\\s+", "")  // Remove any whitespace
                    .replace(THOUSAND_SEPARATOR, "");

            // Replace decimal separator with dot for parsing
            cleanString = cleanString.replace(DECIMAL_SEPARATOR, ".");

            return Double.parseDouble(cleanString);
        } catch (Exception e) {
            return 0.0;
        }
    }
}