package com.praxis.sandbox_spei.repository;

import com.praxis.sandbox_spei.model.Operacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperacionRepository extends JpaRepository<Operacion, String> {

    // Esto es para V12: referenciaSeguimiento no registrada previamente → PRX-010
    boolean existsByReferenciaSeguimiento(String referenciaSeguimiento);

}
