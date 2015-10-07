package org.pentaho.community.di.impl.provider;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.util.structures.Pair;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.community.di.util.GraphUtils;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.junit.Assert.assertThat;

/**
 * @author nhudak
 */
public class HorizontalLayoutTest {
  private HorizontalLayout horizontalLayout;
  private Graph graph;

  @Before
  public void setUp() throws Exception {
    graph = new TinkerGraph();

    Vertex S0 = graph.addVertex( "S0" );
    Vertex S1 = graph.addVertex( "S1" );
    Vertex a = graph.addVertex( "A" );
    Vertex b = graph.addVertex( "B" );
    Vertex c = graph.addVertex( "C" );
    Vertex d = graph.addVertex( "D" );

    graph.addEdge( null, S0, a, GraphUtils.EDGE_HOPSTO );
    graph.addEdge( null, S1, b, GraphUtils.EDGE_HOPSTO );
    graph.addEdge( null, a, b, GraphUtils.EDGE_HOPSTO );
    graph.addEdge( null, b, c, GraphUtils.EDGE_HOPSTO );
    graph.addEdge( null, b, d, GraphUtils.EDGE_HOPSTO );

    // Expect layout
    // S0 -> A -> B -> C
    // S1 -------^ `-> D

    horizontalLayout = new HorizontalLayout();
  }

  @Test
  public void testApplyLayout() throws Exception {
    horizontalLayout.applyLayout( graph, 1000, 1000 );

    assertThat( getColumn( 0 ), containsInAnyOrder( "S0", "S1" ) );
    assertThat( getColumn( 1 ), contains( "A" ) );
    assertThat( getColumn( 2 ), contains( "B" ) );
    assertThat( getColumn( 3 ), containsInAnyOrder( "C", "D" ) );

    List<String> orderedX = new GremlinPipeline<>( graph ).V()
      .order( new PipeFunction<Pair<Vertex, Vertex>, Integer>() {
        @Override public Integer compute( Pair<Vertex, Vertex> argument ) {
          return Integer.compare(
            argument.getA().<Integer>getProperty( "x" ),
            argument.getB().<Integer>getProperty( "x" )
          );
        }
      } )
      .id().cast( String.class )
      .toList();

    assertThat( orderedX, containsInRelativeOrder( "S1", "A", "B", "D" ) );
  }

  private List<String> getColumn( int value ) {
    return new GremlinPipeline<Graph, Vertex>( graph ).V( "column", value ).id().cast( String.class ).toList();
  }
}
