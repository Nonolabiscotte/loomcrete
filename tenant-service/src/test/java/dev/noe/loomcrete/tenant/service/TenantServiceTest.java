package dev.noe.loomcrete.tenant.service;

import dev.noe.loomcrete.tenant.infrastructure.TenantEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TenantService with virtual threads.
 * Tests verify that service methods work correctly when dispatched to virtual threads.
 */
@QuarkusTest
class TenantServiceTest {

    @Inject
    TenantService tenantService;

    @Test
    void testGetTenantById_NotFound() {
        Optional<TenantEntity> result = tenantService.getTenantById("nonexistent-id");
        assertFalse(result.isPresent(), "Should return empty Optional for non-existent tenant");
    }

    @Test
    @Transactional
    void testPersistAndRetrieveTenant() {
        // Arrange
        TenantEntity tenant = new TenantEntity();
        tenant.id = "tenant-1";
        tenant.name = "Test Tenant";

        // Act: Persist via service
        tenantService.persistTenant(tenant);

        // Assert: Retrieve via service
        Optional<TenantEntity> retrieved = tenantService.getTenantById("tenant-1");
        assertTrue(retrieved.isPresent(), "Should find persisted tenant");
        assertEquals("Test Tenant", retrieved.get().name);
    }

    @Test
    @Transactional
    void testDeleteTenantById() {
        // Arrange
        TenantEntity tenant = new TenantEntity();
        tenant.id = "tenant-to-delete";
        tenant.name = "Tenant to Delete";

        tenantService.persistTenant(tenant);
        assertTrue(tenantService.getTenantById("tenant-to-delete").isPresent(), "Should exist before deletion");

        // Act
        tenantService.deleteTenantById("tenant-to-delete");

        // Assert
        assertFalse(tenantService.getTenantById("tenant-to-delete").isPresent(), "Should not exist after deletion");
    }
}
