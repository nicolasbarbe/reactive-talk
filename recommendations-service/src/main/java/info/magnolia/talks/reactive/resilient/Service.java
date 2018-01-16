/**
 * This file Copyright (c) 2012-2014 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 */
package info.magnolia.talks.reactive.resilient;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.Builder;
import lombok.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Configuration
public class Service {

    private final Map<String, Flux<Book>> recommendations = new HashMap<String, Flux<Book>>();

    public Service() {
        recommendations.put("jdoe", Flux.just(
                Book.builder().title("The Devops Handbook").authors("Gene Kim, John Willis, Patrick Debois, John Allspaw").build(),
                Book.builder().title("Continuous Delivery: Reliable Software Releases through Build, Test, and Deployment Automation").authors("Jez Humble, David Farley").build(),
                Book.builder().title("Release It! Second Edition").authors("Michael Nygard").build(),
                Book.builder().title("Production-Ready Microservices").authors("Susan J. Fowler").build(),
                Book.builder().title("The Mythical Man-Month").authors("Frederick P. Brooks Jr.").build()));

        recommendations.put("cnorris", Flux.empty() );
    }

    public static void main(String[] args) {
        SpringApplication.run(info.magnolia.talks.reactive.resilient.Service.class, args);
    }

    ///
    /// Router
    ///
    @Bean
    protected RouterFunction<ServerResponse> routes() {
        return route( path("/user/{username}/recommendations"), this::getUserRecommendations);
    }

    ///
    /// Handler
    ///
    public Mono<ServerResponse> getUserRecommendations(ServerRequest request) {
        String username = request.pathVariable("username");
        if(this.recommendations.containsKey(username))
            return ok().body(recommendations.get(username), Book.class);
        else
            return  status(NOT_FOUND).body(fromObject("Cannot find user " + username));
    }


}

@Value
@Builder
class Book {
    private String title;
    private String authors;
}
