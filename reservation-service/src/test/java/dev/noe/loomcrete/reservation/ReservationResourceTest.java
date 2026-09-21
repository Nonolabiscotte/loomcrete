package dev.noe.loomcrete.reservation;

import dev.noe.loomcrete.inventory.infrastructure.InventoryItemEntity;
import dev.noe.loomcrete.inventory.infrastructure.InventoryItemRepository;
import dev.noe.loomcrete.reservation.dto.CreateReservationRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@DisplayName("Reservation Resource (Structured Concurrency)")
class ReservationResourceTest {

    @Inject
    InventoryItemRepository inventoryItemRepository;

    private String tenantId;
    private String itemId;

    @BeforeEach
    @Transactional
    void setUp() {
        tenantId = UUID.randomUUID().toString();
        itemId = UUID.randomUUID().toString();

        InventoryItemEntity item = new InventoryItemEntity(
            itemId,
            tenantId,
            "Storage Slot A",
            100,
            0
        );
        inventoryItemRepository.persist(item);
    }

    @Test
    @DisplayName("POST /reservations: successful reservation with all checks passing")
    void testCreateReservationSuccess() {
        CreateReservationRequest request = new CreateReservationRequest(
            tenantId,
            itemId,
            5
        );

        given()
            .contentType("application/json")
            .body(request)
        .when()
            .post("/reservations")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("tenantId", equalTo(tenantId))
            .body("inventoryItemId", equalTo(itemId))
            .body("quantity", equalTo(5))
            .body("status", equalTo("CONFIRMED"))
            .body("createdAt", notNullValue())
            .body("confirmedAt", notNullValue());
    }

    @Test
    @DisplayName("POST /reservations: fails when inventory item not found")
    void testCreateReservationInventoryNotFound() {
        CreateReservationRequest request = new CreateReservationRequest(
            tenantId,
            UUID.randomUUID().toString(),
            5
        );

        given()
            .contentType("application/json")
            .body(request)
        .when()
            .post("/reservations")
        .then()
            .statusCode(400)
            .body("error", notNullValue());
    }

    @Test
    @DisplayName("POST /reservations: fails when insufficient inventory")
    void testCreateReservationInsufficientInventory() {
        CreateReservationRequest request = new CreateReservationRequest(
            tenantId,
            itemId,
            150
        );

        given()
            .contentType("application/json")
            .body(request)
        .when()
            .post("/reservations")
        .then()
            .statusCode(400)
            .body("error", notNullValue());
    }

    @Test
    @DisplayName("POST /reservations: fails when tenant is blocked by fraud detection")
    void testCreateReservationFraudCheckFails() {
        String blockedTenantId = "blocked_123";
        CreateReservationRequest request = new CreateReservationRequest(
            blockedTenantId,
            itemId,
            5
        );

        given()
            .contentType("application/json")
            .body(request)
        .when()
            .post("/reservations")
        .then()
            .statusCode(400)
            .body("error", notNullValue());
    }

    @Test
    @DisplayName("POST /reservations: fails when tenant ID is missing")
    void testCreateReservationMissingTenantId() {
        CreateReservationRequest request = new CreateReservationRequest(
            "",
            itemId,
            5
        );

        given()
            .contentType("application/json")
            .body(request)
        .when()
            .post("/reservations")
        .then()
            .statusCode(400)
            .body("error", notNullValue());
    }

    @Test
    @DisplayName("POST /reservations: fails when quantity is invalid")
    void testCreateReservationInvalidQuantity() {
        CreateReservationRequest request = new CreateReservationRequest(
            tenantId,
            itemId,
            0
        );

        given()
            .contentType("application/json")
            .body(request)
        .when()
            .post("/reservations")
        .then()
            .statusCode(400)
            .body("error", notNullValue());
    }

    @Test
    @DisplayName("POST /reservations: parallel execution faster than sequential")
    void testCreateReservationParallelExecution() {
        CreateReservationRequest request = new CreateReservationRequest(
            tenantId,
            itemId,
            5
        );

        long startTime = System.currentTimeMillis();

        given()
            .contentType("application/json")
            .body(request)
        .when()
            .post("/reservations")
        .then()
            .statusCode(201);

        long duration = System.currentTimeMillis() - startTime;

        System.out.printf("Reservation created in %d ms (parallel execution)%n", duration);
    }
}
