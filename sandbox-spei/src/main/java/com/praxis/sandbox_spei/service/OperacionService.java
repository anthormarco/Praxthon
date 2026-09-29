package com.praxis.sandbox_spei.service;

import com.praxis.sandbox_spei.dto.OperacionDtos.*;
import com.praxis.sandbox_spei.model.*;
import com.praxis.sandbox_spei.repository.ClaveRepository;
import com.praxis.sandbox_spei.repository.OperacionRepository;
import com.praxis.sandbox_spei.repository.TransicionRepository;
import com.praxis.sandbox_spei.util.ClabeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;

@Service
public class OperacionService {

    private final OperacionRepository operacionRepo;
    private final TransicionRepository transicionRepo;
    private final ClaveRepository claveRepo;
    private final ValidacionService validacionService;

    public OperacionService(OperacionRepository operacionRepo, TransicionRepository transicionRepo,
                            ClaveRepository claveRepo, ValidacionService validacionService) {
        this.operacionRepo = operacionRepo;
        this.transicionRepo = transicionRepo;
        this.claveRepo = claveRepo;
        this.validacionService = validacionService;
    }

    // ========== CREACIÓN CON IDEMPOTENCIA  ==========
    @Transactional
    public ResultadoCreacion crear(
            String claveIdempotencia,
            String bodyRawParaHash, // el JSON crudo que te llega para calcular SHA256
            String referenciaSeguimiento,
            String tipoOperacion,
            ImporteDTO importe,
            String concepto,
            Integer folioNumerico,
            EmisorT2TDTO emisorT2T,
            EmisorVNTDTO emisorVNT,
            ReceptorDTO receptor
    ) {
        // 1. Validar hash de idempotencia antes de tocar BD
        String hashNuevo = sha256(bodyRawParaHash);

        if (claveIdempotencia != null) {
            var claveExistente = claveRepo.findById(claveIdempotencia).orElse(null);
            if (claveExistente != null) {
                if (!claveExistente.getHashCuerpo().equals(hashNuevo)) {
                    // Cuerpo distinto con misma clave → 409 PRX-015
                    return new ResultadoCreacion(409, null, List.of(
                            new ErrorDTO("PRX-015", "Clave-Idempotencia", "Clave ya usada con cuerpo distinto")
                    ));
                }
                // Mismo cuerpo, misma clave → 200 devolver original, no crear duplicado
                var opOriginal = operacionRepo.findById(claveExistente.getOperacionId()).orElse(null);
                if (opOriginal != null) {
                    return new ResultadoCreacion(200, aDto(opOriginal), List.of());
                }
            }
        }

        // 2. Validaciones V01-V19
        boolean refDuplicada = operacionRepo.existsByReferenciaSeguimiento(referenciaSeguimiento);
        List<ErrorDTO> errores = validacionService.validar(
                referenciaSeguimiento, tipoOperacion, importe, concepto, folioNumerico,
                emisorT2T, emisorVNT, receptor, refDuplicada
        );

        if (!errores.isEmpty()) {
            return new ResultadoCreacion(422, null, errores);
        }

        // 3. Crear entidad Operacion
        Operacion op = new Operacion();
        op.setReferenciaSeguimiento(referenciaSeguimiento);
        op.setTipoOperacion(TipoOperacion.valueOf(tipoOperacion));
        op.setImporteValor(importe.valor());
        op.setImporteDivisa(importe.divisa() != null ? importe.divisa() : "MXN");
        op.setConcepto(concepto);
        op.setFolioNumerico(folioNumerico);

        if ("T2T".equals(tipoOperacion)) {
            op.setEmisorInstitucion(emisorT2T.institucion());
            op.setEmisorCuenta(emisorT2T.cuenta());
            op.setEmisorNombre(emisorT2T.nombre());
            op.setEmisorIdentificacionFiscal(emisorT2T.identificacionFiscal());
        } else {
            op.setEmisorInstitucion(emisorVNT.institucion());
            op.setEmisorSucursal(emisorVNT.sucursal());
            op.setEmisorNombre(emisorVNT.nombre());
            op.setEmisorDocumentoTipo(emisorVNT.documentoIdentidad().tipo());
            op.setEmisorDocumentoNumero(emisorVNT.documentoIdentidad().numero());
        }

        op.setReceptorInstitucion(receptor.institucion());
        op.setReceptorCuenta(receptor.cuenta());
        op.setReceptorNombre(receptor.nombre());
        op.setEstado(EstadoOperacion.RECIBIDO);
        op.setFechaRegistro(Instant.now());
        op.setFechaActualizacion(Instant.now());

        operacionRepo.save(op);

        // 4. Guardar primera transición RECIBIDO
        Transicion t0 = new Transicion(op, EstadoOperacion.RECIBIDO, null);
        transicionRepo.save(t0);

        // 5. Guardar clave idempotencia con hash
        if (claveIdempotencia != null) {
            claveRepo.save(new ClaveIdempotencia(claveIdempotencia, op.getId(), hashNuevo));
        }

        // 6. Simular motor determinista S01-S06
        simularMotor(op);

        return new ResultadoCreacion(201, aDto(op), List.of());
    }

