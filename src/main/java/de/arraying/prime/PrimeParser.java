package de.arraying.prime;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Copyright 2018 Arraying
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
final class PrimeParser {

    /**
     * The identifier for importing other code.
     */
    private static final String IDENTIFIER = "#include ";

    private final Prime prime;
    private final String initialCode;
    private final LinkedList<PrimeSourceProvider> providers;
    private final Set<String> sources;

    /**
     * Creates a new Prime parser.
     * @param prime The prime instance.
     * @param initialCode The initial code.
     * @param providers  The providers.
     */
    PrimeParser(Prime prime, String initialCode, LinkedList<PrimeSourceProvider> providers) {
        this(prime, initialCode, providers, new HashSet<>());
    }

    /**
     * Creates a new Prime parser.
     * @param prime The prime instance.
     * @param initialCode The initial code.
     * @param providers The providers.
     * @param sources A set of already imported paths.
     */
    private PrimeParser(Prime prime, String initialCode, LinkedList<PrimeSourceProvider> providers, Set<String> sources) {
        this.prime = prime;
        this.initialCode = initialCode;
        this.providers = providers;
        this.sources = sources;
    }

    /**
     * Parses the code and gives back the code to evaluate.
     * @return The code.
     */
    String parse() {
        String[] parts = initialCode.split("\n");
        StringBuilder resultBuilder = new StringBuilder();
        for(String part : parts) {
            if(part.startsWith(IDENTIFIER)) {
                part = part.substring(IDENTIFIER.length());
                Prime.Match match = Prime.Util.getMatches(prime, part);
                if(match == null) {
                    continue;
                }
                String result = match.getMatch();
                if(sources.contains(result)) {
                    continue;
                }
                String code = match.getProvider().getSource(result);
                sources.add(part);
                PrimeParser parser = new PrimeParser(prime, code, providers, sources);
                code = parser.parse();
                sources.addAll(parser.sources);
                resultBuilder.append(code);
                sources.add(part);
//                for(PrimeSourceProvider provider : providers) {
//                    Matcher matcher = provider.getIncludePattern().matcher(part);
//                    if(!matcher.find()) {
//                        continue;
//                    }
//                    String path = matcher.group();
//                    if(sources.contains(path)) {
//                        continue;
//                    }
//                    String code = provider.getSource(path);
//                    sources.add(part);
//                    PrimeParser parser = new PrimeParser(code, providers, sources);
//                    code = parser.parse();
//                    sources.addAll(parser.sources);
//                    resultBuilder.append(code);
//                    sources.add(part);
//                    break;
//                }
            } else {
                resultBuilder.append(part);
            }
            resultBuilder.append("\n");
        }
        return resultBuilder.toString().trim();
    }


}
