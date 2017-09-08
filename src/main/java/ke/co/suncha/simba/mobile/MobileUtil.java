package ke.co.suncha.simba.mobile;

import java.text.DecimalFormat;

/**
 * Created by maitha.manyala on 9/7/17.
 */
public class MobileUtil {
    public static String getAmount(Double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        String amountStr = "00.00";

        if (Double.compare(amount, 0d) != 0) {
            amountStr = formatter.format(amount);
        }
        return amountStr;
    }

    public static String getAmountPlain(Double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        String amountStr = "0";

        if (Double.compare(amount, 0d) != 0) {
            amountStr = formatter.format(amount);
        }
        return amountStr;
    }
}
