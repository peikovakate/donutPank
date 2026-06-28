package com.donutpank.bank.currency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "currencies")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {

    @Id
    @NonNull
    @Column(length = 3)
    private String code;

    @NonNull
    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "minor_unit", nullable = false)
    private short minorUnit;

    @Column(nullable = false)
    private boolean active;
}
