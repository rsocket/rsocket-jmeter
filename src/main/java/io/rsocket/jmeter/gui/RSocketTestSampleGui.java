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

package io.rsocket.jmeter.gui;

import io.rsocket.jmeter.RSocketSamplerBase;
import io.rsocket.jmeter.config.gui.RequestConfigGui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * HTTP Sampler GUI
 */
@GUIMenuSortOrder(1)
@TestElementMetadata(labelResource = "rsocket_testing_title")
public class RSocketTestSampleGui extends AbstractSamplerGui {

  private static final long serialVersionUID = 242L;

  private RequestConfigGui requestConfigGui;
//  private JTextField connectTimeOut;
//  private JTextField responseTimeOut;

  private final boolean isAJP;

  public RSocketTestSampleGui() {
    isAJP = false;
    init();
  }

  // For use by AJP
  protected RSocketTestSampleGui(boolean ajp) {
    isAJP = ajp;
    init();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void configure(TestElement element) {
    super.configure(element);
    final RSocketSamplerBase samplerBase = (RSocketSamplerBase) element;
    requestConfigGui.configure(element);
    if (!isAJP) {
//      connectTimeOut.setText(samplerBase.getPropertyAsString(RSocketSamplerBase.CONNECT_TIMEOUT));
//      responseTimeOut.setText(samplerBase.getPropertyAsString(RSocketSamplerBase.RESPONSE_TIMEOUT));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TestElement createTestElement() {
    RSocketSamplerBase sampler = new SimpleRSocketSampler();
    modifyTestElement(sampler);
    return sampler;
  }

  /**
   * Modifies a given TestElement to mirror the data in the gui components.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void modifyTestElement(TestElement sampler) {
    sampler.clear();
    requestConfigGui.modifyTestElement(sampler);
    final RSocketSamplerBase samplerBase = (RSocketSamplerBase) sampler;
    if (!isAJP) {
//      samplerBase.setProperty(RSocketSamplerBase.CONNECT_TIMEOUT, connectTimeOut.getText());
//      samplerBase.setProperty(RSocketSamplerBase.RESPONSE_TIMEOUT, responseTimeOut.getText());
    }
    super.configureTestElement(sampler);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLabelResource() {
    return "web_testing_title"; // $NON-NLS-1$
  }

  private void init() {// called from ctor, so must not be overridable
    setLayout(new BorderLayout(0, 5));
    setBorder(BorderFactory.createEmptyBorder());

    // URL CONFIG
    requestConfigGui = createRequestConfigGui();

    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setBorder(makeBorder());
    wrapper.add(makeTitlePanel(), BorderLayout.CENTER);

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, wrapper, requestConfigGui);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    splitPane.setOneTouchExpandable(true);
    add(splitPane);
  }

  /**
   * Create a {@link RequestConfigGui} which is used as the Basic tab in the parameters configuration
   * tabstrip.
   *
   * @return a {@link RequestConfigGui} which is used as the Basic tab
   */
  protected RequestConfigGui createRequestConfigGui() {
    final RequestConfigGui configGui = new RequestConfigGui(true, true, true);
    configGui.setBorder(makeBorder());
    return configGui;
  }

//  private JPanel createAdvancedConfigPanel() {
//    // HTTP request options
//    JPanel httpOptions = new HorizontalPanel();
//    httpOptions.add(getTimeOutPanel());
//
//    // AdvancedPanel (embedded resources, source address and optional tasks)
//    JPanel advancedPanel = new VerticalPanel();
//    advancedPanel.setBorder(makeBorder());
//    if (!isAJP) {
//      advancedPanel.add(httpOptions);
//    }
//    advancedPanel.add(createOptionalTasksPanel());
//    return advancedPanel;
//  }
//
//  private JPanel getTimeOutPanel() {
//    JPanel timeOut = new HorizontalPanel();
//    timeOut.setBorder(BorderFactory.createTitledBorder(
//        JMeterUtils.getResString("web_server_timeout_title"))); // $NON-NLS-1$
//    final JPanel connPanel = getConnectTimeOutPanel();
//    final JPanel reqPanel = getResponseTimeOutPanel();
//    timeOut.add(connPanel);
//    timeOut.add(reqPanel);
//    return timeOut;
//  }

//  private JPanel getConnectTimeOutPanel() {
//    connectTimeOut = new JTextField(10);
//
//    JLabel label = new JLabel(
//        JMeterUtils.getResString("web_server_timeout_connect")); // $NON-NLS-1$
//    label.setLabelFor(connectTimeOut);
//
//    JPanel panel = new JPanel(new BorderLayout(5, 0));
//    panel.add(label, BorderLayout.WEST);
//    panel.add(connectTimeOut, BorderLayout.CENTER);
//
//    return panel;
//  }

//  private JPanel getResponseTimeOutPanel() {
//    responseTimeOut = new JTextField(10);
//
//    JLabel label = new JLabel(
//        JMeterUtils.getResString("web_server_timeout_response")); // $NON-NLS-1$
//    label.setLabelFor(responseTimeOut);
//
//    JPanel panel = new JPanel(new BorderLayout(5, 0));
//    panel.add(label, BorderLayout.WEST);
//    panel.add(responseTimeOut, BorderLayout.CENTER);
//
//    return panel;
//  }

  private JTextField addTextFieldWithLabel(JPanel panel, String labelText) {
    JLabel label = new JLabel(labelText); // $NON-NLS-1$
    JTextField field = new JTextField(100);
    label.setLabelFor(field);
    panel.add(label);
    panel.add(field, "span");
    return field;
  }

  protected JPanel createOptionalTasksPanel() {
    // OPTIONAL TASKS
    final JPanel checkBoxPanel = new VerticalPanel();
    checkBoxPanel.setBorder(BorderFactory.createTitledBorder(
        JMeterUtils.getResString("optional_tasks"))); // $NON-NLS-1
    return checkBoxPanel;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Dimension getPreferredSize() {
    return getMinimumSize();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clearGui() {
    super.clearGui();
    requestConfigGui.clear();
    if (!isAJP) {
//      connectTimeOut.setText(""); // $NON-NLS-1$
//      responseTimeOut.setText(""); // $NON-NLS-1$
    }
  }

}
