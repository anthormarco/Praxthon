package com.praxis.sandbox_spei.repository;

import com.praxis.sandbox_spei.model.Transicion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransicionRepository extends JpaRepository<Transicion, String> {

    // Para GET /operaciones/{id} que pide historial ordenado
    List<Transicion> findByOperacionIdOrderByMomentoAsc(String operacionId);

}
