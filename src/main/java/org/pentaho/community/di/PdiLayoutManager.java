package org.pentaho.community.di;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.pentaho.community.di.api.LayoutProvider;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.Spoon;
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
import org.pentaho.ui.xul.jface.tags.JfaceMenupopup;

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
  private static final String PDI_LAYOUT_MANAGER = "pdiLayoutManager";

  private final List<LayoutProvider> providers;
  private final AtomicReference<XulMenupopup> menuPopup = new AtomicReference<>();

  public PdiLayoutManager( List<LayoutProvider> providers ) {
    this.providers = providers;
  }

  @Override
  public String getName() {
    return PDI_LAYOUT_MANAGER;
  }

  @Override
  public void applyToContainer( String category, XulDomContainer container ) throws XulException {
    ClassLoader cl = getClass().getClassLoader();
    container.registerClassLoader( cl );
    if ( category.equals( "trans-graph" ) ) {
      container.loadOverlay( "org/pentaho/community/di/trans_layout_menu_overlay.xul", bundle );
      container.addEventHandler( this );

      refreshProviders( (JfaceMenupopup) document.getElementById( "trans-graph-background-layout-popup" ) );
    }
  }

  private void refreshProviders( final JfaceMenupopup popupMenu ) {
    ( (MenuManager) popupMenu.getManagedObject() ).addMenuListener( new IMenuListener() {
      @Override public void menuAboutToShow( IMenuManager iMenuManager ) {
        popupMenu.removeChildren();
        for ( LayoutProvider provider : providers ) {
          XulMenuitem menuItem = popupMenu.createNewMenuitem();
          menuItem.setId( provider.getId() );
          menuItem.setLabel( provider.getName() );
          menuItem.setCommand( String.format( "%s.runLayout(\"%s\")", PDI_LAYOUT_MANAGER, provider.getId() ) );
        }
      }
    } );
  }

  public void runLayout( String id ) {
    for ( LayoutProvider provider : providers ) {
      if ( id.equals( provider.getId() ) ) {
        provider.applyLayout( Spoon.getInstance().getActiveMeta() );
        return;
      }
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
