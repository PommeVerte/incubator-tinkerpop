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

package org.apache.tinkerpop.gremlin.hadoop.structure.io;

import org.apache.tinkerpop.gremlin.structure.io.gryo.GryoMapper;
import org.apache.tinkerpop.gremlin.structure.util.star.StarGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class IOClasses {

    public static List<Class> getGryoClasses(final GryoMapper mapper) {
        return mapper.getRegisteredClasses();
    }

    public static List<Class> getSharedHadoopClasses() {
        final List<Class> hadoopClasses = new ArrayList<>();
        hadoopClasses.add(VertexWritable.class);
        hadoopClasses.add(ObjectWritable.class);
        hadoopClasses.add(StarGraph.class);
        hadoopClasses.add(StarGraph.StarVertex.class);
        hadoopClasses.add(StarGraph.StarVertexProperty.class);
        hadoopClasses.add(StarGraph.StarProperty.class);
        hadoopClasses.add(StarGraph.StarOutEdge.class);
        hadoopClasses.add(StarGraph.StarInEdge.class);
        hadoopClasses.add(StarGraph.StarAdjacentVertex.class);
        return hadoopClasses;
    }
}
