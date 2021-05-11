/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.rsocket.jmeter;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.apache.jmeter.processor.gui.AbstractPreProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * The GUI for SampleTimeout.
 */
public class RSocketSetupGui extends AbstractPreProcessorGui {

  private static final long serialVersionUID = 240L;

  /**
   * The default value for the timeout.
   */
  private static final String DEFAULT_HOST = "localhost";
  private static final String DEFAULT_PORT = "8080";
  private static final String DEFAULT_TRANSPORT = "TCP";
  private static final String DEFAULT_TRANSPORT_WEBSOCKET_PATH = "/rsocket";

  private JTextField hostField;
  private JTextField portField;
  private JComboBox<String> transportField;
  private JTextField transportWebsocketPathField;

  /**
   * No-arg constructor.
   */
  public RSocketSetupGui() {
    init();
  }

  /**
   * Handle an error.
   *
   * @param e       the Exception that was thrown.
   * @param thrower the JComponent that threw the Exception.
   */
  public static void error(Exception e, JComponent thrower) {
    JOptionPane.showMessageDialog(thrower, e, "Error", JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public String getStaticLabel() {
    return "RSocket Setup";
  }

  @Override
  public String getLabelResource() {
    return "sample_timeout_title"; // $NON-NLS-1$
  }

  /**
   * Create the test element underlying this GUI component.
   *
   * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
   */
  @Override
  public TestElement createTestElement() {
    RSocketSetup RSocketSetup = new RSocketSetup();
    modifyTestElement(RSocketSetup);
    return RSocketSetup;
  }

  /**
   * Modifies a given TestElement to mirror the data in the gui components.
   *
   * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
   */
  @Override
  public void modifyTestElement(TestElement el) {
    super.configureTestElement(el);

    RSocketSetup RSocketSetup = (RSocketSetup) el;

    RSocketSetup.setTransport(transportField.getSelectedItem().toString());
    RSocketSetup.setHost(hostField.getText());
    try {
      RSocketSetup.setPort(Integer.parseInt(portField.getText()));
    } catch (NumberFormatException ignored) {
    }
    RSocketSetup.setTransportWebsocketPath(transportWebsocketPathField.getText());
  }

  /**
   * Configure this GUI component from the underlying TestElement.
   *
   * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
   */
  @Override
  public void configure(TestElement el) {
    super.configure(el);

    RSocketSetup RSocketSetup = (RSocketSetup) el;

    hostField.setText(RSocketSetup.getHost());
    portField.setText(String.valueOf(RSocketSetup.getPort()));
    transportField.setSelectedItem(RSocketSetup.getTransport());
    transportWebsocketPathField.setText(RSocketSetup.getTransportWebsocketPath());
  }

  /**
   * Initialize this component.
   */
  private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
    setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));

    setBorder(makeBorder());
    add(makeTitlePanel());

    Box hostAndPortPanel = Box.createHorizontalBox();
    JLabel hostLabel = new JLabel("RSocket Server Host");//$NON-NLS-1$
    hostAndPortPanel.add(hostLabel);
    hostField = new JTextField(6);
    hostField.setText(DEFAULT_HOST);
    hostAndPortPanel.add(hostField);
    JLabel portLabel = new JLabel("Port");//$NON-NLS-1$
    hostAndPortPanel.add(portLabel);
    portField = new JTextField(6);
    portField.setText(DEFAULT_PORT);
    hostAndPortPanel.add(portField);

    Box transportPanel = Box.createHorizontalBox();
    JLabel transportLabel = new JLabel("RSocket Transport Type");//$NON-NLS-1$
    transportPanel.add(transportLabel);
    transportField = new JComboBox<>(new String[]{"TCP", "WS"});
    transportField.setSelectedItem(DEFAULT_TRANSPORT);
    transportPanel.add(transportField);
    JLabel wsPathLabel = new JLabel("Path");//$NON-NLS-1$
    transportPanel.add(wsPathLabel);
    transportWebsocketPathField = new JTextField(6);
    transportWebsocketPathField.setText(DEFAULT_TRANSPORT_WEBSOCKET_PATH);
    transportWebsocketPathField.setVisible(false);
    transportField
        .addItemListener(e -> transportWebsocketPathField.setVisible(e.getItem().equals("WS")));
    transportPanel.add(transportWebsocketPathField);

    add(hostAndPortPanel);
    add(transportPanel);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clearGui() {
    transportWebsocketPathField.setText(DEFAULT_TRANSPORT_WEBSOCKET_PATH);
    transportField.setSelectedItem(DEFAULT_TRANSPORT);
    hostField.setText(DEFAULT_HOST);
    portField.setText(DEFAULT_PORT);

    super.clearGui();
  }
}
