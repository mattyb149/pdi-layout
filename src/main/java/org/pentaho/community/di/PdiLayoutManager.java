package org.pentaho.community.di;

import org.pentaho.community.di.api.LayoutProvider;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

@SpoonPlugin( id = "PdiLayoutManager", image = "" )
@SpoonPluginCategories( { "trans-graph" } )
public class PdiLayoutManager extends AbstractXulEventHandler implements SpoonPluginInterface {

  private static final ResourceBundle bundle = new ResourceBundle() {
    @Override
    public Enumeration<String> getKeys() {
      return Collections.emptyEnumeration();
    }

    @Override
    protected Object handleGetObject( String key ) {
      return BaseMessages.getString( PdiLayoutManager.class, key );
    }
  };

  private final List<LayoutProvider> providers;
  private final AtomicReference<XulMenupopup> menuPopup = new AtomicReference<>();

  public PdiLayoutManager( List<LayoutProvider> providers ) {
    this.providers = providers;
  }

  @Override
  public String getName() {
    return "pdiLayoutManager";
  }

  @Override
  public void applyToContainer( String category, XulDomContainer container ) throws XulException {
    ClassLoader cl = getClass().getClassLoader();
    container.registerClassLoader( cl );
    if ( category.equals( "trans-graph" ) ) {
      container.loadOverlay( "org/pentaho/community/di/trans_layout_menu_overlay.xul", bundle );
      container.addEventHandler( this );
      menuPopup.set( (XulMenupopup) document.getElementById( "trans-graph-background-layout-popup" ) );
      for ( LayoutProvider provider : providers ) {
        addProvider( provider );
      }
    }
  }

  public void addProvider( LayoutProvider provider ) {
    XulMenupopup xulMenupopup = menuPopup.get();
    if ( xulMenupopup == null ) {
      return;
    }
    try {
      XulMenuitem menuItem;
      menuItem = (XulMenuitem) document.createElement( "menuitem" );
      menuItem.setId( provider.getId() );
      menuItem.setLabel( provider.getName() );
      menuItem.setCommand( String.format( "%s.runLayout(\"%s\")", getName(), provider.getId() ) );
      xulMenupopup.addChild( menuItem );
    } catch ( XulException e ) {
      e.printStackTrace();
    }
  }

  @Override
  public SpoonLifecycleListener getLifecycleListener() {
    return null;
  }

  @Override
  public SpoonPerspective getPerspective() {
    return null;
  }
}
