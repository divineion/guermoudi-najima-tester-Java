package com.parkit.parkingsystem.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FormatUtil {

    public static double roundToTwoDecimals(double number) {
    
        BigDecimal formattedNumber = new BigDecimal(number).setScale(2, RoundingMode.HALF_UP);

        return formattedNumber.doubleValue();
    }

}
