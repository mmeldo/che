package org.eclipse.che.ide.ui.smartTree;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.DefaultPresentationRenderer;
import org.eclipse.che.ide.util.dom.Elements;

import java.util.ArrayList;
import java.util.List;

public class SpeedSearchRender extends DefaultPresentationRenderer<Node> {

  private String searchRequest;
  private String searchPattern;
  private final String matchingStyle;

  public SpeedSearchRender(TreeStyles treeStyles, String matchingStyle) {
    super(treeStyles);
    this.matchingStyle = matchingStyle;
  }

  void setSearchRequest(String searchRequest) {
    this.searchRequest = searchRequest;
  }

  void setRequestPattern(String requestPattern) {
    this.searchPattern = requestPattern;
  }

  @Override
  public Element render(
      final Node node, final String domID, final Tree.Joint joint, final int depth) {
    // Initialize HTML elements.
    final Element rootContainer = super.render(node, domID, joint, depth);
    final Element nodeContainer = rootContainer.getFirstChildElement();

    if (searchRequest == null || searchRequest.isEmpty()) {
      return rootContainer;
    }

    Element item = nodeContainer.getElementsByTagName("span").getItem(0);
    String innerText = item.getInnerText();

    if (innerText.isEmpty()) {
      item = nodeContainer.getElementsByTagName("div").getItem(0).getFirstChildElement();
      innerText = item.getInnerText();
    }

    List<String> groups = getMatchings(innerText);
    if (groups.isEmpty()) {
      return rootContainer;
    }

    if (!innerText.toLowerCase().matches(searchPattern)) {
      return rootContainer;
    }

    item.setInnerText("");

    for (int i = 0; i < groups.size(); i++) {
      String groupValue = groups.get(i);
      SpanElement spanElement1 = (SpanElement) Elements.createSpanElement();
      SpanElement spanElement2 = (SpanElement) Elements.createSpanElement(matchingStyle);
      spanElement1.setInnerText(
          innerText.substring(0, innerText.toLowerCase().indexOf(groupValue)));
      int index = innerText.toLowerCase().indexOf(groupValue);
      spanElement2.setInnerText(innerText.substring(index, index + groupValue.length()));
      item.appendChild(spanElement1);
      item.appendChild(spanElement2);

      if (i == groups.size() - 1) {
        SpanElement spanElement3 = (SpanElement) Elements.createSpanElement();
        spanElement3.setInnerText(
            innerText.substring(innerText.toLowerCase().indexOf(groupValue) + groupValue.length()));
        item.appendChild(spanElement3);
      } else {
        innerText =
            innerText.substring(innerText.toLowerCase().indexOf(groupValue) + groupValue.length());
      }
    }

    return rootContainer;
  }

  private List<String> getMatchings(String input) {
    String group = "";
    List<String> groups = new ArrayList<>();
    for (int i = 0; i < searchRequest.length(); i++) {

      String value = String.valueOf(searchRequest.charAt(i)).toLowerCase();

      if (input.toLowerCase().contains(group + value)) {
        group += value;
        if (i == searchRequest.length() - 1) {
          groups.add(group);
          input = input.substring(input.indexOf(group) + group.length());
        }
      } else if (!group.isEmpty()) {
        groups.add(group);
        input = input.substring(input.indexOf(group) + group.length());
        if (i == searchRequest.length() - 1) {
          groups.add(value);
          input = input.substring(input.indexOf(group) + group.length());
        } else if (input.toLowerCase().contains(value)) {
          group = value;
        } else {
          group = "";
        }
      }
    }
    return groups;
  }
}
