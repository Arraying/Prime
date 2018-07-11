package de.arraying.prime;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
@SuppressWarnings("unused")
public final class Prime {

    private final String code;
    private final LinkedList<PrimeSourceProvider> providers;
    private final ScriptEngine engine;

    /**
     * Creates a new Prime instance.
     * @param code The code.
     * @param filter The filter.
     * @param variables The variables.
     * @param providers The providers.
     */
    private Prime(String code, PrimeClassFilter filter, Map<String, Object> variables, LinkedList<PrimeSourceProvider> providers) {
        this.code = code;
        this.providers = providers;
        engine = new NashornScriptEngineFactory().getScriptEngine(filter);
        variables.forEach(engine::put);
    }

    /**
     * Evaluates the code.
     * @throws Exception If there is an error.
     */
    public void evaluate() throws Exception {
        String codeRaw = new PrimeParser(this.code, providers).parse();
        String code = "(function(){" + codeRaw + "})();";
        engine.eval(code);
    }

    /**
     * The builder.
     */
    public static final class Builder {

        private final PrimeClassFilter filter = new PrimeClassFilter();
        private final Map<String, Object> variables = new HashMap<>();
        private final LinkedList<PrimeSourceProvider> providers = new LinkedList<>();

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
         * Builds the builder to make a Prime object.
         * @param code The code.
         * @return The object.
         */
        public Prime build(String code) {
            return new Prime(code, filter, variables, providers);
        }

    }

}
