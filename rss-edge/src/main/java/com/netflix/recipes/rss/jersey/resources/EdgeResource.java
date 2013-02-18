/*
 * 	Copyright 2012 Chris Fregly
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.netflix.recipes.rss.jersey.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chris Fregly (chris@fregly.com)
 */
@Path("")
public class EdgeResource {
    private static final Logger logger = LoggerFactory.getLogger(EdgeResource.class);

    public EdgeResource() {
    }

    @GET
    @Path("/dummy")
    public Response dummy() {
        try {
            return Response.ok("dummy", MediaType.TEXT_PLAIN).build();
        } catch (Exception ex) {
            return Response.serverError().build();
        }
    }
}