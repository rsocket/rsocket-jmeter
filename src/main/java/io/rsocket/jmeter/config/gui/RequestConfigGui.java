/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rsocket.jmeter.config.gui;

import io.rsocket.jmeter.RSocketSamplerBase;
import io.rsocket.jmeter.gui.CompositeMetadataPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.protocol.http.config.gui.UrlConfigDefaults;
import org.apache.jmeter.protocol.http.gui.HTTPFileArgsPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * Basic URL / HTTP Request configuration:
 * <ul>
 * <li>host and port</li>
 * <li>connect and response timeouts</li>
 * <li>path, method, encoding, parameters</li>
 * <li>redirects and keepalive</li>
 * </ul>
 */
public class RequestConfigGui extends JPanel implements ChangeListener {

  private static final long serialVersionUID = 240L;

  /**
   * Default value settings for URL Configuration GUI elements.
   */
  private static final RequestConfigDefaults REQUEST_CONFIG_DEFAULTS = new RequestConfigDefaults();

  private static final int TAB_PARAMETERS = 0;

  private int tabRawBodyIndex = 1;

  private int tabFileUploadIndex = 2;

  private CompositeMetadataPanel metadataPanel;
  private HTTPFileArgsPanel filesPanel;

  private JLabeledTextField contentEncoding;

  private JLabeledTextField route;

  private JLabeledChoice requestType;

  // set this false to suppress some items for use in HTTP Request defaults
  private final boolean notConfigOnly;

  // Body data
  private JSyntaxTextArea postBodyContent;

  // Tabbed pane that contains parameters and raw body
  private JTabbedPane postContentTabbedPane;

  private boolean showRawBodyPane;
  private boolean showFileUploadPane;

  /**
   * Constructor which is setup to show HTTP implementation, raw body pane and sampler fields.
   */
  public RequestConfigGui() {
    this(true);
  }

  /**
   * Constructor which is setup to show HTTP implementation and raw body pane.
   *
   * @param showSamplerFields flag whether sampler fields should be shown.
   */
  public RequestConfigGui(boolean showSamplerFields) {
    this(showSamplerFields, true);
  }

  /**
   * @param showSamplerFields flag whether sampler fields should be shown
   * @param showRawBodyPane   flag whether the raw body pane should be shown
   */
  public RequestConfigGui(boolean showSamplerFields, boolean showRawBodyPane) {
    this(showSamplerFields, showRawBodyPane, false);
  }

  /**
   * @param showSamplerFields  flag whether sampler fields should be shown
   * @param showRawBodyPane    flag whether the raw body pane should be shown
   * @param showFileUploadPane flag whether the file upload pane should be shown
   */
  public RequestConfigGui(boolean showSamplerFields, boolean showRawBodyPane,
      boolean showFileUploadPane) {
    this.notConfigOnly = showSamplerFields;
    this.showRawBodyPane = showRawBodyPane;
    this.showFileUploadPane = showFileUploadPane;
    init();
  }

  public void clear() {
    if (notConfigOnly) {
      requestType.setText(getRequestConfigDefaults().getDefaultRequestType());
    }
    route.setText(""); // $NON-NLS-1$
    contentEncoding.setText(""); // $NON-NLS-1$
    metadataPanel.clear();
    if (showFileUploadPane) {
      filesPanel.clear();
    }
    if (showRawBodyPane) {
      postBodyContent.setInitialText("");// $NON-NLS-1$
    }
    postContentTabbedPane.setSelectedIndex(TAB_PARAMETERS);
  }

  public TestElement createTestElement() {
    ConfigTestElement element = new ConfigTestElement();

    element.setName(this.getName());
    element.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
    element.setProperty(TestElement.TEST_CLASS, element.getClass().getName());
    modifyTestElement(element);
    return element;
  }

  /**
   * Save the GUI values in the sampler.
   *
   * @param element {@link TestElement} to modify
   */
  public void modifyTestElement(TestElement element) {
    String data = postBodyContent.getText();
    Arguments metadata = (Arguments) metadataPanel.createTestElement();

//    if (showFileUploadPane) {
//      filesPanel.modifyTestElement(element);
//    }
    element.setProperty(RSocketSamplerBase.DATA, data);
    element.setProperty(new TestElementProperty(RSocketSamplerBase.METADATA, metadata));
    element.setProperty(RSocketSamplerBase.CONTENT_ENCODING, contentEncoding.getText());
    element.setProperty(RSocketSamplerBase.ROUTE, route.getText());
    if (notConfigOnly) {
      element.setProperty(RSocketSamplerBase.REQUEST_TYPE, requestType.getText());
    }
  }

