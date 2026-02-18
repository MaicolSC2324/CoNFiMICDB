package com.java.fx.utils;

public class TimeUtils {

    public static String segundosAFormatoHoras(long segundos) {
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        return String.format("%d:%02d", horas, minutos);
    }

    public static long formatoHorasASegundos(String formato) {
        if (formato == null || formato.isEmpty()) {
            return 0;
        }
        String[] partes = formato.split(":");
        if (partes.length == 2) {
            long horas = Long.parseLong(partes[0]);
            long minutos = Long.parseLong(partes[1]);
            return horas * 3600 + minutos * 60;
        }
        return 0;
    }

    public static String sumarTiempos(String tiempo1, String tiempo2) {
        long segundos1 = formatoHorasASegundos(tiempo1);
        long segundos2 = formatoHorasASegundos(tiempo2);
        return segundosAFormatoHoras(segundos1 + segundos2);
    }
}
