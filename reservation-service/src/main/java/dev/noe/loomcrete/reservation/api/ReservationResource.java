package dev.noe.loomcrete.reservation.api;

import dev.noe.loomcrete.reservation.api.dto.in.CreateReservationRequest;
import dev.noe.loomcrete.reservation.api.dto.out.ReservationResponse;
import dev.noe.loomcrete.reservation.api.dto.out.ErrorResponse;
import dev.noe.loomcrete.reservation.application.usecase.createReservation.CreateReservationUseCase;
import dev.noe.loomcrete.reservation.application.usecase.createReservation.CreateReservationResult;
import dev.noe.loomcrete.reservation.application.usecase.createReservation.CreateReservationSuccess;
import dev.noe.loomcrete.reservation.application.usecase.createReservation.CreateReservationFailure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@ApplicationScoped
@Path("/reservations")
@Produces(MediaType.APPLICATION_JSON)
public class ReservationResource {
    
    private static final Logger LOG = Logger.getLogger(ReservationResource.class);

    @Inject
    CreateReservationUseCase createReservationUseCase;

    @POST
    @Transactional
    public Response createReservation(CreateReservationRequest request) {
        var result = createReservationUseCase.execute(
            request.tenantId(),
            request.inventoryItemId(),
            request.quantity()
        );

        if (result instanceof CreateReservationSuccess success) {
            var confirmed = success.confirmed();
            var response = new ReservationResponse(
                confirmed.id(),
                confirmed.tenantId(),
                confirmed.inventoryItemId(),
                confirmed.quantity(),
                "CONFIRMED",
                confirmed.createdAt(),
                confirmed.confirmedAt()
            );
            return Response.status(201).entity(response).build();
        } else if (result instanceof CreateReservationFailure failure) {
            LOG.warnf("Reservation creation failed: %s", failure.error());
            return Response.status(400)
                .entity(new ErrorResponse(failure.error()))
                .build();
        }

        throw new IllegalStateException("Unknown result type: " + result.getClass());
    }
}