  /**
   * Set the text, etc. in the UI.
   *
   * @param el contains the data to be displayed
   */
  public void configure(TestElement el) {
    setName(el.getName());
    Arguments arguments = (Arguments) el.getProperty(RSocketSamplerBase.METADATA).getObjectValue();
    String postBody = el.getPropertyAsString(RSocketSamplerBase.DATA,
        ""); // Convert CRLF to CR, see modifyTestElement

    postBodyContent.setInitialText(postBody);
    postBodyContent.setCaretPosition(0);
    postContentTabbedPane.setSelectedIndex(tabRawBodyIndex);
    metadataPanel.configure(arguments);

    if (showFileUploadPane) {
      filesPanel.configure(el);
    }

    contentEncoding.setText(el.getPropertyAsString(RSocketSamplerBase.CONTENT_ENCODING));
    route.setText(el.getPropertyAsString(RSocketSamplerBase.ROUTE));
    if (notConfigOnly) {
      requestType.setText(el.getPropertyAsString(RSocketSamplerBase.REQUEST_TYPE));
    }
  }

  private void init() {// called from ctor, so must not be overridable
    this.setLayout(new BorderLayout());

    // WEB REQUEST PANEL
    JPanel webRequestPanel = new JPanel();
    webRequestPanel.setLayout(new BorderLayout());
    webRequestPanel.setBorder(BorderFactory.createTitledBorder(
        JMeterUtils.getResString("web_request"))); // $NON-NLS-1$

    webRequestPanel.add(getPathPanel(), BorderLayout.NORTH);
    webRequestPanel.add(getParameterPanel(), BorderLayout.CENTER);

    this.add(getWebServerPanel(), BorderLayout.NORTH);
    this.add(webRequestPanel, BorderLayout.CENTER);
  }

  /**
   * Create a panel containing the webserver (domain+port) and scheme.
   *
   * @return the panel
   */
  protected final JPanel getWebServerPanel() {
    // PROTOCOL
    JPanel webServerPanel = new HorizontalPanel();
    webServerPanel.setBorder(BorderFactory.createTitledBorder(
        JMeterUtils.getResString("web_server"))); // $NON-NLS-1$
    return webServerPanel;
  }

  /**
   * Return the {@link UrlConfigDefaults} instance to be used when configuring the UI elements and
   * default values.
   *
   * @return the {@link UrlConfigDefaults} instance to be used when configuring the UI elements and
   * default values
   */
  protected RequestConfigDefaults getRequestConfigDefaults() {
    return REQUEST_CONFIG_DEFAULTS;
  }

  /**
   * This method defines the Panel for: the HTTP path, Method and Content Encoding 'Follow
   * Redirects', 'Use KeepAlive', and 'Use multipart for HTTP POST' elements.
   *
   * @return JPanel The Panel for the path, 'Follow Redirects' and 'Use KeepAlive' elements.
   */
  protected Component getPathPanel() {
    route = new JLabeledTextField(JMeterUtils.getResString("route"), 80); //$NON-NLS-1$
    // CONTENT_ENCODING
    contentEncoding = new JLabeledTextField(JMeterUtils.getResString("content_encoding"),
        7); // $NON-NLS-1$

    if (notConfigOnly) {
      requestType = new JLabeledChoice(JMeterUtils.getResString("requestTypes"), // $NON-NLS-1$
          getRequestConfigDefaults().getValidRequestTypes(), true, false);
      requestType.addChangeListener(this);
    }

    JPanel pathPanel = new HorizontalPanel();
    if (notConfigOnly) {
      pathPanel.add(requestType);
    }
    pathPanel.add(route);
    pathPanel.add(contentEncoding);
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(pathPanel);
    if (notConfigOnly) {
      JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      optionPanel.setMinimumSize(optionPanel.getPreferredSize());
      panel.add(optionPanel);
    }

    return panel;
  }

  protected JTabbedPane getParameterPanel() {
    postContentTabbedPane = new JTabbedPane();
    metadataPanel = new CompositeMetadataPanel();
    postContentTabbedPane
        .add(JMeterUtils.getResString("post_as_parameters"), metadataPanel);// $NON-NLS-1$

    int indx = TAB_PARAMETERS;
    if (showRawBodyPane) {
      tabRawBodyIndex = ++indx;
      postBodyContent = JSyntaxTextArea.getInstance(30, 50);// $NON-NLS-1$
      postContentTabbedPane.add(JMeterUtils.getResString("post_body"),
          JTextScrollPane.getInstance(postBodyContent));// $NON-NLS-1$
    }

    if (showFileUploadPane) {
      tabFileUploadIndex = ++indx;
      filesPanel = new HTTPFileArgsPanel();
      postContentTabbedPane.add(JMeterUtils.getResString("post_files_upload"), filesPanel);
    }
    return postContentTabbedPane;
  }

  @Override
  public void stateChanged(ChangeEvent e) {

  }
}
