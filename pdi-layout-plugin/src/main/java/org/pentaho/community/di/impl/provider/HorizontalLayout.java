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


import com.tinkerpop.blueprints.Graph;
import org.pentaho.community.di.api.LayoutProvider;
import org.pentaho.community.di.util.GraphUtils;
import org.pentaho.di.trans.step.StepMeta;

import java.util.List;

public class HorizontalLayout implements LayoutProvider {

  @Override
  public String getId() {
    return "horizontal";
  }

  @Override
  public String getName() {
    return "Horizontal Tree";
  }

  @Override
  public void applyLayout( Graph graph, int canvasWidth, int canvasHeight ) {

    if ( graph != null ) {

      List<StepMeta> longestPath = GraphUtils.getLongestPath( graph );

      // TBD traverse/query the graph and update the X/Y positions of the steps as needed
    }
  }
}