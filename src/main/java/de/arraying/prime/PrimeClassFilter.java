package de.arraying.prime;

import jdk.nashorn.api.scripting.ClassFilter;

import java.util.HashSet;
import java.util.Set;

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
final class PrimeClassFilter implements ClassFilter {

    private final Set<String> whitelisted = new HashSet<>();

    /**
     * Whether or not the class should be exposed to scripts.
     * @param s The class name.
     * @return True if it should, false otherwise.
     */
    public boolean exposeToScripts(String s) {
        return whitelisted.contains(s);
    }


    /**
     * Whitelists a class.
     * @param clazz The class.
     */
    void whitelist(Class<?> clazz) {
        whitelisted.add(clazz.getName());
    }

}
