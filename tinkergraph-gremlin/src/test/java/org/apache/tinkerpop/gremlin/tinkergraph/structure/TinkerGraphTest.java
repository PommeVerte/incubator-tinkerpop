/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.tinkergraph.structure;

import org.apache.commons.io.FileUtils;
import org.apache.tinkerpop.gremlin.AbstractGremlinTest;
import org.apache.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.TestHelper;
import org.apache.tinkerpop.gremlin.structure.util.star.StarGraph;
import org.apache.tinkerpop.gremlin.process.traversal.T;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.engine.StandardTraversalEngine;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.IsStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.PropertiesStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.GraphStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Operator;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.GraphReader;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLWriter;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONMapper;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONWriter;
import org.apache.tinkerpop.gremlin.structure.io.gryo.GryoReader;
import org.apache.tinkerpop.gremlin.structure.io.gryo.GryoWriter;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedFactory;
import org.apache.tinkerpop.gremlin.tinkergraph.process.traversal.dsl.graph.DefaultVariableGraphTraversal;
import org.apache.tinkerpop.gremlin.tinkergraph.process.traversal.dsl.graph.TraversalVariable;
import org.apache.tinkerpop.gremlin.tinkergraph.process.traversal.dsl.graph.TraversalVariablePosition;
import org.apache.tinkerpop.gremlin.tinkergraph.process.traversal.dsl.graph.VariableGraphTraversal;
import org.apache.tinkerpop.gremlin.tinkergraph.process.traversal.dsl.graph.VariableGraphTraversalSource;
import org.apache.tinkerpop.gremlin.util.StreamFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class TinkerGraphTest {

    private static String tempPath;

    static {
        tempPath = TestHelper.makeTestDataPath(TinkerGraphTest.class, "tinkerpop-io").getPath() + File.separator;
    }

    @BeforeClass
    public static void before() throws IOException {
        final File tempDir = new File(tempPath);
        FileUtils.deleteDirectory(tempDir);
        if (!tempDir.mkdirs()) throw new IOException(String.format("Could not create %s", tempDir));
    }

    @Test
    @Ignore
    public void testPlay() {
        Graph g = TinkerGraph.open();
        Vertex v1 = g.addVertex(T.id, "1", "animal", "males");
        Vertex v2 = g.addVertex(T.id, "2", "animal", "puppy");
        Vertex v3 = g.addVertex(T.id, "3", "animal", "mama");
        Vertex v4 = g.addVertex(T.id, "4", "animal", "puppy");
        Vertex v5 = g.addVertex(T.id, "5", "animal", "chelsea");
        Vertex v6 = g.addVertex(T.id, "6", "animal", "low");
        Vertex v7 = g.addVertex(T.id, "7", "animal", "mama");
        Vertex v8 = g.addVertex(T.id, "8", "animal", "puppy");
        Vertex v9 = g.addVertex(T.id, "9", "animal", "chula");

        v1.addEdge("link", v2, "weight", 2f);
        v2.addEdge("link", v3, "weight", 3f);
        v2.addEdge("link", v4, "weight", 4f);
        v2.addEdge("link", v5, "weight", 5f);
        v3.addEdge("link", v6, "weight", 1f);
        v4.addEdge("link", v6, "weight", 2f);
        v5.addEdge("link", v6, "weight", 3f);
        v6.addEdge("link", v7, "weight", 2f);
        v6.addEdge("link", v8, "weight", 3f);
        v7.addEdge("link", v9, "weight", 1f);
        v8.addEdge("link", v9, "weight", 7f);

        g.traversal().V(v1).withSack(Float.MIN_VALUE).repeat(outE().sack(Operator.max, "weight").inV()).times(5).sack().forEachRemaining(System.out::println);
    }

   /* @Test
    public void testTraversalDSL() throws Exception {
        Graph g = TinkerFactory.createClassic();
        assertEquals(2, g.of(TinkerFactory.SocialTraversal.class).people("marko").knows().name().toList().size());
        g.of(TinkerFactory.SocialTraversal.class).people("marko").knows().name().forEachRemaining(name -> assertTrue(name.equals("josh") || name.equals("vadas")));
        assertEquals(1, g.of(TinkerFactory.SocialTraversal.class).people("marko").created().name().toList().size());
        g.of(TinkerFactory.SocialTraversal.class).people("marko").created().name().forEachRemaining(name -> assertEquals("lop", name));
    }*/

    @Test
    @Ignore
    public void benchmarkStandardTraversals() throws Exception {
        Graph graph = TinkerGraph.open();
        GraphTraversalSource g = graph.traversal();
        graph.io().readGraphML("data/grateful-dead.xml");
        final List<Supplier<Traversal>> traversals = Arrays.asList(
                () -> g.V().outE().inV().outE().inV().outE().inV(),
                () -> g.V().out().out().out(),
                () -> g.V().out().out().out().path(),
                () -> g.V().repeat(out()).times(2),
                () -> g.V().repeat(out()).times(3),
                () -> g.V().local(out().out().values("name").fold()),
                () -> g.V().out().local(out().out().values("name").fold()),
                () -> g.V().out().map(v -> g.V(v.get()).out().out().values("name").toList())
        );
        traversals.forEach(traversal -> {
            System.out.println("\nTESTING: " + traversal.get());
            for (int i = 0; i < 7; i++) {
                final long t = System.currentTimeMillis();
                traversal.get().iterate();
                System.out.print("   " + (System.currentTimeMillis() - t));
            }
        });
    }

    @Test
    @Ignore
    public void testPlay3() throws Exception {
        TinkerGraph tg = TinkerFactory.createModern();
        StarGraph sg = StarGraph.open();
        tg.vertices().forEachRemaining(v -> StarGraph.addTo(sg, DetachedFactory.detach(v,true)));
        tg.vertices(1).next().edges(Direction.BOTH).forEachRemaining(e -> StarGraph.addTo(sg, DetachedFactory.detach(e, true)));
        sg.vertices().forEachRemaining(System.out::println);
        sg.edges().forEachRemaining(System.out::println);
    }

    @Test
    @Ignore
    public void testPlay4() throws Exception {
        Graph graph = TinkerGraph.open();
        graph.io().readGraphML("/Users/marko/software/tinkerpop/tinkerpop3/data/grateful-dead.xml");
        GraphTraversalSource g = graph.traversal();
        final List<Supplier<Traversal>> traversals = Arrays.asList(
                () -> g.V().has(T.label, "song").out().groupCount().<Vertex>by(t ->
                        g.V(t).choose(r -> g.V(r).has(T.label, "artist").hasNext(),
                                in("writtenBy", "sungBy"),
                                both("followedBy")).values("name").next()).fold(),
                () -> g.V().has(T.label, "song").out().groupCount().<Vertex>by(t ->
                        g.V(t).choose(has(T.label, "artist"),
                                in("writtenBy", "sungBy"),
                                both("followedBy")).values("name").next()).fold(),
                () -> g.V().has(T.label, "song").out().groupCount().by(
                        choose(has(T.label, "artist"),
                                in("writtenBy", "sungBy"),
                                both("followedBy")).values("name")).fold(),
                () -> g.V().has(T.label, "song").both().groupCount().<Vertex>by(t -> g.V(t).both().values("name").next()),
                () -> g.V().has(T.label, "song").both().groupCount().by(both().values("name")));
        traversals.forEach(traversal -> {
            System.out.println("\nTESTING: " + traversal.get());
            for (int i = 0; i < 10; i++) {
                final long t = System.currentTimeMillis();
                traversal.get().iterate();
                //System.out.println(traversal.get().toList());
                System.out.print("   " + (System.currentTimeMillis() - t));
            }
        });
    }

    @Test
    @Ignore
    public void testPlayDK() throws Exception {
        GraphTraversalSource g = TinkerFactory.createModern().traversal();
        Traversal t = g.V().hasLabel("person").as("person").local(bothE().label().groupCount("x").cap("x")).as("relations").select().by("name").by();
        t.forEachRemaining(System.out::println);
        System.out.println("--");

        t = g.V().match("a",
                as("a").out("knows").as("b"),
                as("b").out("created").has("name", "lop"),
                as("b").match("a1",
                        as("a1").out("created").as("b1"),
                        as("b1").in("created").as("c1")).select("c1").as("c")).<String>select().by("name");
        t.forEachRemaining(System.out::println);
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteClassicGraphAsGryo() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-classic.kryo");
        GryoWriter.build().create().writeGraph(os, TinkerFactory.createClassic());
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteModernGraphAsGryo() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-modern.kryo");
        GryoWriter.build().create().writeGraph(os, TinkerFactory.createModern());
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteCrewGraphAsGryo() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-crew.kryo");
        GryoWriter.build().create().writeGraph(os, TinkerFactory.createTheCrew());
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteClassicVerticesAsGryo() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-classic-vertices.kryo");
        GryoWriter.build().create().writeVertices(os, TinkerFactory.createClassic().traversal().V(), Direction.BOTH);
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteClassicVerticesAsGraphSON() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-classic-vertices.ldjson");
        GraphSONWriter.build().create().writeVertices(os, TinkerFactory.createClassic().traversal().V(), Direction.BOTH);
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteModernVerticesAsGryo() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-modern-vertices.kryo");
        GryoWriter.build().create().writeVertices(os, TinkerFactory.createModern().traversal().V(), Direction.BOTH);
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteModernVerticesAsGraphSON() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-modern-vertices.ldjson");
        GraphSONWriter.build().create().writeVertices(os, TinkerFactory.createModern().traversal().V(), Direction.BOTH);
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteCrewVerticesAsGraphSON() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-crew-vertices.ldjson");
        GraphSONWriter.build().create().writeVertices(os, TinkerFactory.createTheCrew().traversal().V(), Direction.BOTH);
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteCrewVerticesAsGryo() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-crew-vertices.kryo");
        GryoWriter.build().create().writeVertices(os, TinkerFactory.createTheCrew().traversal().V(), Direction.BOTH);
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteClassicGraphAsGraphML() throws IOException {
        try (final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-classic.xml")) {
            GraphMLWriter.build().create().writeGraph(os, TinkerFactory.createClassic());
        }
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteModernGraphAsGraphML() throws IOException {
        try (final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-modern.xml")) {
            GraphMLWriter.build().create().writeGraph(os, TinkerFactory.createModern());
        }
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteClassicGraphAsGraphSONNoTypes() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-classic.json");
        GraphSONWriter.build().create().writeGraph(os, TinkerFactory.createClassic());
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteModernGraphAsGraphSONNoTypes() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-modern.json");
        GraphSONWriter.build().create().writeGraph(os, TinkerFactory.createModern());
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteCrewGraphAsGraphSONNoTypes() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-crew.json");
        GraphSONWriter.build().create().writeGraph(os, TinkerFactory.createTheCrew());
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteClassicGraphNormalizedAsGraphSON() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-classic-normalized.json");
        GraphSONWriter.build().mapper(GraphSONMapper.build().normalize(true).create()).create().writeGraph(os, TinkerFactory.createClassic());
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteModernGraphNormalizedAsGraphSON() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-modern-normalized.json");
        GraphSONWriter.build().mapper(GraphSONMapper.build().normalize(true).create()).create().writeGraph(os, TinkerFactory.createModern());
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteClassicGraphAsGraphSONWithTypes() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-classic-typed.json");
        GraphSONWriter.build().mapper(GraphSONMapper.build().embedTypes(true).create())
                .create().writeGraph(os, TinkerFactory.createClassic());
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteModernGraphAsGraphSONWithTypes() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-modern-typed.json");
        GraphSONWriter.build().mapper(GraphSONMapper.build().embedTypes(true).create())
                .create().writeGraph(os, TinkerFactory.createModern());
        os.close();
    }

    /**
     * No assertions.  Just write out the graph for convenience.
     */
    @Test
    public void shouldWriteCrewGraphAsGraphSONWithTypes() throws IOException {
        final OutputStream os = new FileOutputStream(tempPath + "tinkerpop-crew-typed.json");
        GraphSONWriter.build().mapper(GraphSONMapper.build().embedTypes(true).create())
                .create().writeGraph(os, TinkerFactory.createTheCrew());
        os.close();
    }

    @Test
    public void shouldManageIndices() {
        final TinkerGraph g = TinkerGraph.open();

        Set<String> keys = g.getIndexedKeys(Vertex.class);
        assertEquals(0, keys.size());
        keys = g.getIndexedKeys(Edge.class);
        assertEquals(0, keys.size());

        g.createIndex("name1", Vertex.class);
        g.createIndex("name2", Vertex.class);
        g.createIndex("oid1", Edge.class);
        g.createIndex("oid2", Edge.class);

        // add the same one twice to check idempotance
        g.createIndex("name1", Vertex.class);

        keys = g.getIndexedKeys(Vertex.class);
        assertEquals(2, keys.size());
        for (String k : keys) {
            assertTrue(k.equals("name1") || k.equals("name2"));
        }

        keys = g.getIndexedKeys(Edge.class);
        assertEquals(2, keys.size());
        for (String k : keys) {
            assertTrue(k.equals("oid1") || k.equals("oid2"));
        }

        g.dropIndex("name2", Vertex.class);
        keys = g.getIndexedKeys(Vertex.class);
        assertEquals(1, keys.size());
        assertEquals("name1", keys.iterator().next());

        g.dropIndex("name1", Vertex.class);
        keys = g.getIndexedKeys(Vertex.class);
        assertEquals(0, keys.size());

        g.dropIndex("oid1", Edge.class);
        keys = g.getIndexedKeys(Edge.class);
        assertEquals(1, keys.size());
        assertEquals("oid2", keys.iterator().next());

        g.dropIndex("oid2", Edge.class);
        keys = g.getIndexedKeys(Edge.class);
        assertEquals(0, keys.size());

        g.dropIndex("better-not-error-index-key-does-not-exist", Vertex.class);
        g.dropIndex("better-not-error-index-key-does-not-exist", Edge.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateVertexIndexWithNullKey() {
        final TinkerGraph g = TinkerGraph.open();
        g.createIndex(null, Vertex.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateEdgeIndexWithNullKey() {
        final TinkerGraph g = TinkerGraph.open();
        g.createIndex(null, Edge.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateVertexIndexWithEmptyKey() {
        final TinkerGraph g = TinkerGraph.open();
        g.createIndex("", Vertex.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateEdgeIndexWithEmptyKey() {
        final TinkerGraph g = TinkerGraph.open();
        g.createIndex("", Edge.class);
    }

    @Ignore
    @Test
    public void shouldUpdateVertexIndicesInNewGraph() {
        final TinkerGraph g = TinkerGraph.open();
        g.createIndex("name", Vertex.class);

        g.addVertex("name", "marko", "age", 29);
        g.addVertex("name", "stephen", "age", 35);

        // a tricky way to evaluate if indices are actually being used is to pass a fake BiPredicate to has()
        // to get into the Pipeline and evaluate what's going through it.  in this case, we know that at index
        // is used because only "stephen" ages should pass through the pipeline due to the inclusion of the
        // key index lookup on "name".  If there's an age of something other than 35 in the pipeline being evaluated
        // then something is wrong.
        assertEquals(new Long(1), g.traversal().V().has("age", (t, u) -> {
            assertEquals(35, t);
            return true;
        }, 35).has("name", "stephen").count().next());
    }

    @Ignore
    @Test
    public void shouldRemoveAVertexFromAnIndex() {
        final TinkerGraph g = TinkerGraph.open();
        g.createIndex("name", Vertex.class);

        g.addVertex("name", "marko", "age", 29);
        g.addVertex("name", "stephen", "age", 35);
        final Vertex v = g.addVertex("name", "stephen", "age", 35);

        // a tricky way to evaluate if indices are actually being used is to pass a fake BiPredicate to has()
        // to get into the Pipeline and evaluate what's going through it.  in this case, we know that at index
        // is used because only "stephen" ages should pass through the pipeline due to the inclusion of the
        // key index lookup on "name".  If there's an age of something other than 35 in the pipeline being evaluated
        // then something is wrong.
        assertEquals(new Long(2), g.traversal().V().has("age", (t, u) -> {
            assertEquals(35, t);
            return true;
        }, 35).has("name", "stephen").count().next());

        v.remove();
        assertEquals(new Long(1), g.traversal().V().has("age", (t, u) -> {
            assertEquals(35, t);
            return true;
        }, 35).has("name", "stephen").count().next());
    }

    @Ignore
    @Test
    public void shouldUpdateVertexIndicesInExistingGraph() {
        final TinkerGraph g = TinkerGraph.open();

        g.addVertex("name", "marko", "age", 29);
        g.addVertex("name", "stephen", "age", 35);

        // a tricky way to evaluate if indices are actually being used is to pass a fake BiPredicate to has()
        // to get into the Pipeline and evaluate what's going through it.  in this case, we know that at index
        // is not used because "stephen" and "marko" ages both pass through the pipeline.
        assertEquals(new Long(1), g.traversal().V().has("age", (t, u) -> {
            assertTrue(t.equals(35) || t.equals(29));
            return true;
        }, 35).has("name", "stephen").count().next());

        g.createIndex("name", Vertex.class);

        // another spy into the pipeline for index check.  in this case, we know that at index
        // is used because only "stephen" ages should pass through the pipeline due to the inclusion of the
        // key index lookup on "name".  If there's an age of something other than 35 in the pipeline being evaluated
        // then something is wrong.
        assertEquals(new Long(1), g.traversal().V().has("age", (t, u) -> {
            assertEquals(35, t);
            return true;
        }, 35).has("name", "stephen").count().next());
    }

    @Ignore
    @Test
    public void shouldUpdateEdgeIndicesInNewGraph() {
        final TinkerGraph g = TinkerGraph.open();
        g.createIndex("oid", Edge.class);

        final Vertex v = g.addVertex();
        v.addEdge("friend", v, "oid", "1", "weight", 0.5f);
        v.addEdge("friend", v, "oid", "2", "weight", 0.6f);

        // a tricky way to evaluate if indices are actually being used is to pass a fake BiPredicate to has()
        // to get into the Pipeline and evaluate what's going through it.  in this case, we know that at index
        // is used because only oid 1 should pass through the pipeline due to the inclusion of the
        // key index lookup on "oid".  If there's an weight of something other than 0.5f in the pipeline being
        // evaluated then something is wrong.
        assertEquals(new Long(1), g.traversal().E().has("weight", (t, u) -> {
            assertEquals(0.5f, t);
            return true;
        }, 0.5).has("oid", "1").count().next());
    }

    @Ignore
    @Test
    public void shouldRemoveEdgeFromAnIndex() {
        final TinkerGraph g = TinkerGraph.open();
        g.createIndex("oid", Edge.class);

        final Vertex v = g.addVertex();
        v.addEdge("friend", v, "oid", "1", "weight", 0.5f);
        final Edge e = v.addEdge("friend", v, "oid", "1", "weight", 0.5f);
        v.addEdge("friend", v, "oid", "2", "weight", 0.6f);

        // a tricky way to evaluate if indices are actually being used is to pass a fake BiPredicate to has()
        // to get into the Pipeline and evaluate what's going through it.  in this case, we know that at index
        // is used because only oid 1 should pass through the pipeline due to the inclusion of the
        // key index lookup on "oid".  If there's an weight of something other than 0.5f in the pipeline being
        // evaluated then something is wrong.
        assertEquals(new Long(2), g.traversal().E().has("weight", (t, u) -> {
            assertEquals(0.5f, t);
            return true;
        }, 0.5).has("oid", "1").count().next());

        e.remove();
        assertEquals(new Long(1), g.traversal().E().has("weight", (t, u) -> {
            assertEquals(0.5f, t);
            return true;
        }, 0.5).has("oid", "1").count().next());
    }

    @Ignore
    @Test
    public void shouldUpdateEdgeIndicesInExistingGraph() {
        final TinkerGraph g = TinkerGraph.open();

        final Vertex v = g.addVertex();
        v.addEdge("friend", v, "oid", "1", "weight", 0.5f);
        v.addEdge("friend", v, "oid", "2", "weight", 0.6f);

        // a tricky way to evaluate if indices are actually being used is to pass a fake BiPredicate to has()
        // to get into the Pipeline and evaluate what's going through it.  in this case, we know that at index
        // is not used because "1" and "2" weights both pass through the pipeline.
        assertEquals(new Long(1), g.traversal().E().has("weight", (t, u) -> {
            assertTrue(t.equals(0.5f) || t.equals(0.6f));
            return true;
        }, 0.5).has("oid", "1").count().next());

        g.createIndex("oid", Edge.class);

        // another spy into the pipeline for index check.  in this case, we know that at index
        // is used because only oid 1 should pass through the pipeline due to the inclusion of the
        // key index lookup on "oid".  If there's an weight of something other than 0.5f in the pipeline being
        // evaluated then something is wrong.
        assertEquals(new Long(1), g.traversal().E().has("weight", (t, u) -> {
            assertEquals(0.5f, t);
            return true;
        }, 0.5).has("oid", "1").count().next());
    }

    /**
     * This test helps with data conversions on Grateful Dead.  No Assertions...run as needed. Never read from the
     * GraphML source as it will always use a String identifier.
     */
    @Test
    public void shouldWriteGratefulDead() throws IOException {
        final Graph g = TinkerGraph.open();

        final GraphReader reader = GryoReader.build().create();
        try (final InputStream stream = AbstractGremlinTest.class.getResourceAsStream("/org/apache/tinkerpop/gremlin/structure/io/gryo/grateful-dead.kryo")) {
            reader.readGraph(stream, g);
        }

        /* keep this hanging around because changes to gryo format will need grateful dead generated from json so you can generate the gio
        final GraphSONMapper mapper = GraphSONMapper.build().embedTypes(true).create();
        final GraphReader reader = org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONReader.build().mapper(mapper).create();
        try (final InputStream stream = AbstractGremlinTest.class.getResourceAsStream("/org/apache/tinkerpop/gremlin/structure/io/graphson/grateful-dead.json")) {
            reader.readGraph(stream, g);
        }
        */

        final Graph ng = TinkerGraph.open();
        g.traversal().V().sideEffect(ov -> {
            final Vertex v = ov.get();
            if (v.label().equals("song"))
                ng.addVertex(T.id, Integer.parseInt(v.id().toString()), T.label, "song", "name", v.value("name"), "performances", v.property("performances").orElse(0), "songType", v.property("songType").orElse(""));
            else if (v.label().equals("artist"))
                ng.addVertex(T.id, Integer.parseInt(v.id().toString()), T.label, "artist", "name", v.value("name"));
            else
                throw new RuntimeException("damn");
        }).iterate();

        g.traversal().E().sideEffect(oe -> {
            final Edge e = oe.get();
            final Vertex v2 = ng.traversal().V(Integer.parseInt(e.inVertex().id().toString())).next();
            final Vertex v1 = ng.traversal().V(Integer.parseInt(e.outVertex().id().toString())).next();

            if (e.label().equals("followedBy"))
                v1.addEdge("followedBy", v2, T.id, Integer.parseInt(e.id().toString()), "weight", e.value("weight"));
            else if (e.label().equals("sungBy"))
                v1.addEdge("sungBy", v2, T.id, Integer.parseInt(e.id().toString()));
            else if (e.label().equals("writtenBy"))
                v1.addEdge("writtenBy", v2, T.id, Integer.parseInt(e.id().toString()));
            else
                throw new RuntimeException("bah");

        }).iterate();

        final OutputStream os = new FileOutputStream(tempPath + "grateful-dead.kryo");
        GryoWriter.build().create().writeGraph(os, ng);
        os.close();

        final OutputStream os2 = new FileOutputStream(tempPath + "grateful-dead.json");
        GraphSONWriter.build().mapper(GraphSONMapper.build().embedTypes(true).create()).create().writeGraph(os2, g);
        os2.close();

        final OutputStream os3 = new FileOutputStream(tempPath + "grateful-dead.xml");
        GraphMLWriter.build().create().writeGraph(os3, g);
        os3.close();

        final OutputStream os4 = new FileOutputStream(tempPath + "grateful-dead-vertices.kryo");
        GryoWriter.build().create().writeVertices(os4, g.traversal().V(), Direction.BOTH);
        os.close();

        final OutputStream os5 = new FileOutputStream(tempPath + "grateful-dead-vertices.ldjson");
        GraphSONWriter.build().create().writeVertices(os5, g.traversal().V(), Direction.BOTH);
        os.close();
    }

    @Test
    public void shouldGrabParameters() throws Exception {
        // compile() should cache the script to avoid future compilation
        final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();

        final Graph graph = TinkerFactory.createModern();

        // initially bind "g" to the variablized version of a Traversal
        final VariableGraphTraversalSource gVar = graph.traversal(VariableGraphTraversalSource.build().engine(StandardTraversalEngine.build()));

        final String script = "g.V(x).out().has('name',y).values('age').is(z).range(aa,10)";
        final CompiledScript compilable = engine.compile(script);

        // get the variablized Traversal instance.  variables are tracked within the Traversal implementation itself
        // and are referenced by step as a key within a Map
        try {
            final Bindings b = new SimpleBindings();
            b.put("g", gVar);
            b.put("x", new TraversalVariable("x"));
            b.put("y", new TraversalVariable("y"));
            b.put("z", new TraversalVariable("z"));
            b.put("aa", new TraversalVariable("aa"));
            final VariableGraphTraversal o = (VariableGraphTraversal) compilable.eval(b);
            System.out.println(o);

            final Map<Step, List<TraversalVariablePosition>> variables = o.getStepVariables();

            assertEquals(b.get("x"), variables.get(o.asAdmin().getStartStep()).get(0).getVariable());
            assertEquals(b.get("y"), variables.get(o.asAdmin().getSteps().get(2)).get(0).getVariable());
            assertEquals(b.get("z"), variables.get(o.asAdmin().getSteps().get(4)).get(0).getVariable());
            assertEquals(b.get("aa"), variables.get(o.asAdmin().getSteps().get(5)).get(0).getVariable());
        } catch (ScriptException se) {
            se.printStackTrace();
        }

        // at this point the script is compiled so we saved that step and can re-use it with a different
        // and standard GraphTraversalSource for the binding to "g" in the ScriptEngine:
        final GraphTraversalSource g = graph.traversal();
        final Bindings b = new SimpleBindings();
        b.put("g", g);
        b.put("x", 1);
        b.put("y", "josh");
        b.put("z", 32);
        b.put("aa", 0);

        final Traversal t = (Traversal) engine.eval(script, b);
        assertEquals(32, t.next());
    }

    @Test
    public void shouldTrackTraversalVariables() throws Exception {
        final Graph graph = TinkerFactory.createModern();

        final VariableGraphTraversalSource gVar = graph.traversal(VariableGraphTraversalSource.build().engine(StandardTraversalEngine.build()));
        final TraversalVariable varX = new TraversalVariable("x");
        final TraversalVariable varY = new TraversalVariable("y");
        final TraversalVariable varZ = new TraversalVariable("z");
        final TraversalVariable varAa = new TraversalVariable("aa");

        final VariableGraphTraversal<Vertex,Object> t = gVar.V(varX).out().has("name", varY)
                .values("age").is(varZ).range(varAa, 10);

        final Map<Step, List<TraversalVariablePosition>> variables = t.getStepVariables();

        assertEquals(varX, variables.get(t.asAdmin().getStartStep()).get(0).getVariable());
        assertEquals(varY, variables.get(t.asAdmin().getSteps().get(2)).get(0).getVariable());
        assertEquals(varZ, variables.get(t.asAdmin().getSteps().get(4)).get(0).getVariable());
        assertEquals(varAa, variables.get(t.asAdmin().getSteps().get(5)).get(0).getVariable());

        /*
        final Map<String,Object> bindings = new HashMap<>();
        bindings.put("x", 1);
        bindings.put("y", "josh");
        bindings.put("z", 32);
        bindings.put("aa", 0);

        // bind() would clone "t" with traversals applied, as possible, given statically defined steps (i.e. that
        // don't have variables).  in this way the traversal is "prepared" as best it can be given the information
        // that it has available.  TraversalVariables would be replaced with the values from the "bindings" Map
        // thus making the traversal "final" or "static"
        final Traversal t1 = t.bind(bindings);

        // when next() is called on "t1", remaining strategies can be executed given that the bindings are final
        assertEquals(32, t1.next());
        */
    }
}
