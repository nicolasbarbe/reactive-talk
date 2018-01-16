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

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;


import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.Builder;
import lombok.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@SpringBootApplication
@Configuration
public class Service {

    private final Flux<Long> fibonacci = Flux.interval(Duration.ofSeconds(2)).scan( Tuples.of(0l, 1l), (pair, i) -> Tuples.of( pair.getT2(), pair.getT1() + pair.getT2())).map(pair -> pair.getT2());

    private final Flux<Long> hotFlux = fibonacci.publish().autoConnect(0);

    private final WebClient us_service   = WebClient.builder().baseUrl("https://rus.blob.core.windows.net/reactive").build(),
                            asia_service = WebClient.builder().baseUrl("https://rasia.blob.core.windows.net/reactive").build(),
                            eu_service   = WebClient.builder().baseUrl("https://reu.blob.core.windows.net/reactive").build();


    public static void main(String[] args) {
        SpringApplication.run(info.magnolia.talks.reactive.resilient.Service.class, args);
    }

    ///
    /// Clients
    ///
    WebClient recommendations_service = WebClient.builder().baseUrl("http://localhost:8004").build();

    ///
    /// Router
    ///
    @Bean
    protected RouterFunction<ServerResponse> routes() {
        return route(

                path("/foo"),  this::foo      ).andRoute(
                path("/bar"),  this::bar      ).andRoute(

                path("/fibonacci-cold"), this::coldFlux ).andRoute(
                path("/fibonacci-hot") , this::hotFlux  ).andRoute(
                        
                path("/suggestions"), this::suggestions ).andRoute(

                path("/fastest"), this::fastestResponder );
    }

    ///
    /// Handlers
    ///
    private Mono<ServerResponse> foo(ServerRequest request) {
        return null;
    }

    private Mono<ServerResponse> bar(ServerRequest request) {
        return null;
    }

    private Mono<ServerResponse> coldFlux(ServerRequest request) {
        return null;
    }

    private Mono<ServerResponse> hotFlux(ServerRequest request) {
        return null;
    }

    private Mono<ServerResponse> fastestResponder(ServerRequest request) {

        return null;
    }

    /**
     * For a given username, returns a list of suggested books
     *
     * 1. The method returns a personalized list of books provided by the recommendation service
     *
     * 2. If the recommendation service returns an empty list, the method returns the best-sellers
     *
     * 3. The list must be limited to 5 elements maximum
     *
     * 4. If the user is not known by the recommendation service, the method returns the best-sellers
     *
     * 5. The method should return the resulting list within 100ms
     *
     */
    private Mono<ServerResponse> suggestions(ServerRequest request) {
        String username = request.cookies().getFirst("username").getValue();

        Flux<Book> suggestions = null;

        return ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(suggestions, Book.class);
    }


    private Flux<Book> bestSellers() {
        return Flux.just(
                Book.builder().title("A Dark Lure").authors("Loreth Anne White").build(),
                Book.builder().title( "Harry Potter and the Sorcerer's Stone").authors("J.K. Rowling, Mary GrandPré").build()
        );
    }

    private Flux<Book> cachedRecommendations(String username) {
        if("jdoe".equals(username))
            return Flux.just(
                Book.builder().title("Release It! Second Edition").authors("Michael Nygard").build(),
                Book.builder().title("Production-Ready Microservices").authors("Susan J. Fowler").build()
            );
        else
            return Flux.empty();
    }

}

@Value
@Builder
class Book {
    private String title;
    private String authors;
}