package com.praxis.sandbox_spei.model;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Entity
@Table(name = "operaciones")
public class Operacion {

    @Id
    private String id = "op_" + UUID.randomUUID().toString().replace("-", "")
            .substring(0, 26).toUpperCase();

    @Column(nullable = false, unique = true, length = 30)
    private String referenciaDeSegimiento; // V11-V12

    @Enumerated(EnumType.STRING)
    private TipoOperacion tipoOperacion; // V13

    @Column(precision = 12, scale = 2)
    private BigDecimal importeValor; // V06-V07

    private String importeDivisa;

    @Column(length = 40)
    private String concepto; // V09

    private Integer folioNumerico; // V10

    // Para el emisor de T2T
    private String emisorInstitucion; //V04
    private String emisorCuenta; // V03, V14, V17
    private String emisorNombre;      // V18
    private String emisorIdentificacionFiscal; // campo opcional del YAML

    // Para emisor VNT unicamente
    private String emisorSucursal; // V16
    private String emisorDocumentoTipo;
    private String emisorDocumentoNumero;

    // Receptor (siempre)
    private String receptorInstitucion; // V04
    private String receptorCuenta;      // V01-V02
    private String receptorNombre;      // V18


    @Enumerated(EnumType.STRING)
    private EstadoOperacion estado = EstadoOperacion.RECIBIDO;

    private String motivo; // PXR-020, PXR-21.... para el DEVUELTO

    private Instant fechaRegistro = Instant.now();
    private Instant fechaActualizacion = Instant.now();


    @OneToMany(mappedBy = "operacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("momento ASC")
    private List<Transicion> transiciones = new ArrayList<>();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReferenciaDeSegimiento() {
        return referenciaDeSegimiento;
    }

    public void setReferenciaDeSegimiento(String referenciaDeSegimiento) {
        this.referenciaDeSegimiento = referenciaDeSegimiento;
    }

    public TipoOperacion getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(TipoOperacion tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public BigDecimal getImporteValor() {
        return importeValor;
    }

    public void setImporteValor(BigDecimal importeValor) {
        this.importeValor = importeValor;
    }

    public String getImporteDivisa() {
        return importeDivisa;
    }

    public void setImporteDivisa(String importeDivisa) {
        this.importeDivisa = importeDivisa;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Integer getFolioNumerico() {
        return folioNumerico;
    }

    public void setFolioNumerico(Integer folioNumerico) {
        this.folioNumerico = folioNumerico;
    }

    public String getEmisorInstitucion() {
        return emisorInstitucion;
    }

    public void setEmisorInstitucion(String emisorInstitucion) {
        this.emisorInstitucion = emisorInstitucion;
    }

    public String getEmisorCuenta() {
        return emisorCuenta;
    }

    public void setEmisorCuenta(String emisorCuenta) {
        this.emisorCuenta = emisorCuenta;
    }

    public String getEmisorNombre() {
        return emisorNombre;
    }

    public void setEmisorNombre(String emisorNombre) {
        this.emisorNombre = emisorNombre;
    }

    public String getEmisorIdentificacionFiscal() {
        return emisorIdentificacionFiscal;
    }

    public void setEmisorIdentificacionFiscal(String emisorIdentificacionFiscal) {
        this.emisorIdentificacionFiscal = emisorIdentificacionFiscal;
    }

    public String getEmisorSucursal() {
        return emisorSucursal;
    }

    public void setEmisorSucursal(String emisorSucursal) {
        this.emisorSucursal = emisorSucursal;
    }

    public String getEmisorDocumentoTipo() {
        return emisorDocumentoTipo;
    }

    public void setEmisorDocumentoTipo(String emisorDocumentoTipo) {
        this.emisorDocumentoTipo = emisorDocumentoTipo;
    }

    public String getEmisorDocumentoNumero() {
        return emisorDocumentoNumero;
    }

    public void setEmisorDocumentoNumero(String emisorDocumentoNumero) {
        this.emisorDocumentoNumero = emisorDocumentoNumero;
    }

    public String getReceptorInstitucion() {
        return receptorInstitucion;
    }

    public void setReceptorInstitucion(String receptorInstitucion) {
        this.receptorInstitucion = receptorInstitucion;
    }

    public String getReceptorCuenta() {
        return receptorCuenta;
    }

    public void setReceptorCuenta(String receptorCuenta) {
        this.receptorCuenta = receptorCuenta;
    }

    public String getReceptorNombre() {
        return receptorNombre;
    }

    public void setReceptorNombre(String receptorNombre) {
        this.receptorNombre = receptorNombre;
    }

    public EstadoOperacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoOperacion estado) {
        this.estado = estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Instant fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Instant fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public List<Transicion> getTransiciones() {
        return transiciones;
    }

    public void setTransiciones(List<Transicion> transiciones) {
        this.transiciones = transiciones;
    }
}
