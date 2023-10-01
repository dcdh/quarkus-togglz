/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkiverse.togglz.it;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.togglz.core.manager.FeatureManager;

@Path("/togglz")
@ApplicationScoped
public class TogglzResource {

    private final FeatureManager featureManager;

    public TogglzResource(final FeatureManager featureManager) {
        this.featureManager = Objects.requireNonNull(featureManager);
    }

    @GET
    @Path("{basicFeatures}")
    public Boolean isActive(@PathParam("basicFeatures") final BasicFeatures basicFeatures) {
        return this.featureManager.isActive(basicFeatures);
    }

    @POST
    @Path("{basicFeatures}/enable")
    public Boolean enable(@PathParam("basicFeatures") final BasicFeatures basicFeatures) {
        this.featureManager.enable(basicFeatures);
        return basicFeatures.isActive();
    }

    @POST
    @Path("{basicFeatures}/disable")
    public Boolean disable(@PathParam("basicFeatures") final BasicFeatures basicFeatures) {
        this.featureManager.disable(basicFeatures);
        return this.featureManager.isActive(basicFeatures);
    }
}
