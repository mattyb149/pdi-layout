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
package org.pentaho.community.di;

import com.google.common.collect.ImmutableMap;
import com.tinkerpop.blueprints.Graph;
import org.pentaho.community.di.api.LayoutProvider;
import org.pentaho.community.di.util.GraphUtils;
import org.pentaho.community.di.util.LayoutUtils;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
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

      for ( LayoutProvider provider : providers ) {
        addProvider( provider );
      }
    }
  }

  public void addProvider( final LayoutProvider provider ) throws XulException {
    // Ensure spoon has loaded the menu overlay
    if ( document == null || xulDomContainer == null ) {
      return;
    }

    // Check if this provider is already added
    XulComponent element = document.getElementById( provider.getId() );
    if ( element != null ) {
      element.setVisible( true );
    } else {
      xulDomContainer.loadOverlay( "org/pentaho/community/di/trans_layout_provider_template.xul", new ResourceBundle() {

        Map<String, String> propertyMap = ImmutableMap.of( "id", provider.getId(), "name", provider.getName() );

        @Override
        public Enumeration<String> getKeys() {
          return Collections.enumeration( propertyMap.keySet() );
        }

        @Override
        protected Object handleGetObject( String key ) {
          return propertyMap.get( key );
        }
      } );
    }

    // Hide the placeholder
    document.getElementById( "layout-placeholder" ).setVisible( false );
  }

  public void runLayout( String id ) {

    Spoon spoon = Spoon.getInstance();
    // Either a Job or Transformation is active, both are AbstractGraphs
    EngineMetaInterface meta = spoon.getActiveMeta();

    if ( meta != null ) {
      Point canvasDimensions = LayoutUtils.getGraphDimensions( meta );
      Graph g = GraphUtils.createGraph( meta );
      for ( LayoutProvider provider : providers ) {
        if ( id.equals( provider.getId() ) ) {
          provider.applyLayout( g, canvasDimensions.x, canvasDimensions.y );
          LayoutUtils.applyGraphToMeta( g );

          // Normalize layout so all steps/entries are visible on the canvas
          LayoutUtils.fitToBounds( g );
          return;
        }
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
