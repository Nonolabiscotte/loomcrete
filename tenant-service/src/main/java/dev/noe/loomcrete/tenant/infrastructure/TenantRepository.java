package dev.noe.loomcrete.tenant.infrastructure;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Panache repository for Tenant persistence.
 * Provides CRUD operations and query methods for TenantEntity.
 * Uses String ID type to match domain model UUID identifiers.
 */
@ApplicationScoped
public class TenantRepository implements PanacheRepositoryBase<TenantEntity, String> {
}
