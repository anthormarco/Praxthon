package com.praxis.sandbox_spei.util;

public class ClabeUtil {

    public static boolean es18Digitos(String cuenta) {
        return cuenta != null && cuenta.matches("^[0-9]{18}$");
    }

    // Algoritmo oficial §5.5 de tu guía
    public static int digitoVerificador(String primeros17) {
        int[] pesos = {3, 7, 1};
        int suma = 0;
        for (int i = 0; i < 17; i++) {
            int digito = primeros17.charAt(i) - '0';
            suma += (digito * pesos[i % 3]) % 10;
        }
        return (10 - (suma % 10)) % 10;
    }

    public static boolean valida(String cuenta18) {
        if (!es18Digitos(cuenta18)) return false;
        String p17 = cuenta18.substring(0, 17);
        int esperado = digitoVerificador(p17);
        int real = cuenta18.charAt(17) - '0';
        return esperado == real;
    }

    // Prueba con escenarios 9002, 9003, etc.
    // Dígitos 14 al 17 = posiciones 13 al 16 (0-index)
    public static String generarCuentaValida(String institucion3, String plaza3, String digitos14a17) {
        // institucion(3) + plaza(3) + 7 ceros + digitos14a17 = 17 dígitos
        String p17 = institucion3 + plaza3 + "0000000" + digitos14a17;
        return p17 + digitoVerificador(p17);
    }

    // Main unicamente para mandar pruebas
   /* public static void main(String[] args) {
        System.out.println(ClabeUtil.digitoVerificador("03518000011835971")); // debe imprimir 9
        System.out.println(ClabeUtil.digitoVerificador("10315012415234578")); // debe imprimir 6
        System.out.println(ClabeUtil.valida("032180000118359719")); // debe ser true
    }*/
}
