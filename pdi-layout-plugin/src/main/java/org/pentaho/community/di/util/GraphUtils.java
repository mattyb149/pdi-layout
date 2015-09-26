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
package org.pentaho.community.di.util;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.LoopPipe;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GraphUtils {

  public static final String PROPERTY_NAME = "name";
  public static final String PROPERTY_REF = "ref";
  public static final String PROPERTY_PLUGINID = "pluginId";
  public static final String PROPERTY_X = "x";
  public static final String PROPERTY_Y = "y";

  public static final String EDGE_HOPSTO = "hops_to";

  public static Graph createGraph( EngineMetaInterface meta ) {
    if ( meta == null ) {
      return null;
    }
    Graph g = new TinkerGraph();
    if ( meta instanceof TransMeta ) {
      TransMeta transMeta = (TransMeta) meta;

      // Add nodes
      List<StepMeta> steps = transMeta.getSteps();
      if ( steps != null ) {
        for ( StepMeta step : steps ) {
          Vertex v = g.addVertex( null );
          v.setProperty( PROPERTY_NAME, step.getName() );
          v.setProperty( PROPERTY_PLUGINID, step.getStepID() );
          Point location = step.getLocation();
          v.setProperty( PROPERTY_X, location.x );
          v.setProperty( PROPERTY_Y, location.y );
          v.setProperty( PROPERTY_REF, step );
        }
      }
      int numHops = transMeta.nrTransHops();
      for ( int i = 0; i < numHops; i++ ) {
        TransHopMeta hop = transMeta.getTransHop( i );
        StepMeta fromStep = hop.getFromStep();
        StepMeta toStep = hop.getToStep();
        Vertex fromV = g.getVertices( PROPERTY_NAME, fromStep.getName() ).iterator().next();
        Vertex toV = g.getVertices( PROPERTY_NAME, toStep.getName() ).iterator().next();
        g.addEdge( null, fromV, toV, EDGE_HOPSTO );
      }
    } else if ( meta instanceof JobMeta ) {
      JobMeta jobMeta = (JobMeta) meta;
    }
    return g;
  }

  public static List<StepMeta> getInputSteps( Graph g ) {
    List<StepMeta> inputSteps = new ArrayList<>();
    GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<>( g );
    List<Vertex> vertices = pipe.V().filter( new PipeFunction<Vertex, Boolean>() {

      @Override
      public Boolean compute( Vertex vertex ) {
        return vertex != null && !vertex.getEdges( Direction.IN ).iterator().hasNext();
      }
    } ).toList();
    for ( Vertex v : vertices ) {
      inputSteps.add( (StepMeta) v.getProperty( PROPERTY_REF ) );
    }
    return inputSteps;
  }

  public static List<StepMeta> getLongestPath( Graph g ) {
    List<StepMeta> longestPath = new LinkedList<>();

    // g.V().out.loop(1){it.loops<1000}{true}.path().last()

    return longestPath;
  }

  /**
   * This is a loop closure that returns true if the current loop count is less than the given number, false otherwise
   *
   * @param <S> the type of object passed into the loop closure
   */
  protected static class NumLoops<S> implements PipeFunction<LoopPipe.LoopBundle<S>, Boolean> {
    private int numLoops = 1;

    public NumLoops( int numLoops ) {
      this.numLoops = numLoops;
    }

    @Override
    public Boolean compute( LoopPipe.LoopBundle argument ) {
      return argument.getLoops() < numLoops;
    }
  }
}
