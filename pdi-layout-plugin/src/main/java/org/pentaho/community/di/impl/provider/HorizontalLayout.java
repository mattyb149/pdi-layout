/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.pentaho.community.di.impl.provider;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.LoopPipe;
import org.pentaho.community.di.api.LayoutProvider;
import org.pentaho.community.di.util.GraphUtils;

import java.util.List;

public class HorizontalLayout implements LayoutProvider {

  public static final String PROPERTY_COLUMN = "column";
  public static final String PROPERTY_ROW = "row";
  public static final int COLUMN_FACTOR = 5;
  public static final int ROW_FACTOR = 8;

  @Override
  public String getId() {
    return "horizontalGridLayout";
  }

  @Override
  public String getName() {
    return "Horizontal Grid";
  }

  @Override
  public void applyLayout( Graph graph, int canvasWidth, int canvasHeight ) {
    GremlinPipeline<Graph, Vertex> pipe = new GremlinPipeline<>( graph );
    List<Vertex> vertices = pipe.V()
      .as( "loop" )
        // Set degree for each vertex
      .transform( new PipeFunction<Vertex, Iterable<Vertex>>() {
        @Override public Iterable<Vertex> compute( Vertex vertex ) {
          FluentIterable<Integer> degrees = FluentIterable.from( vertex.getVertices( Direction.IN ) )
            .transform( GraphUtils.<Integer>getProperty( PROPERTY_COLUMN ) );

          ImmutableList.Builder<Vertex> output = ImmutableList.builder();
          // If no inputs, rank as 0
          if ( degrees.isEmpty() ) {
            vertex.setProperty( PROPERTY_COLUMN, 0 );
          } else {
            // Find max degree of all inputs
            Integer value = Ordering.natural().nullsLast().max( degrees );
            if ( value != null ) {
              vertex.setProperty( PROPERTY_COLUMN, value + 1 );
            } else {
              output.addAll( vertex.getVertices( Direction.IN ) );
            }
          }
          output.add( vertex );
          return output.build();
        }
      } )
      .scatter().cast( Vertex.class )
      .loop( "loop", new PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean>() {
        @Override public Boolean compute( LoopPipe.LoopBundle<Vertex> argument ) {
          return !argument.getObject().getPropertyKeys().contains( PROPERTY_COLUMN );
        }
      } )
      .dedup()
      .groupBy( new PipeFunction<Vertex, Integer>() {
        @Override public Integer compute( Vertex vertex ) {
          return vertex.getProperty( PROPERTY_COLUMN );
        }
      }, new PipeFunction<Vertex, Vertex>() {
        @Override public Vertex compute( Vertex vertex ) {
          return vertex;
        }
      }, new PipeFunction<List<Vertex>, List<Vertex>>() {
        @Override public List<Vertex> compute( List<Vertex> group ) {
          int row = 0;
          for ( Vertex vertex : group ) {
            vertex.setProperty( PROPERTY_ROW, row++ );
          }
          return group;
        }
      } )
      .toList();

    updateXY( canvasWidth, canvasHeight, vertices );
  }

  protected void updateXY( int canvasWidth, int canvasHeight, List<Vertex> vertices ) {
    int columnWidth = canvasWidth / COLUMN_FACTOR;
    int rowWidth = canvasHeight / ROW_FACTOR;
    for ( Vertex vertex : vertices ) {
      int column = vertex.getProperty( PROPERTY_COLUMN );
      int row = vertex.getProperty( PROPERTY_ROW );
      vertex.setProperty( GraphUtils.PROPERTY_X, columnWidth / 2 + column * columnWidth );
      vertex.setProperty( GraphUtils.PROPERTY_Y, rowWidth / 2 + row * rowWidth );
    }
  }

}
