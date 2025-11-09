package com.josephken.roors.reservation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dining_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiningTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String floor;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private DiningTableStatus status;
}


