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

package org.pentaho.community.di.impl;

import org.pentaho.community.di.api.LayoutProvider;
import org.pentaho.community.di.api.LayoutService;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mburgess on 9/16/15.
 */
public class LayoutServiceImpl implements LayoutService {

  Set<LayoutProvider> providers = new HashSet<>();

  @Override
  public Set<LayoutProvider> getLayoutProviders() {
    return providers;
  }

  public void addLayoutProvider( LayoutProvider provider ) {
    if ( provider != null ) {
      providers.add( provider );
    }
  }

  public void removeLayoutProvider( LayoutProvider provider ) {
    if ( provider != null ) {
      providers.remove( provider );
    }
  }
}
