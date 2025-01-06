package app.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    public static String formatToRupiah(double amount) {
        // Locale untuk Indonesia
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(amount);
    }
}
