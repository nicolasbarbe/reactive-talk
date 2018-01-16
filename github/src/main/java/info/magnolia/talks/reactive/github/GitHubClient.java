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
package info.magnolia.talks.reactive.github;


import java.util.Comparator;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;


public class GitHubClient {

    private WebClient client;

    /**
     * Create a webclient in constructor that connects to https://api.github.com
     * Authorization token must follow the format "token %s"
     */
    public GitHubClient(String token) {
        
        this.client = null;
    }

    /**
     * List repositories for a given user
     * uri: "/users/{username}/repos"
     */
    public Flux<Repository> listRepositories(String user) {

        return Flux.empty();
    }

    /**
     * List contributors for a given git repo
     * uri: "/repos/{owner}/{repo}/contributors"
     */
    public Flux<Contributor> listContributors(String owner, String repo) {

        return Flux.empty();
    }


    /**
     * List the top10 contributors on a project
     * Returns a flux of pair of contributor x contributions count
     * Result must be sorted (top contributor first)
     * Result must returns 10 element maximum
     */
    public Flux<Tuple2<String, Integer>> top10Contributors(String owner) {
        return this.listRepositories(owner)
                .flatMap( repo -> this.listContributors(owner, repo.getName()) )
                .groupBy( contributor -> contributor.getLogin(), contributor -> contributor.getContributions() )
                .flatMap( group -> group
                        .reduce( (acc, curr) -> acc + curr )
                        .map( count -> Tuples.of( group.key(), count)))
                .sort( Comparator.comparing( Tuple2<String, Integer>::getT2 ).reversed())
                .take(10);
    }
}

