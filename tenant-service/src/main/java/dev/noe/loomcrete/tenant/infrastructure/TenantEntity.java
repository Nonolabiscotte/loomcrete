package dev.noe.loomcrete.tenant.infrastructure;

import dev.noe.loomcrete.domain.Tenant;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapping the domain Tenant record to the database.
 * Uses string ID (UUID) as primary key.
 */
@Entity
@Table(name = "tenants")
public class TenantEntity {
    @Id
    @Column(name = "id", length = 36)
    public String id;

    @Column(name = "name", nullable = false, length = 255)
    public String name;

    @Column(name = "config", columnDefinition = "TEXT")
    public String config;

    public TenantEntity() {
    }

    public TenantEntity(String id, String name, String config) {
        this.id = id;
        this.name = name;
        this.config = config;
    }

    public static TenantEntity from(Tenant domain) {
        return new TenantEntity(domain.id(), domain.name(), domain.config());
    }

    public Tenant toDomain() {
        return new Tenant(id, name, config);
    }
}
