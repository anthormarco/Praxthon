package com.praxis.sandbox_spei.model;


import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transiciones")
public class Transicion {

    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operacion_id", nullable = false)
    private Operacion operacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOperacion estado;

    private String motivo; // PRX-020, etc. nullable
    private Instant momento = Instant.now();
    public Transicion() {

    }
    public Transicion(Operacion operacion, EstadoOperacion estado, String motivo) {
        this.operacion = operacion;
        this.estado = estado;
        this.motivo = motivo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Operacion getOperacion() {
        return operacion;
    }

    public void setOperacion(Operacion operacion) {
        this.operacion = operacion;
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

    public Instant getMomento() {
        return momento;
    }

    public void setMomento(Instant momento) {
        this.momento = momento;
    }
}
