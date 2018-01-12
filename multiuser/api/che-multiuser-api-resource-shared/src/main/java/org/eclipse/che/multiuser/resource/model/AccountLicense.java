/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.model;

import java.util.List;

/**
 * Permits account to use some resources.
 *
 * @author gazarenkov
 * @author Sergii Leschenko
 */
public interface AccountLicense {
  /** Returns id of account that is owner of this license. */
  String getAccountId();

  /** Returns detailed list of resources which can be used by owner. */
  List<? extends ProvidedResources> getResourcesDetails();

  /** Returns list of resources which can be used by owner. */
  List<? extends Resource> getTotalResources();
}
