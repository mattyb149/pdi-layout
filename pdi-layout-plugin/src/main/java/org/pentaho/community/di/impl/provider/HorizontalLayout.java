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

import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.LoopPipe;
import org.pentaho.community.di.api.LayoutProvider;
import org.pentaho.community.di.util.GraphUtils;

import java.util.List;
import java.util.Set;

public class HorizontalLayout implements LayoutProvider {

  public static final String PROPERTY_DEGREE = "degree";
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
    ListMultimap<Integer, Vertex> groups = groupByDegree( graph );
    updateGrid( groups );
    updateXY( canvasWidth, canvasHeight, groups.values() );
  }

  protected ListMultimap<Integer, Vertex> groupByDegree( Graph graph ) {
    final ListMultimap<Integer, Vertex> groups = ArrayListMultimap.create();
    new GremlinPipeline<Graph, Vertex>( graph ).V()
      .as( "loop" )
        // Set degree for each vertex (defined here as distance to the furthest input step)
      .transform( new PipeFunction<Vertex, Iterable<Vertex>>() {
        @Override public Iterable<Vertex> compute( Vertex vertex ) {
          FluentIterable<Integer> degrees = FluentIterable.from( vertex.getVertices( Direction.IN ) )
            .transform( GraphUtils.<Integer>getProperty( PROPERTY_DEGREE ) );

          ImmutableList.Builder<Vertex> output = ImmutableList.builder();
          // If no inputs, rank as 0
          if ( degrees.isEmpty() ) {
            vertex.setProperty( PROPERTY_DEGREE, 0 );
          } else {
            // Find max degree of all inputs
            Integer value = Ordering.natural().nullsLast().max( degrees );
            if ( value != null ) {
              vertex.setProperty( PROPERTY_DEGREE, value + 1 );
            } else {
              // Degree of an input was missing, check inputs and try again
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
          // Only allow a vertex to exit the loop if it's degree is defined
          return !argument.getObject().getPropertyKeys().contains( PROPERTY_DEGREE );
        }
      } )
      .dedup()
      .groupBy( new PipeFunction<Vertex, Integer>() {
        @Override public Integer compute( Vertex vertex ) {
          return vertex.getProperty( PROPERTY_DEGREE );
        }
      }, new PipeFunction<Vertex, Vertex>() {
        @Override public Vertex compute( Vertex vertex ) {
          return vertex;
        }
      }, new PipeFunction<List<Vertex>, List<Vertex>>() {
        @Override public List<Vertex> compute( List<Vertex> group ) {
          // Group vertices by degree
          if ( !group.isEmpty() ) {
            Integer degree = group.get( 0 ).getProperty( PROPERTY_DEGREE );
            groups.putAll( degree, group );
          }
          return group;
        }
      } )
      .iterate();
    return groups;
  }

  protected void updateGrid( ListMultimap<Integer, Vertex> groups ) {
    // Go through each group in order
    for ( int degree = 0; degree < groups.size(); degree++ ) {
      Set<Integer> rowSet = Sets.newHashSet();
      for ( Vertex vertex : groups.get( degree ) ) {
        // Attempt to place step in same row as input
        List<Integer> inputRows = FluentIterable.from( vertex.getVertices( Direction.IN ) )
          .transform( GraphUtils.<Integer>getProperty( PROPERTY_ROW ) )
          .filter( Predicates.notNull() )
          .toList();

        int row = 0;
        for ( Integer input : inputRows ) {
          row += input;
        }
        if ( !inputRows.isEmpty() ) {
          row = (int) Math.ceil( row * 1.0 / inputRows.size() );
        }
        while ( rowSet.contains( row ) ) {
          row++;
        }

        vertex.setProperty( PROPERTY_COLUMN, degree );
        vertex.setProperty( PROPERTY_ROW, row );
        rowSet.add( row );
      }
    }
  }

  protected void updateXY( int canvasWidth, int canvasHeight, Iterable<Vertex> vertices ) {
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
