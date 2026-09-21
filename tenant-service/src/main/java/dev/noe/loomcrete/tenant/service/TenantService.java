package dev.noe.loomcrete.tenant.service;

import dev.noe.loomcrete.tenant.infrastructure.TenantEntity;
import dev.noe.loomcrete.tenant.infrastructure.TenantRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Tenant business logic.
 *
 * Quarkus 3.8.6 automatically dispatches blocking JDBC calls to virtual threads.
 * No explicit annotation needed — this is the default behavior when using Panache
 * repositories with JDBC. This unlocks high concurrency without manual thread-pool
 * tuning (the core advantage of Java 21 virtual threads).
 */
@ApplicationScoped
public class TenantService {
    private static final Logger LOG = Logger.getLogger(TenantService.class);

    @Inject
    TenantRepository tenantRepository;

    /**
     * Retrieve a tenant by ID.
     * JDBC call is automatically dispatched to a virtual thread by Quarkus.
     */
    public Optional<TenantEntity> getTenantById(String id) {
        logThreadInfo("getTenantById");
        return tenantRepository.findByIdOptional(id);
    }

    /**
     * List all tenants.
     * JDBC call is automatically dispatched to a virtual thread by Quarkus.
     */
    public List<TenantEntity> listAllTenants() {
        logThreadInfo("listAllTenants");
        return tenantRepository.listAll();
    }

    /**
     * Persist a tenant entity.
     * JDBC write is automatically dispatched to a virtual thread by Quarkus.
     */
    public void persistTenant(TenantEntity entity) {
        logThreadInfo("persistTenant");
        tenantRepository.persist(entity);
    }

    /**
     * Delete a tenant by ID.
     * JDBC delete is automatically dispatched to a virtual thread by Quarkus.
     */
    public void deleteTenantById(String id) {
        logThreadInfo("deleteTenantById");
        tenantRepository.deleteById(id);
    }

    private void logThreadInfo(String methodName) {
        Thread currentThread = Thread.currentThread();
        String threadInfo = String.format(
            "method=%s thread=%s isVirtual=%s",
            methodName,
            currentThread.getName(),
            currentThread.isVirtual()
        );
        LOG.debug(threadInfo);
    }
}
