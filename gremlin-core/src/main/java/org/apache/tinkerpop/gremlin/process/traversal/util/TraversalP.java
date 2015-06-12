/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.apache.tinkerpop.gremlin.process.traversal.util;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalEngine;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSideEffects;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.TraverserGenerator;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class TraversalP<S, E> extends P<E> {

    private Traversal.Admin<S, E> traversal;
    private final boolean negate;

    public TraversalP(final Traversal.Admin<S, E> traversal, final E end, final boolean negate) {
        super(null, end);
        this.traversal = traversal;
        this.negate = negate;
        this.biPredicate = (BiPredicate) new TraversalBiPredicate(this);
    }

    public TraversalP(final Traversal.Admin<S, E> traversal, final boolean negate) {
        this(traversal, null, negate);
    }

    public List<Traversal.Admin<S, E>> getTraversals() {
        return Collections.singletonList(this.traversal);
    }

    @Override
    public int hashCode() {
        return this.traversal.hashCode() ^ Boolean.hashCode(this.negate);
    }

    @Override
    public boolean equals(final Object other) {
        if (other != null && other.getClass().equals(this.getClass())) {
            final TraversalP otherTraversalP = (TraversalP) other;
            return this.negate == otherTraversalP.negate &&
                    this.traversal.equals(otherTraversalP.traversal);
        }
        return false;
    }

    @Override
    public TraversalP<S, E> negate() {
        return new TraversalP<>(this.traversal.clone(), this.value, !this.negate);
    }

    @Override
    public TraversalP<S, E> clone() {
        final TraversalP<S, E> clone = (TraversalP<S, E>) super.clone();
        clone.traversal = this.traversal.clone();
        clone.biPredicate = (BiPredicate) new TraversalBiPredicate<>(clone);
        return clone;
    }

    @Override
    public void addStart(final Traverser start) {
        this.traversal.getStartStep().addStart(start);
    }

    @Override
    public boolean hasNext() {
        if (this.negate)
            return !this.traversal.hasNext();
        else
            return this.traversal.hasNext();
    }

    @Override
    public E next() {
        return this.traversal.next();
    }

    @Override
    public void reset() {
        this.traversal.reset();
    }

    public List<Step> getSteps() {
        return Collections.emptyList();
    }

    @Override
    public <S2, E2> Traversal.Admin<S2, E2> addStep(final int index, final Step<?, ?> step) throws IllegalStateException {
        return (Traversal.Admin<S2, E2>) this;
    }

    @Override
    public <S2, E2> Traversal.Admin<S2, E2> removeStep(final int index) throws IllegalStateException {
        return (Traversal.Admin<S2, E2>) this;
    }

    @Override
    public void applyStrategies() throws IllegalStateException {
        this.traversal.applyStrategies();
    }

    @Override
    public TraversalEngine getEngine() {
        return this.traversal.getEngine();
    }

    @Override
    public TraverserGenerator getTraverserGenerator() {
        return this.traversal.getTraverserGenerator();
    }

    @Override
    public void setSideEffects(final TraversalSideEffects sideEffects) {
        this.traversal.setSideEffects(sideEffects);
    }

    @Override
    public TraversalSideEffects getSideEffects() {
        return this.traversal.getSideEffects();
    }

    @Override
    public void setStrategies(final TraversalStrategies strategies) {
        this.traversal.setStrategies(strategies);
    }

    @Override
    public TraversalStrategies getStrategies() {
        return this.traversal.getStrategies();
    }

    @Override
    public void setParent(final TraversalParent step) {
        this.traversal.setParent(step);
    }

    @Override
    public TraversalParent getParent() {
        return this.traversal.getParent();
    }

    @Override
    public boolean isLocked() {
        return this.traversal.isLocked();
    }

    @Override
    public void setEngine(final TraversalEngine engine) {
        this.traversal.setEngine(engine);
    }

    @Override
    public Optional<Graph> getGraph() {
        return this.traversal.getGraph();
    }

    @Override
    public void setGraph(final Graph graph) {
        this.traversal.setGraph(graph);
    }


    private static class TraversalBiPredicate<S, E> implements BiPredicate<S, E>, Serializable {

        private final TraversalP<S, E> traversalP;

        public TraversalBiPredicate(final TraversalP<S, E> traversalP) {
            this.traversalP = traversalP;
        }

        @Override
        public boolean test(final S start, final E end) {
            if (null == start)
                throw new IllegalArgumentException("The traversal must be provided a start: " + traversalP.traversal);
            final boolean result;
            if (start instanceof Traverser)
                result = TraversalUtil.test(((Traverser<S>) start).asAdmin(), traversalP.traversal, end);
            else
                result = TraversalUtil.test(start, traversalP.traversal, end);
            return traversalP.negate ? !result : result;
        }

        @Override
        public String toString() {
            return this.traversalP.negate ? "!" + this.traversalP.traversal.toString() : this.traversalP.traversal.toString();
        }
    }
}
