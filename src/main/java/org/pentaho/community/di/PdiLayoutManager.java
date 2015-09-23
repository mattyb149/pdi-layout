package org.pentaho.community.di;

import org.pentaho.community.di.api.LayoutProvider;
import org.pentaho.di.ui.spoon.ISpoonMenuController;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

@SpoonPlugin( id = "PdiLayoutManager", image = "" )
@SpoonPluginCategories( { "trans-graph" } )
public class PdiLayoutManager extends AbstractXulEventHandler
  implements SpoonPluginInterface, ISpoonMenuController, SpoonLifecycleListener {

  List<LayoutProvider> providers = new ArrayList<>();

  ResourceBundle bundle = new ResourceBundle() {
    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject( String key ) {
      return PdiLayoutManager.class.getName();
    }
  };

  public PdiLayoutManager(List<LayoutProvider> providers) {
    this.providers = providers;
  }

  @Override
  public void applyToContainer( String category, XulDomContainer container ) throws XulException {
    ClassLoader cl = getClass().getClassLoader();
    container.registerClassLoader( cl );
    if ( category.equals( "spoon" ) || category.equals( "trans-graph" ) ) {
      container.loadOverlay( "spoon_overlays.xul", bundle );
      container.addEventHandler( this );
    }
  }

  @Override
  public SpoonLifecycleListener getLifecycleListener() {
    return this;
  }

  @Override
  public SpoonPerspective getPerspective() {
    return null;
  }

  @Override
  public void updateMenu( Document doc ) {
    // Empty method
  }

  @Override
  public String getName() {
    return "pdiLayoutManager";
  }

  @Override
  public void onEvent( SpoonLifeCycleEvent spoonLifeCycleEvent ) {

  }

  public void init() {

  }

  public List<LayoutProvider> getLayoutProviders() {
    return providers;
  }
}
