package com.praxis.sandbox_spei.service;

import com.praxis.sandbox_spei.model.Operacion;

import java.util.HashMap;
import java.util.Map;

public class CatalogoInstituciones {


    // Catalogo ficticio del emdpoint /catalogos/instituciones
    public static final Map<String, String> CATALOGO = Map.of(
            "801", "Banco Praxis Alfa",
            "802", "Banco Praxis Beta",
            "803", "Banco Praxis Gamma",
            "804", "Praxis Servicios de Pago",
            "805", "Banco Praxis Delta"
    );

    public static boolean existe(String codigo){
        // V04: si la institucion no existe -> PXR003
        return CATALOGO.containsKey(codigo);
    }

    public static boolean enMantenimiento(String codigo){
        // Este escenario A20 es para prueba si el key es 805 devuelve PXR-022 en mantenimiento
        return "805".equals(codigo);
    }
}
