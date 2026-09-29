package com.praxis.sandbox_spei.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OperacionDtos {
    // Corresponde a schema Importe
    public record ImporteDTO(BigDecimal valor, String divisa) {}

    // Corresponde a documentoIdentidad
    public record DocumentoIdentidadDTO(String tipo, String numero) {}

    // Emisor T2T
    public record EmisorT2TDTO(String institucion, String cuenta, String nombre, String identificacionFiscal) {}

    // Emisor VNT
    public record EmisorVNTDTO(String institucion, String sucursal, String nombre, DocumentoIdentidadDTO documentoIdentidad) {}

    // Receptor
    public record ReceptorDTO(String institucion, String cuenta, String nombre) {}

    // Respuesta de operación - lo que devuelve POST 201
    public record TransicionDTO(String estado, Instant momento, String motivo) {}

    public record OperacionDTO(
            String id,
            String referenciaSeguimiento,
            String estado,
            String tipoOperacion,
            ImporteDTO importe,
            Instant fechaRegistro,
            List<TransicionDTO> transiciones
    ) {}

    // Para GET /operaciones?page=0&size=20
    public record PaginaOperacionesDTO(
            List<OperacionDTO> contenido,
            int pagina,
            int tamano,
            long totalElementos,
            int totalPaginas
    ) {}

    // Errores
    public record ErrorDTO(String codigo, String campo, String mensaje) {}

    public record RespuestaErrorDTO(String referenciaSeguimiento, List<ErrorDTO> errores) {}
}
