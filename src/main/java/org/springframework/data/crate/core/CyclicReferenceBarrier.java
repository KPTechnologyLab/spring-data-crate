/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.crate.core;

import org.springframework.data.crate.core.mapping.CratePersistentProperty;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

/**
 * {@link CyclicReferenceBarrier} holds information about properties and the paths for accessing those. This information is used
 * to detect potential cycles within references. If a potential cycle is detected, a {@link CyclicReferenceException}
 * is thrown
 *
 * @author Hasnain Javed
 * @since 1.0.0
 */
public class CyclicReferenceBarrier {

    private String dotPath;

    private final Map<String, List<VisitedPath>> visitedPaths;

    private CyclicReferenceBarrier() {
        this.visitedPaths = new LinkedHashMap<>();
    }

    public static CyclicReferenceBarrier cyclicReferenceBarrier() {
        return new CyclicReferenceBarrier();
    }

    public void guard(CratePersistentProperty property) {
        pushPath(property);
    }

    /**
     * @param property The property to watch
     * @throws CyclicReferenceException in case a potential cycle is detected.
     */
    private void pushPath(CratePersistentProperty property) {

        if (getVisitedPath(property) != null) {
            throw new CyclicReferenceException(property.getFieldName(), property.getOwner().getType(), dotPath);
        }

        String type = property.getActualType().getName();

        List<VisitedPath> paths = visitedPaths.get(type);

        if (paths == null) {
            paths = new LinkedList<>();
        }

        paths.add(new VisitedPath(property));

        if (!visitedPaths.containsKey(type)) {
            visitedPaths.put(type, paths);
        }

        if (hasText(dotPath)) {
            dotPath = dotPath.concat(".").concat(property.getFieldName());
        } else {
            dotPath = property.getFieldName();
        }
    }

    /**
     * @param property The property to inspect
     * @see VisitedPath#isCyclic(CratePersistentProperty)
     */
    private VisitedPath getVisitedPath(CratePersistentProperty property) {

        VisitedPath path = null;

        String type = property.getActualType().getName();

        if (visitedPaths.containsKey(type)) {

            List<VisitedPath> paths = visitedPaths.get(type);

            if (paths != null && !paths.isEmpty()) {
                for (VisitedPath visitedPath : paths) {
                    if (visitedPath.isCyclic(property)) {
                        path = visitedPath;
                        break;
                    }
                }
            }
        }

        return path;
    }

    /**
     * @author Hasnain Javed
     * @since 1.0.0
     */
    class VisitedPath {

        private String fieldName;

        private Class<?> referer;
        private Class<?> referee;

        public VisitedPath(CratePersistentProperty property) {
            super();
            this.fieldName = property.getFieldName();
            this.referer = property.getOwner().getType();
            this.referee = property.getActualType();
        }

        boolean isCyclic(CratePersistentProperty property) {
            return (fieldName.equals(property.getFieldName()) &&
                    referer.equals(property.getOwner().getType()) &&
                    referee.equals(property.getActualType()));
        }

        @Override
        public String toString() {
            return "fieldName=".concat(fieldName)
                    .concat(", ")
                    .concat("container=")
                    .concat(referer.getName())
                    .concat(", ")
                    .concat("referencingClass=")
                    .concat(referee.getName());
        }
    }

    @Override
    public String toString() {
        return visitedPaths.toString();
    }
}
