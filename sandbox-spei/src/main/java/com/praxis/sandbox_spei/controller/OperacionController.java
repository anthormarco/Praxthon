package com.praxis.sandbox_spei.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.praxis.sandbox_spei.dto.OperacionDtos.*;
import com.praxis.sandbox_spei.service.CatalogoInstituciones;
import com.praxis.sandbox_spei.service.OperacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping
public class OperacionController {

    private final OperacionService operacionService;
    private final ObjectMapper mapper = new ObjectMapper();

    public OperacionController(OperacionService operacionService) {
        this.operacionService = operacionService;
    }

    // === POST /operaciones - Crear pago ===
    @PostMapping("/operaciones")
    public ResponseEntity<?> crearOperacion(
            @RequestHeader(value = "Clave-Idempotencia", required = false) String claveIdempotencia,
            @RequestBody String rawBody // Recibimos el raw para hash SHA256
    ) {
        try {
            JsonNode json = mapper.readTree(rawBody);

            String referencia = json.path("referenciaSeguimiento").asText(null);
            String tipoOp = json.path("tipoOperacion").asText(null);
            String concepto = json.path("concepto").asText(null);
            Integer folio = json.path("folioNumerico").isNumber() ? json.path("folioNumerico").asInt() : null;

            // Importe
            JsonNode importeNode = json.path("importe");
            ImporteDTO importe = null;
            if (!importeNode.isMissingNode()) {
                var valor = importeNode.path("valor").isNumber() ? importeNode.path("valor").decimalValue() : null;
                var divisa = importeNode.path("divisa").asText("MXN");
                importe = new ImporteDTO(valor, divisa);
            }

            // Receptor (siempre)
            JsonNode receptorNode = json.path("receptor");
            ReceptorDTO receptor = null;
            if (!receptorNode.isMissingNode()) {
                receptor = new ReceptorDTO(
                        receptorNode.path("institucion").asText(null),
                        receptorNode.path("cuenta").asText(null),
                        receptorNode.path("nombre").asText(null)
                );
            }

            // Emisor discriminado por tipoOperacion
            JsonNode emisorNode = json.path("emisor");
            EmisorT2TDTO emisorT2T = null;
            EmisorVNTDTO emisorVNT = null;

            if ("T2T".equals(tipoOp) && !emisorNode.isMissingNode()) {
                emisorT2T = new EmisorT2TDTO(
                        emisorNode.path("institucion").asText(null),
                        emisorNode.path("cuenta").asText(null),
                        emisorNode.path("nombre").asText(null),
                        emisorNode.path("identificacionFiscal").asText(null)
                );
            } else if ("VNT".equals(tipoOp) && !emisorNode.isMissingNode()) {
                JsonNode doc = emisorNode.path("documentoIdentidad");
                DocumentoIdentidadDTO docDto = null;
                if (!doc.isMissingNode()) {
                    docDto = new DocumentoIdentidadDTO(doc.path("tipo").asText(null), doc.path("numero").asText(null));
                }
                emisorVNT = new EmisorVNTDTO(
                        emisorNode.path("institucion").asText(null),
                        emisorNode.path("sucursal").asText(null),
                        emisorNode.path("nombre").asText(null),
                        docDto
                );
            }

            var resultado = operacionService.crear(
                    claveIdempotencia, rawBody, referencia, tipoOp, importe, concepto, folio,
                    emisorT2T, emisorVNT, receptor
            );

            if (resultado.status() == 201) {
                return ResponseEntity.status(201).body(resultado.operacion());
            } else if (resultado.status() == 200) {
                // Reintento idempotente exitoso
                return ResponseEntity.ok(resultado.operacion());
            } else if (resultado.status() == 409) {
                var errorResp = new RespuestaErrorDTO(referencia, resultado.errores());
                return ResponseEntity.status(409).body(errorResp);
            } else { // 422
                var errorResp = new RespuestaErrorDTO(referencia, resultado.errores());
                return ResponseEntity.unprocessableEntity().body(errorResp);
            }

        } catch (Exception e) {
            // JSON malformado → 400 como dice el YAML
            return ResponseEntity.badRequest().body(
                    Map.of("codigo", "PRX-000", "mensaje", "JSON inválido: " + e.getMessage())
            );
        }
    }

    // === GET /operaciones/{id} ===
    @GetMapping("/operaciones/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        var dto = operacionService.buscarPorId(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    // === GET /operaciones?page=0&size=20 ===
    @GetMapping("/operaciones")
    public ResponseEntity<PaginaOperacionesDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(operacionService.listar(page, size));
    }

    // === GET /catalogos/instituciones ===
    @GetMapping("/catalogos/instituciones")
    public ResponseEntity<?> catalogo() {
        var lista = CatalogoInstituciones.CATALOGO.entrySet().stream()
                .map(e -> Map.of("codigo", e.getKey(), "nombre", e.getValue()))
                .toList();
        return ResponseEntity.ok(Map.of("instituciones", lista));
    }

    // === GET /health ===
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
