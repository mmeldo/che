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
package org.eclipse.che.ide.ui.smartTree;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.ui.smartTree.converter.NodeConverter;
import org.eclipse.che.ide.ui.smartTree.converter.impl.NodeNameConverter;
import org.eclipse.che.ide.ui.smartTree.data.Node;

import static com.google.gwt.dom.client.Style.BorderStyle.SOLID;
import static com.google.gwt.dom.client.Style.Position.FIXED;
import static com.google.gwt.dom.client.Style.Unit.PX;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_BACKSPACE;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ENTER;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ESCAPE;
import static org.eclipse.che.ide.api.theme.Style.theme;

/** @author Vlad Zhukovskiy */
public class SpeedSearch {

  private Tree tree;
  private final String style;
  private final boolean filterElements;
  private NodeConverter<Node, String> nodeConverter;
  private DelayedTask searchTask;
  private StringBuilder searchRequest;
  private SearchPopUp searchPopUp;
  private static final String ID = "speedSearch";

  private final SpeedSearchRender speedSearchRender;

  private int searchDelay;
  private List<Node> savedNodes;

  private class SearchPopUp extends HorizontalPanel {
    private Label searchText;

    private SearchPopUp() {
      getElement().setId(ID);
      setVisible(false); // by default

      Label icon = new Label();
      icon.getElement().setInnerHTML(FontAwesome.SEARCH);
      Style iconStyle = icon.getElement().getStyle();
      iconStyle.setFontSize(16, PX);
      iconStyle.setMarginLeft(5, PX);
      iconStyle.setMarginRight(5, PX);

      searchText = new Label();
      Style searchTextStyle = searchText.getElement().getStyle();
      searchTextStyle.setFontSize(12, PX);
      searchTextStyle.setMarginRight(5, PX);
      searchTextStyle.setMarginTop(4, PX);

      add(icon);
      add(searchText);
    }

    private void setSearchRequest(String request) {
      searchText.setText(request);
    }
  }

  SpeedSearch(
      Tree tree, String style, NodeConverter<Node, String> nodeConverter, boolean filterElements) {
    this.tree = tree;
    this.style = style;
    this.filterElements = filterElements;
    speedSearchRender = new SpeedSearchRender(tree.getTreeStyles(), style);
    this.tree.setPresentationRenderer(speedSearchRender);
    this.nodeConverter = nodeConverter != null ? nodeConverter : new NodeNameConverter();

    this.tree.addKeyPressHandler(
        event -> {
          event.stopPropagation();
          searchRequest.append(String.valueOf(event.getCharCode()));
          update();
        });

    this.tree.addKeyDownHandler(
        event -> {
          switch (event.getNativeKeyCode()) {
            case KEY_ENTER:
              removeSearchPopUpFromTreeIfVisible();
              break;
            case KEY_BACKSPACE:
              if (!Strings.isNullOrEmpty(searchRequest.toString())) {
                event.preventDefault();
                searchRequest.setLength(searchRequest.length() - 1);
                update();
              }
              break;
            case KEY_ESCAPE:
              if (searchRequest.length() != 0) {
                event.stopPropagation();
                searchRequest.setLength(0);
                update();
              }
              break;
          }
        });

    this.searchDelay = 100; // 100ms
    this.searchRequest = new StringBuilder();
    initSearchPopUp();
  }

  private void initSearchPopUp() {
    this.searchPopUp = new SearchPopUp();
    Style style = this.searchPopUp.getElement().getStyle();

    style.setBackgroundColor(theme.backgroundColor());
    style.setBorderStyle(SOLID);
    style.setBorderColor(theme.getPopupBorderColor());
    style.setBorderWidth(1, PX);
    style.setPadding(2, PX);
    style.setPosition(FIXED);
    style.setBottom(33, PX);
    style.setLeft(20, PX);
  }

  private void addSearchPopUpToTree() {
    if (Document.get().getElementById(ID) == null) {
      searchPopUp.setVisible(true);
      tree.getParent().getElement().appendChild(searchPopUp.getElement());
    }
  }

