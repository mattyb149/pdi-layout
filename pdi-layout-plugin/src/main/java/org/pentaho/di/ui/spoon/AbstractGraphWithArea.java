package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.gui.Point;

/**
 * Hacky class to get at the protected getArea() method of AbstractGraph
 */
public class AbstractGraphWithArea extends AbstractGraph {

  private final AbstractGraph parent;

  public AbstractGraphWithArea( AbstractGraph parent ) {
    super( parent, parent.getStyle());
    this.parent = parent;
  }

  @Override
  protected Point getOffset() {
    return null;
  }

  public Point getCanvasArea() {
    return parent.getArea();
  }
}
