package com.praxis.sandbox_spei.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "clave_idempotencia")
public class ClaveIdempotencia {

    @Id
    private String clave; // viene del header Clave-Idempotencia

    @Column(nullable = false)
    private String operacionId;

    @Column(nullable = false, length = 64)
    private String hashCuerpo; // SHA-256 del body

    private Instant fechaCreacion =  Instant.now();

    public ClaveIdempotencia() {
    }

    public ClaveIdempotencia(String clave, String operacionId, String hashCuerpo) {
        this.clave = clave;
        this.operacionId = operacionId;
        this.hashCuerpo = hashCuerpo;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getOperacionId() {
        return operacionId;
    }

    public void setOperacionId(String operacionId) {
        this.operacionId = operacionId;
    }

    public String getHashCuerpo() {
        return hashCuerpo;
    }

    public void setHashCuerpo(String hashCuerpo) {
        this.hashCuerpo = hashCuerpo;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
