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

package org.apache.tinkerpop.gremlin.process.traversal;

import org.apache.tinkerpop.gremlin.process.traversal.lambda.AbstractLambdaTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.util.AndP;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;
import org.apache.tinkerpop.gremlin.process.traversal.util.OrP;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalP;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class P<V> extends AbstractLambdaTraversal<V, V> implements Predicate<V>, Serializable, Cloneable {

    protected Supplier<IllegalArgumentException> AND_OR_EXCEPTION = () -> new IllegalArgumentException("Only P predicates and traversals can be part of a conjunction");

    protected BiPredicate<V, V> biPredicate;
    protected V value;
    protected V nextValue;

    public P(final BiPredicate<V, V> biPredicate, final V value) {
        this.value = value;
        this.biPredicate = biPredicate;
    }

    public BiPredicate<V, V> getBiPredicate() {
        return this.biPredicate;
    }

    public V getValue() {
        return this.value;
    }

    public void setValue(final V value) {
        this.value = value;
    }

    @Override
    public boolean test(final V testValue) {
        return this.biPredicate.test(testValue, this.value);
    }

    @Override
    public int hashCode() {
        return this.biPredicate.hashCode() + this.value.hashCode();
    }

    @Override
    public boolean hasNext() {
        return this.biPredicate.test(this.nextValue, this.value);
    }

    @Override
    public V next() {
        if (this.hasNext()) return this.nextValue;  // TODO: this does a "double check" on biPredicate
        else throw FastNoSuchElementException.instance();
    }

    @Override
    public void addStart(final Traverser<V> start) {
        this.nextValue = start.get();
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof P &&
                ((P) other).getClass().equals(this.getClass()) &&
                ((P) other).getBiPredicate().equals(this.biPredicate) &&
                ((((P) other).getValue() == null && this.value == null) || ((P) other).getValue().equals(this.value));
    }

    @Override
    public String toString() {
        return null == this.value ? this.biPredicate.toString() : this.biPredicate.toString() + "(" + this.value + ")";
    }

    @Override
    public P<V> negate() {
        return new P<>(this.biPredicate.negate(), this.value);
    }

    public P<V> and(final Object traversal) {
        if (!(traversal instanceof Traversal))
            throw AND_OR_EXCEPTION.get();
        return this.and(P.traversal((Traversal) traversal));
    }

    public P<V> or(final Object traversal) {
        if (!(traversal instanceof Traversal))
            throw AND_OR_EXCEPTION.get();
        return this.or(P.traversal((Traversal) traversal));
    }

    @Override
    public P<V> and(final Predicate<? super V> predicate) {
        if (!(predicate instanceof P))
            throw AND_OR_EXCEPTION.get();
        return new AndP<>(this, (P<V>) predicate);
    }

    @Override
    public P<V> or(final Predicate<? super V> predicate) {
        if (!(predicate instanceof P))
            throw AND_OR_EXCEPTION.get();
        return new OrP<>(this, (P<V>) predicate);
    }

    public <S, E> List<Traversal.Admin<S, E>> getTraversals() {
        return Collections.emptyList();
    }

    public P<V> clone() {
        return (P<V>) super.clone();
    }

    //////////////// statics

    public static <V> P<V> eq(final V value) {
        return new P(Compare.eq, value);
    }

    public static <V> P<V> neq(final V value) {
        return new P(Compare.neq, value);
    }

    public static <V> P<V> lt(final V value) {
        return new P(Compare.lt, value);
    }

    public static <V> P<V> lte(final V value) {
        return new P(Compare.lte, value);
    }

    public static <V> P<V> gt(final V value) {
        return new P(Compare.gt, value);
    }

    public static <V> P<V> gte(final V value) {
        return new P(Compare.gte, value);
    }

    public static <V> P<V> inside(final V first, final V second) {
        return new AndP<>(new P(Compare.gt, first), new P(Compare.lt, second));
    }

    public static <V> P<V> outside(final V first, final V second) {
        return new OrP<>(new P(Compare.lt, first), new P(Compare.gt, second));
    }

    public static <V> P<V> between(final V first, final V second) {
        return new AndP<>(new P(Compare.gte, first), new P(Compare.lt, second));
    }

    public static <V> P<V> within(final V... values) {
        return P.within(Arrays.asList(values));
    }

    public static <V> P<V> within(final Collection<V> value) {
        return new P(Contains.within, value);
    }

    public static <V> P<V> without(final V... values) {
        return P.without(Arrays.asList(values));
    }

    public static <V> P<V> without(final Collection<V> value) {
        return new P(Contains.without, value);
    }

    public static <S, E> P<E> traversal(final Traversal<S, E> traversal) {
        return new TraversalP<>(traversal.asAdmin(), false);
    }

    public static <S, E> P<E> not(final Traversal<S, E> traversal) {
        return new TraversalP<>(traversal.asAdmin(), true);
    }

    public static P test(final BiPredicate biPredicate, final Object value) {
        return new P(biPredicate, value);
    }

    public static <V> P<V> not(final P<V> predicate) {
        return predicate.negate();
    }
}
