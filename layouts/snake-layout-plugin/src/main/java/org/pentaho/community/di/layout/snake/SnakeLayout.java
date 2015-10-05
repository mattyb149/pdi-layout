package org.pentaho.community.di.layout.snake;

import com.tinkerpop.blueprints.Graph;
import org.pentaho.community.di.api.LayoutProvider;

/**
 * @author nhudak
 */
public class SnakeLayout implements LayoutProvider {
  public SnakeLayout() {
  }

  @Override public String getId() {
    return "snake";
  }

  @Override public String getName() {
    return "Wrap Horizontal";
  }

  @Override public void applyLayout( Graph graph, int canvasWidth, int canvasHeight ) {
    System.out.println( "Snake quit messing around. Snake? ... SNAKE!!" );
  }
}
