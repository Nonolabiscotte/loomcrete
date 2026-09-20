package dev.noe.loomcrete.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantTest {

    @Test
    void testCreateTenant() {
        Tenant tenant = Tenant.create("ACME Corp", "config-data");

        assertEquals("ACME Corp", tenant.name());
        assertEquals("config-data", tenant.config());
        assertNotNull(tenant.id());
    }

    @Test
    void testCreateTenantWithNullConfig() {
        Tenant tenant = Tenant.create("ACME Corp", null);

        assertEquals("ACME Corp", tenant.name());
        assertEquals("", tenant.config());
        assertNotNull(tenant.id());
    }

    @Test
    void testTenantWithInvalidName() {
        assertThrows(IllegalArgumentException.class,
            () -> Tenant.create(null, "config"));

        assertThrows(IllegalArgumentException.class,
            () -> Tenant.create("", "config"));
    }

    @Test
    void testTenantImmutability() {
        Tenant tenant = new Tenant("id-1", "ACME Corp", "config");

        assertEquals("id-1", tenant.id());
        assertEquals("ACME Corp", tenant.name());
        assertEquals("config", tenant.config());

        // Records are immutable by nature
        Tenant tenant2 = new Tenant("id-1", "ACME Corp", "config");
        assertEquals(tenant, tenant2);
    }

}
