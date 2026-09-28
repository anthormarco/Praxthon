package com.praxis.sandbox_spei.model;


import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "transiciones")
public class Transicion {

    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)


}
