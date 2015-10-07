package org.pentaho.community.di.layout.snake;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.pentaho.community.di.impl.provider.HorizontalLayout;
import org.pentaho.community.di.util.GraphUtils;

/**
 * @author nhudak
 */
public class SnakeLayout extends HorizontalLayout {

  public static final String PROPERTY_PAGE_NUMBER = "pageNumber";

  @Override public String getId() {
    return "snakeLayout";
  }

  @Override public String getName() {
    return "Horizontal Wrapped";
  }

  @Override public void applyLayout( Graph graph, int canvasWidth, int canvasHeight ) {
    System.out.println( "Snake quit messing around. Snake? ... SNAKE!!" );
    super.applyLayout( graph, canvasWidth, canvasHeight );
  }

  @Override protected void updateGrid( ListMultimap<Integer, Vertex> groups ) {
    super.updateGrid( groups );
    FluentIterable<Vertex> vertices = FluentIterable.from( groups.values() );

    // Get maximum number of rows to compute "page height"
    FluentIterable<Integer> rows = vertices
      .transform( GraphUtils.<Integer>getProperty( PROPERTY_ROW ) )
      .filter( Predicates.notNull() );
    final int pageHeight = rows.isEmpty() ? 0 : 1 + Ordering.natural().max( rows );

    // Wrap columns extending beyond the canvas
    for ( Vertex vertex : vertices ) {
      int column = vertex.getProperty( PROPERTY_COLUMN );
      int row = vertex.getProperty( PROPERTY_ROW );
      int pageNumber = column / COLUMN_FACTOR;
      vertex.setProperty( PROPERTY_PAGE_NUMBER, pageNumber );
      vertex.setProperty( PROPERTY_COLUMN, column - pageNumber * COLUMN_FACTOR );
      vertex.setProperty( PROPERTY_ROW, row + pageNumber * pageHeight );
    }
  }
}
