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
package org.eclipse.che.selenium.core.client;

/** @author Anatolii Bazko */
public interface TestAuthServiceClient {

  /** Logs user into the system and returns auth token. */
  String login(String username, String password) throws Exception;

  void logout(String token) throws Exception;
}