  private void removeSearchPopUpFromTreeIfVisible() {
    searchRequest.setLength(0);
    Element popUp = Document.get().getElementById(ID);
    if (popUp != null) {
      popUp.removeFromParent();
    }
  }

  protected void update() {
    if (searchTask == null) {
      searchTask =
          new DelayedTask() {
            @Override
            public void onExecute() {
              doSearch();
            }
          };
    }
    searchTask.delay(searchDelay);
  }

  protected void reset() {
    removeSearchPopUpFromTreeIfVisible();
    searchRequest.setLength(0);
    savedNodes = null;
  }

  private void doSearch() {
    if (Strings.isNullOrEmpty(searchRequest.toString())) {
      removeSearchPopUpFromTreeIfVisible();
    } else {
      addSearchPopUpToTree();
      searchPopUp.setSearchRequest(searchRequest.toString());
    }

    speedSearchRender.setSearchRequest(searchRequest.toString());
    speedSearchRender.setRequestPattern(getSearchPattern());

    tree.getSelectionModel().deselectAll();

    savedNodes = savedNodes == null ? getVisibleNodes() : savedNodes;

    List<Node> filter =
        savedNodes.stream().filter(matchesToSearchRequest()::apply).collect(Collectors.toList());
    NodeStorage nodeStorage = tree.getNodeStorage();

    if (filterElements) {
      for (Node savedNode : savedNodes) {
        if (filter.stream().noneMatch(node -> node.equals(savedNode))) {
          if ((filter
              .stream()
              .noneMatch(node -> node.getParent() != null && node.getParent().equals(savedNode)))) {
            nodeStorage.remove(savedNode);
          }
        } else if (getVisibleNodes().stream().noneMatch(node -> node.equals(savedNode))) {
          if (savedNode.getParent() == null) {
            nodeStorage.add(savedNode);
          } else {
            if (getVisibleNodes().stream().noneMatch(node -> node.equals(savedNode.getParent()))) {
              nodeStorage.add(savedNode.getParent());
            }
            List<Node> children =
                filter
                    .stream()
                    .filter(
                        node ->
                            node.getParent() != null
                                && node.getParent().equals(savedNode.getParent()))
                    .collect(Collectors.toList());
            nodeStorage.replaceChildren(savedNode.getParent(), children);
          }
        }
      }
    }
    getVisibleNodes().forEach(node -> tree.refresh(node));

    Optional<Node> startsOptional =
        filter
            .stream()
            .filter(
                node ->
                    node.getName().toLowerCase().startsWith(searchRequest.toString().toLowerCase()))
            .findFirst();
    Optional<Node> containsOptional =
        filter
            .stream()
            .filter(
                node ->
                    node.getName().toLowerCase().contains(searchRequest.toString().toLowerCase()))
            .findFirst();

    if (startsOptional.isPresent()) {
      tree.getSelectionModel().select(startsOptional.get(), true);
    } else if (containsOptional.isPresent()) {
      tree.getSelectionModel().select(containsOptional.get(), true);
    } else {
      filter.stream().findFirst().ifPresent(node -> tree.getSelectionModel().select(node, true));
    }
  }

  private List<Node> getVisibleNodes() {
    List<Node> rootNodes = tree.getRootNodes();
    return tree.getAllChildNodes(rootNodes, true);
  }

  private Predicate<Node> matchesToSearchRequest() {
    return inputNode -> {
      String nodeString = nodeConverter.convert(inputNode);
      return nodeString.toLowerCase().matches(getSearchPattern().toLowerCase());
    };
  }

  private String getSearchPattern() {
    StringBuilder pattern = new StringBuilder(".*");
    for (int i = 0; i < searchRequest.length(); i++) {
      pattern.append(searchRequest.charAt(i)).append(".*");
    }
    return pattern.toString().toLowerCase();
  }
}
