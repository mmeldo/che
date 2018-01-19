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
package org.eclipse.che.ide.ui.smartTree.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Event fires after an node is redrawn.
 *
 * @author Igor Vinokur
 */
public class RedrawNodeEvent extends GwtEvent<RedrawNodeEvent.RedrawNodeHandler> {

  public interface RedrawNodeHandler extends EventHandler {
    void onExpand(RedrawNodeEvent event);
  }

  public interface HasRedrawItemHandlers {
    HandlerRegistration addRedrawHandler(RedrawNodeHandler handler);
  }

  private static Type<RedrawNodeHandler> TYPE;

  public static Type<RedrawNodeHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private Node node;

  public RedrawNodeEvent(Node node) {
    this.node = node;
  }

  @Override
  public Type<RedrawNodeHandler> getAssociatedType() {
    return TYPE;
  }

  public Node getNode() {
    return node;
  }

  @Override
  protected void dispatch(RedrawNodeHandler handler) {
    handler.onExpand(this);
  }
}
