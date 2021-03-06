package de.arraying.prime;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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
public class PrimeRuntime implements Runnable {

    private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private final ScriptEngine engine;
    private final String code;
    private final Set<String> blacklistedBindings;
    private final Consumer<Exception> error;

    /**
     * Creates a new Prime runtime.
     * @param engine The script engine.
     * @param code The code.
     * @param blacklistedBindings The bindings to blacklist.
     * @param error The error consumer.
     */
    PrimeRuntime(ScriptEngine engine, String code, Set<String> blacklistedBindings, Consumer<Exception> error) {
        this.engine = engine;
        this.code = code;
        this.blacklistedBindings = blacklistedBindings;
        this.error = error;
    }

    /**
     * Whether or not the script finished running.
     * @return The atomic boolean's value.
     */
    boolean isCompleted() {
        return atomicBoolean.get();
    }

    /**
     * Runs the runtime.
     */
    @Override
    public void run() {
        try {
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            for(String blacklisted : blacklistedBindings) {
                bindings.remove(blacklisted);
            }
            engine.eval(code);
            atomicBoolean.set(true);
        } catch(Exception exception) {
            atomicBoolean.set(true);
            if(error != null) {
                error.accept(exception);
            }
        }
    }

}
