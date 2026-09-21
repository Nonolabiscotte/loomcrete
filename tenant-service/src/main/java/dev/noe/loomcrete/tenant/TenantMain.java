package dev.noe.loomcrete.tenant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/tenants")
public class TenantMain {

    @GET
    @Path("health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        return Response.ok()
            .entity("{\"status\":\"UP\",\"service\":\"tenant-service\"}")
            .build();
    }

    @GET
    @Path("health/ready")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ready() {
        return Response.ok()
            .entity("{\"status\":\"READY\",\"service\":\"tenant-service\"}")
            .build();
    }

}
