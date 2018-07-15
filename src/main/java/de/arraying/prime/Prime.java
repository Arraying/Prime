package de.arraying.prime;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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
@SuppressWarnings({"unused", "WeakerAccess"})
public final class Prime {

    private final String code;
    private final LinkedList<PrimeSourceProvider> providers;
    private final ScriptEngine engine;
    private final int maxRuntimeSeconds;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * Creates a new Prime instance.
     * @param code The code.
     * @param filter The filter.
     * @param variables The variables.
     * @param providers The providers.
     * @param maxRuntimeSeconds The maximum runtime in seconds.
     */
    private Prime(String code, PrimeClassFilter filter, Map<String, Object> variables, LinkedList<PrimeSourceProvider> providers, int maxRuntimeSeconds) {
        this.code = code;
        this.providers = providers;
        this.maxRuntimeSeconds = maxRuntimeSeconds;
        engine = new NashornScriptEngineFactory().getScriptEngine(filter);
        variables.forEach(engine::put);
    }

    /**
     * Evaluates the code with no error consumer.
     */
    public void evaluate() {
        evaluate(null);
    }

    /**
     * Evaluates the code.
     * @param error The consumer for when there is an error.
     */
    @SuppressWarnings("deprecation")
    public void evaluate(Consumer<Exception> error) {
        String codeRaw = new PrimeParser(this, this.code, providers).parse();
        String code = "(function(){" + codeRaw + "})();";
        PrimeRuntime runtime = new PrimeRuntime(engine, code, error);
        Thread thread = new Thread(runtime);
        thread.start();
        executor.schedule(() -> {
            if(!runtime.isCompleted()) {
                thread.stop();
            }
        }, maxRuntimeSeconds, TimeUnit.SECONDS);
    }

    /**
     * The builder.
     */
    public static final class Builder {

        private final PrimeClassFilter filter = new PrimeClassFilter();
        private final Map<String, Object> variables = new HashMap<>();
        private final LinkedList<PrimeSourceProvider> providers = new LinkedList<>();
        private int maxRuntimeSeconds = 3;

        /**
         * Exposes a class to the script.
         * @param clazz The class.
         * @return The builder, for chaining.
         */
        public Builder expose(Class<?> clazz) {
            filter.whitelist(clazz);
            return this;
        }

        /**
         * Registers a variable.
         * @param name The variable identifier.
         * @param value The value.
         * @return The builder, for chaining.
         */
        public Builder withVariable(String name, Object value) {
            variables.put(name, value);
            filter.whitelist(value.getClass());
            return this;
        }

        /**
         * Registers a source provider.
         * @param provider The provider.
         * @return The builder, for chaining.
         */
        public Builder withProvider(PrimeSourceProvider provider) {
            providers.add(provider);
            return this;
        }

        /**
         * Sets the maximum runtime duration.
         * @param timeInSeconds The time, in seconds.
         * @return The builder, for chaining.
         */
        public Builder withMaxRuntime(int timeInSeconds) {
            this.maxRuntimeSeconds = timeInSeconds;
            return this;
        }

        /**
         * Builds the builder to make a Prime object.
         * @param code The code.
         * @return The object.
         */
        public Prime build(String code) {
            return new Prime(code, filter, variables, providers, maxRuntimeSeconds);
        }

    }

    /**
     * A source provider pattern matcher wrapper.
     */
    public static final class Match {

        private final PrimeSourceProvider provider;
        private final String match;

        /**
         * Creates a new match.
         * @param provider The matching provider.
         * @param match The match group.
         */
        public Match(PrimeSourceProvider provider, String match) {
            this.provider = provider;
            this.match = match;
        }

        /**
         * Gets the provider.
         * @return The provider.
         */
        public PrimeSourceProvider getProvider() {
            return provider;
        }

        /**
         * Gets the match.
         * @return The match.
         */
        public String getMatch() {
            return match;
        }

    }

    /**
     * The util class.
     */
    public static final class Util {

        /**
         * Gets a match for the provided input string.
         * @param prime The prime instance.
         * @param input The input string.
         * @return A match.
         */
        public static Match getMatches(Prime prime, String input) {
            for(PrimeSourceProvider provider : prime.providers) {
                Matcher matcher = provider.getIncludePattern().matcher(input);
                if(!matcher.find()) {
                    continue;
                }
                return new Match(provider, matcher.group());
            }
            return null;
        }

        /**
         * Gets the provider for the given input string.
         * @param prime The prime instance.
         * @param identifier The input string.
         * @return A prime provider, or null if none were found.
         */
        public static PrimeSourceProvider getProvider(Prime prime, String identifier) {
            for(PrimeSourceProvider provider : prime.providers) {
                Matcher matcher = provider.getIncludePattern().matcher(identifier);
                if(!matcher.find()) {
                    continue;
                }
                return provider;
            }
            return null;
        }

    }

}
