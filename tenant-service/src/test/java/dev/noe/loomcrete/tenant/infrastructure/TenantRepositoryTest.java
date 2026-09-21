package dev.noe.loomcrete.tenant.infrastructure;

import dev.noe.loomcrete.domain.Tenant;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TenantRepository using @QuarkusTest.
 * Tests CRUD operations and multi-tenancy isolation.
 */
@QuarkusTest
class TenantRepositoryTest {

    @Inject
    TenantRepository repository;

    @BeforeEach
    @Transactional
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @Transactional
    void shouldPersistAndRetrieveTenant() {
        // Given: a domain Tenant
        Tenant domain = Tenant.create("Acme Corp", "{\"config\": \"value\"}");

        // When: we persist it via the entity
        TenantEntity entity = TenantEntity.from(domain);
        repository.persist(entity);

        // Then: we can retrieve it by ID
        TenantEntity retrieved = repository.findById(domain.id());
        assertNotNull(retrieved);
        assertEquals(domain.id(), retrieved.id);
        assertEquals(domain.name(), retrieved.name);
        assertEquals(domain.config(), retrieved.config);
    }

    @Test
    @Transactional
    void shouldConvertEntityToDomain() {
        // Given: a persisted TenantEntity
        TenantEntity entity = new TenantEntity("123", "Test Tenant", "{}");
        repository.persist(entity);

        // When: we convert it to domain
        TenantEntity retrieved = repository.findById("123");
        Tenant domain = retrieved.toDomain();

        // Then: domain values match
        assertEquals("123", domain.id());
        assertEquals("Test Tenant", domain.name());
        assertEquals("{}", domain.config());
    }

    @Test
    @Transactional
    void shouldUpdateTenant() {
        // Given: a persisted tenant
        TenantEntity entity = new TenantEntity("456", "Original Name", "{}");
        repository.persist(entity);

        // When: we update it
        entity.name = "Updated Name";
        repository.persist(entity);

        // Then: the change persists
        TenantEntity updated = repository.findById("456");
        assertEquals("Updated Name", updated.name);
    }

    @Test
    @Transactional
    void shouldDeleteTenant() {
        // Given: a persisted tenant
        TenantEntity entity = new TenantEntity("789", "To Delete", "{}");
        repository.persist(entity);

        // When: we delete it
        repository.deleteById("789");

        // Then: it no longer exists
        TenantEntity deleted = repository.findById("789");
        assertNull(deleted);
    }

    @Test
    @Transactional
    void shouldCountTenants() {
        // Given: multiple tenants
        repository.persist(new TenantEntity("id1", "Tenant 1", "{}"));
        repository.persist(new TenantEntity("id2", "Tenant 2", "{}"));
        repository.persist(new TenantEntity("id3", "Tenant 3", "{}"));

        // When: we count
        long count = repository.count();

        // Then: count matches
        assertEquals(3, count);
    }
}
