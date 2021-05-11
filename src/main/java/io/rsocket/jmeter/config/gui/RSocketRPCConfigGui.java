///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// */
//
//package io.rsocket.jmeter.config.gui;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Font;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//import javax.swing.ImageIcon;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.SwingConstants;
//
//import io.rsocket.jmeter.config.RSocketRPCConfig;
//import io.rsocket.rpc.annotations.internal.Generated;
//import io.rsocket.rpc.annotations.internal.ResourceType;
//import org.apache.commons.lang3.ArrayUtils;
//import org.apache.jmeter.config.Arguments;
//import org.apache.jmeter.config.gui.AbstractConfigGui;
//import org.apache.jmeter.config.gui.ArgumentsPanel;
//import org.apache.jmeter.gui.util.VerticalPanel;
//import org.apache.jmeter.protocol.java.sampler.JavaSampler;
//import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
//import org.apache.jmeter.testelement.TestElement;
//import org.apache.jmeter.util.JMeterUtils;
//import org.apache.jorphan.gui.JLabeledChoice;
//import org.apache.jorphan.reflect.ClassFinder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * The <code>JavaConfigGui</code> class provides the user interface for the
// * {@link RSocketRPCConfig} object.
// *
// */
//public class RSocketRPCConfigGui extends AbstractConfigGui {
//
//    private static final long serialVersionUID = 241L;
//
//    private static final List<Method> OBJECT_METHODS = Arrays.asList(Object.class.getMethods());
//
//    /** Logging */
//    private static final Logger log = LoggerFactory.getLogger(RSocketRPCConfigGui.class);
//
//    /** A combo box allowing the user to choose a test class. */
//    private JLabeledChoice classNameLabeledChoice;
//
//
//    /** A combo box allowing the user to choose a test method. */
//    private JLabeledChoice methodNameLabeledChoice;
//
//    /**
//     * Indicates whether or not the name of this component should be displayed
//     * as part of the GUI. If true, this is a standalone component. If false, it
//     * is embedded in some other component.
//     */
//    private boolean displayName = true;
//
//    /** A panel allowing the user to set arguments for this test. */
//    private ArgumentsPanel argsPanel;
//
//    /**
//     * Used in case the referenced class is not in classpath or does not implement {@link JavaSamplerClient}
//     */
//    private final JLabel warningLabel;
//
//
//    /**
//     * Create a new JavaConfigGui as a standalone component.
//     */
//    public RSocketRPCConfigGui() {
//        this(true);
//    }
//
//    /**
//     * Create a new JavaConfigGui as either a standalone or an embedded
//     * component.
//     *
//     * @param displayNameField
//     *            tells whether the component name should be displayed with the
//     *            GUI. If true, this is a standalone component. If false, this
//     *            component is embedded in some other component.
//     */
//    public RSocketRPCConfigGui(boolean displayNameField) {
//        this.displayName = displayNameField;
//        ImageIcon image = JMeterUtils.getImage("warning.png");
//        warningLabel = new JLabel(JMeterUtils.getResString("java_request_warning"), image, SwingConstants.LEFT); // $NON-NLS-1$
//        init();
//    }
//
//    @Override
//    public String getStaticLabel() {
//        return "RSocket RPC Client Configuration";
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public String getLabelResource() {
//        return "java_request_defaults"; // $NON-NLS-1$
//    }
//
//    /**
//     * Initialize the GUI components and layout.
//     */
//    private final void init() {// called from ctor, so must not be overridable
//        setLayout(new BorderLayout(0, 5));
//
//        if (displayName) {
//            setBorder(makeBorder());
//            add(makeTitlePanel(), BorderLayout.NORTH);
//        }
//
//        JPanel classnameRequestPanel = new JPanel(new BorderLayout(0, 5));
//        classnameRequestPanel.add(createClassnamePanel(), BorderLayout.NORTH);
//        classnameRequestPanel.add(createParameterPanel(), BorderLayout.CENTER);
//
//        add(classnameRequestPanel, BorderLayout.CENTER);
//    }
//
//    /**
//     * Create a panel with GUI components allowing the user to select a test
//     * class.
//     *
//     * @return a panel containing the relevant components
//     */
//    @SuppressWarnings("unchecked")
//    private JPanel createClassnamePanel() {
//        List<String> possibleClasses = new ArrayList<>();
//
//        try {
//            // Find all the classes which implement the JavaSamplerClient
//            // interface.
//            possibleClasses = ClassFinder.findClasses(
//                    JMeterUtils.getSearchPaths(),
//                    className -> {
//                        try {
//                            Class<?> clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
//                            Generated annotation = clazz.getAnnotation(Generated.class);
//
//                            return annotation != null && !clazz.getSimpleName().startsWith("Blocking") &&  annotation.type() == ResourceType.CLIENT;
//                        }
//                        catch (ClassNotFoundException e) {
//                            log.error("Ignored Class Name [{}]", e, className);
//                        }
//
//                        return false;
//                    }
//            );
//
//            // Remove all Blocking Client/Server classes from the list
//
//            log.info("Found Classes [{}]", possibleClasses);
//        } catch (Exception e) {
//            log.debug("Exception getting interfaces.", e);
//        }
//
//        classNameLabeledChoice = new JLabeledChoice(
//                JMeterUtils.getResString("protocol_java_classname"),
//                possibleClasses.toArray(ArrayUtils.EMPTY_STRING_ARRAY), true,
//                false);
//        classNameLabeledChoice.addChangeListener(evt -> configureMethodPanel());
//
//        warningLabel.setForeground(Color.RED);
//        Font font = warningLabel.getFont();
//        warningLabel.setFont(new Font(font.getFontName(), Font.BOLD, (int)(font.getSize()*1.1)));
//        warningLabel.setVisible(false);
//
//
//        methodNameLabeledChoice = new JLabeledChoice(JMeterUtils.getResString("protocol_java_classname"), false);
//
//        VerticalPanel panel = new VerticalPanel();
//        panel.add(classNameLabeledChoice);
//        panel.add(methodNameLabeledChoice);
//        panel.add(warningLabel);
//        return panel;
//    }
//
//    private void configureMethodPanel() {
//        String className = classNameLabeledChoice.getText().trim();
//
//        try {
//            Class<?> clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
//            methodNameLabeledChoice.setValues(new String[0]);
//            Arrays.stream(clazz.getMethods())
//                  .filter(m -> !OBJECT_METHODS.contains(m))
//                  .forEach(m -> methodNameLabeledChoice.addValue(
//                      m.getReturnType().getSimpleName()
//                      + " "
//                      + m.getName()
//                      + "("
//                      + Arrays.stream(m.getParameterTypes())
//                              .map(Class::getSimpleName)
//                              .collect(Collectors.joining(", "))
//                      + ")"
//                  ));
//        }
//        catch (ClassNotFoundException e) {
//            log.error("Error During Class's Methods Reading. [{}]", e, className);
//        }
//
//    }
//
//    /**
//     * Create a panel containing components allowing the user to provide
//     * arguments to be passed to the test class instance.
//     *
//     * @return a panel containing the relevant components
//     */
//    private JPanel createParameterPanel() {
//        argsPanel = new ArgumentsPanel(JMeterUtils.getResString("paramtable")); // $NON-NLS-1$
//        return argsPanel;
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void configure(TestElement config) {
//        super.configure(config);
//
//        argsPanel.configure((Arguments) config.getProperty(JavaSampler.ARGUMENTS).getObjectValue());
//
//        String className = config.getPropertyAsString(JavaSampler.CLASSNAME);
//        if(!checkContainsClassName(classNameLabeledChoice, className)) {
//            classNameLabeledChoice.addValue(className);
//        }
//
//        warningLabel.setVisible(!classOk(className));
//        classNameLabeledChoice.setText(className);
//        configureMethodPanel();
////        configureClassName();
//    }
//
//    /**
//     *
//     * @param className String class name
//     * @return true if class is ok (exist in classpath and instanceof {@link JavaSamplerClient}
//     */
//    private boolean classOk(String className) {
//        try {
//            Class.forName(className, true, Thread.currentThread().getContextClassLoader());
//            // Just to use client
//            return true;
//        } catch (Exception ex) {
//            log.error("Error creating class:'"+className+"' in JavaSampler "+getName()
//                +", check for a missing jar in your jmeter 'search_paths' and 'plugin_dependency_paths' properties",
//                ex);
//            return false;
//        }
//    }
//
//    /**
//     * Check combo contains className
//     * @param classnameChoice ComboBoxModel
//     * @param className String class name
//     * @return boolean
//     */
//    private static boolean checkContainsClassName(JLabeledChoice classnameChoice, String className) {
//        Set<String> set = new HashSet<>(Arrays.asList(classnameChoice.getItems()));
//        return set.contains(className);
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public TestElement createTestElement() {
//        RSocketRPCConfig config = new RSocketRPCConfig();
//        modifyTestElement(config);
//        return config;
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public void modifyTestElement(TestElement config) {
//        configureTestElement(config);
//        ((RSocketRPCConfig) config).setArguments((Arguments) argsPanel.createTestElement());
//        ((RSocketRPCConfig) config).setClassname(classNameLabeledChoice.getText().trim());
//    }
//
//    /* (non-Javadoc)
//     * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#clearGui()
//     */
//    @Override
//    public void clearGui() {
//        super.clearGui();
//        this.displayName = true;
//        this.warningLabel.setVisible(false);
//        argsPanel.clearGui();
//        classNameLabeledChoice.setSelectedIndex(0);
//        methodNameLabeledChoice.setValues(new String[0]);
//    }
//}
