package com.praxis.sandbox_spei.service;

import com.praxis.sandbox_spei.dto.OperacionDtos.*;
import com.praxis.sandbox_spei.util.ClabeUtil;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidacionService {

    public List<ErrorDTO> validar(
            String referenciaSeguimiento,
            String tipoOperacion,
            ImporteDTO importe,
            String concepto,
            Integer folioNumerico,
            EmisorT2TDTO emisorT2T,
            EmisorVNTDTO emisorVNT,
            ReceptorDTO receptor,
            boolean referenciaDuplicadaEnBD // V12
    ) {
        List<ErrorDTO> errores = new ArrayList<>();

        // V11 y V12 - referencia
        if (referenciaSeguimiento == null || referenciaSeguimiento.isBlank()) {
            errores.add(new ErrorDTO("PRX-010", "referenciaSeguimiento", "La referenciaSeguimiento es requerida"));
        } else {
            if (!referenciaSeguimiento.matches("^[A-Z0-9]{10,30}$")) {
                // V11: alfanumérica mayúsculas, 10-30 → PRX-010
                errores.add(new ErrorDTO("PRX-010", "referenciaSeguimiento", "Formato de referenciaSeguimiento inválido: solo A-Z, 0-9, 10-30 chars"));
            }
            if (referenciaDuplicadaEnBD) {
                errores.add(new ErrorDTO("PRX-010", "referenciaSeguimiento", "referenciaSeguimiento ya registrada"));
            }
        }

        // V13 - tipoOperacion
        if (tipoOperacion == null || (!tipoOperacion.equals("T2T") && !tipoOperacion.equals("VNT"))) {
            errores.add(new ErrorDTO("PRX-011", "tipoOperacion", "tipoOperacion debe ser T2T o VNT"));
        }

        // V01 y V02 - receptor.cuenta
        if (receptor == null || receptor.cuenta() == null) {
            errores.add(new ErrorDTO("PRX-001", "receptor.cuenta", "receptor.cuenta es requerida"));
        } else {
            if (!receptor.cuenta().matches("^[0-9]{18}$")) {
                errores.add(new ErrorDTO("PRX-001", "receptor.cuenta", "receptor.cuenta debe tener 18 dígitos"));
            } else if (!ClabeUtil.valida(receptor.cuenta())) {
                errores.add(new ErrorDTO("PRX-002", "receptor.cuenta", "Dígito verificador de CLABE receptor inválido"));
            }
        }

        // V03 y V14 - emisor T2T cuenta
        if ("T2T".equals(tipoOperacion)) {
            if (emisorT2T == null || emisorT2T.cuenta() == null) {
                errores.add(new ErrorDTO("PRX-001", "emisor.cuenta", "emisor.cuenta es requerida para T2T"));
            } else {
                if (!emisorT2T.cuenta().matches("^[0-9]{18}$")) {
                    errores.add(new ErrorDTO("PRX-001", "emisor.cuenta", "emisor.cuenta debe tener 18 dígitos"));
                } else if (!ClabeUtil.valida(emisorT2T.cuenta())) {
                    errores.add(new ErrorDTO("PRX-002", "emisor.cuenta", "Dígito verificador de CLABE emisor inválido"));
                }
            }
            // V14: emisor.cuenta prohibida en VNT se valida en el otro if
        }

        // V04 - instituciones existen
        if (receptor != null && receptor.institucion() != null) {
            if (!CatalogoInstituciones.existe(receptor.institucion())) {
                errores.add(new ErrorDTO("PRX-003", "receptor.institucion", "Institución receptora no existe: " + receptor.institucion()));
            }
        } else if (receptor != null) {
            errores.add(new ErrorDTO("PRX-003", "receptor.institucion", "receptor.institucion es requerida"));
        }

        if ("T2T".equals(tipoOperacion) && emisorT2T != null) {
            if (emisorT2T.institucion() == null) {
                errores.add(new ErrorDTO("PRX-003", "emisor.institucion", "emisor.institucion es requerida"));
            } else if (!CatalogoInstituciones.existe(emisorT2T.institucion())) {
                errores.add(new ErrorDTO("PRX-003", "emisor.institucion", "Institución emisora no existe"));
            }
        }
        if ("VNT".equals(tipoOperacion) && emisorVNT != null) {
            if (emisorVNT.institucion() == null) {
                errores.add(new ErrorDTO("PRX-003", "emisor.institucion", "emisor.institucion es requerida"));
            } else if (!CatalogoInstituciones.existe(emisorVNT.institucion())) {
                errores.add(new ErrorDTO("PRX-003", "emisor.institucion", "Institución emisora no existe"));
            }
        }

        // V06 y V07 - importe
        if (importe == null || importe.valor() == null) {
            errores.add(new ErrorDTO("PRX-005", "importe.valor", "importe.valor es requerido"));
        } else {
            if (importe.valor().compareTo(new BigDecimal("0.01")) < 0) {
                errores.add(new ErrorDTO("PRX-005", "importe.valor", "importe.valor debe ser >= 0.01"));
            }
            if (importe.valor().scale() > 2) {
                errores.add(new ErrorDTO("PRX-006", "importe.valor", "importe.valor no debe tener más de 2 decimales"));
            }
        }

        // V08 - divisa
        if (importe != null && importe.divisa() != null && !"MXN".equals(importe.divisa())) {
            errores.add(new ErrorDTO("PRX-007", "importe.divisa", "Solo se permite MXN"));
        }

        // V09 - concepto max 40
        if (concepto != null && concepto.length() > 40) {
            errores.add(new ErrorDTO("PRX-008", "concepto", "concepto max 40 caracteres"));
        }

        // V10 - folio numerico 0-9999999
        if (folioNumerico != null && (folioNumerico < 0 || folioNumerico > 9999999)) {
            errores.add(new ErrorDTO("PRX-009", "folioNumerico", "folioNumerico debe estar entre 0 y 9999999"));
        }

        // V15, V16, V17 - condicionales T2T/VNT
        if ("VNT".equals(tipoOperacion)) {
            if (emisorT2T != null && emisorT2T.cuenta() != null) {
                errores.add(new ErrorDTO("PRX-012", "emisor.cuenta", "emisor.cuenta no debe venir en VNT"));
            }
            if (emisorVNT == null || emisorVNT.sucursal() == null || emisorVNT.sucursal().isBlank()) {
                errores.add(new ErrorDTO("PRX-013", "emisor.sucursal", "emisor.sucursal es requerida para VNT"));
            }
            if (emisorVNT == null || emisorVNT.documentoIdentidad() == null) {
                errores.add(new ErrorDTO("PRX-014", "emisor.documentoIdentidad", "documentoIdentidad requerido para VNT"));
            }
        }

        // V18 - nombres con caracteres válidos
        if (receptor != null && receptor.nombre() != null) {
            if (!receptor.nombre().matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
                errores.add(new ErrorDTO("PRX-018", "receptor.nombre", "receptor.nombre contiene caracteres no válidos"));
            }
        }

        return errores;
    }
}