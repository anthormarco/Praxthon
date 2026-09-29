package com.praxis.sandbox_spei.repository;

import com.praxis.sandbox_spei.model.ClaveIdempotencia;
import com.praxis.sandbox_spei.model.Transicion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaveRepository extends JpaRepository<ClaveIdempotencia, String> {

}