    // ========== MOTOR DETERMINISTA §5.6 ==========
    // Esta es la parte se evalua con casos A13-A21
    private void simularMotor(Operacion op) {
        String cuentaDestino = op.getReceptorCuenta();
        String institucionDestino = op.getReceptorInstitucion();

        // Dígitos 14-17 = substraer 13-16 (0-index)
        String digitos14a17 = cuentaDestino.substring(13, 17);
        int escenario = Integer.parseInt(digitos14a17);

        // S01: RECIBIDO -> EN_PROCESO (siempre pasa si pasó las validaciones)
        transitar(op, EstadoOperacion.EN_PROCESO, null);

        // S02-S06 según últimos 4 dígitos
        if (escenario == 9002) {
            // En liquidación - se queda en EN_PROCESO (A13)
            return;
        } else if (escenario == 9003) {
            // Liquidado con éxito - A14
            transitar(op, EstadoOperacion.LIQUIDADO, null);
        } else if (escenario == 9004) {
            // Devuelto - A15
            transitar(op, EstadoOperacion.DEVUELTO, "PRX-020");
            op.setMotivo("PRX-020");
        } else if (escenario == 9005) {
            // Rechazado - A16
            transitar(op, EstadoOperacion.RECHAZADO, "PRX-021");
            op.setMotivo("PRX-021");
        } else if (escenario == 9006) {
            // En investigación - A17
            transitar(op, EstadoOperacion.EN_INVESTIGACION, null);
        } else if (CatalogoInstituciones.enMantenimiento(institucionDestino)) {
            // S04 institución no disponible - A20
            transitar(op, EstadoOperacion.DEVUELTO, "PRX-022");
            op.setMotivo("PRX-022");
        } else {
            // Caso por defecto: 9003 = liquidado
            transitar(op, EstadoOperacion.LIQUIDADO, null);
        }

        operacionRepo.save(op);
    }

    private void transitar(Operacion op, EstadoOperacion nuevo, String motivo) {
        op.setEstado(nuevo);
        op.setFechaActualizacion(Instant.now());
        transicionRepo.save(new Transicion(op, nuevo, motivo));
    }

    private String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return raw.hashCode() + ""; }
    }

    private OperacionDTO aDto(Operacion op) {
        var trans = transicionRepo.findByOperacionIdOrderByMomentoAsc(op.getId())
                .stream().map(t -> new TransicionDTO(t.getEstado().name(), t.getMomento(), t.getMotivo())).toList();

        return new OperacionDTO(
                op.getId(),
                op.getReferenciaSeguimiento(),
                op.getEstado().name(),
                op.getTipoOperacion().name(),
                new ImporteDTO(op.getImporteValor(), op.getImporteDivisa()),
                op.getFechaRegistro(),
                trans
        );
    }

    // Wrapper para devolver 201/200/422/409 desde el mismo servicio
    public record ResultadoCreacion(int status, OperacionDTO operacion, List<ErrorDTO> errores) {}

    // Para GET /operaciones/{id}
    public OperacionDTO buscarPorId(String id) {
        return operacionRepo.findById(id).map(this::aDto).orElse(null);
    }

    // Para GET /operaciones?page=...
    public PaginaOperacionesDTO listar(int pagina, int tamano) {
        var page = operacionRepo.findAll(
                org.springframework.data.domain.PageRequest.of(pagina, tamano,
                        org.springframework.data.domain.Sort.by("fechaRegistro").descending())
        );
        var dtos = page.getContent().stream().map(this::aDto).toList();
        return new PaginaOperacionesDTO(dtos, pagina, tamano, page.getTotalElements(), page.getTotalPages());
    }
}