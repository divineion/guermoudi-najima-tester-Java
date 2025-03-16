package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.util.FormatUtil;

public class FormatUtilTest {
    @DisplayName("arrondi si la troisième décimale est strictement supérieure à 5")
    @Test
    public void roundToTwoDecimalsIfThirdIsAboveHalf() {
        double formattedNumber = FormatUtil.roundToTwoDecimals(123.456);

        assertEquals(123.46, formattedNumber);
    }

    @DisplayName("arrondi si la troisième décimale est inférieure à 5")
    @Test
    public void roundToTwoDecimalsIfThirdIsBelowHalf() {
        double formattedNumber = FormatUtil.roundToTwoDecimals(1.234);

        assertEquals(1.23, formattedNumber);

    }

    @DisplayName("arrondi si la troisième décimale est égale à 5")
    @Test
    public void roundToTwoDecimalsIfThirdIsEqualHalf() {
        double formattedNumber = FormatUtil.roundToTwoDecimals(12.345);

        assertEquals(12.35, formattedNumber);
    }

    @DisplayName("arrondi si la troisième décimale est supérieure à 5")
    @Test
    public void roundToTwoDecimalsIfThirdIsOverHalf() {
        double formattedNumber = FormatUtil.roundToTwoDecimals(12.346);

        assertEquals(12.35, formattedNumber);
    }

    @DisplayName("Ne modifie pas un nombre déjà arrondi à deux décimales")
    @Test
    public void doesNotModifyAlreadyRoundedNumber() {        
        double formattedNumber = FormatUtil.roundToTwoDecimals(12.34);

        assertEquals(formattedNumber, 12.34);
    }

    @DisplayName("ajoute deux décimales même pour un entier")
    @Test
    public void addTwoDecimalsForInteger() {
        double formattedNumber = FormatUtil.roundToTwoDecimals(123);
        
        assertEquals(123.00, formattedNumber); 
    }
}
