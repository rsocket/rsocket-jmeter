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

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.util.JMeterUtils;

/**
 * A GUI panel allowing the user to enter HTTP Parameters.
 * These have names and values, as well as check-boxes to determine whether or not to
 * include the "=" sign in the output and whether or not to encode the output.
 */
public class CompositeMetadataPanel extends ArgumentsPanel {

    private static final long serialVersionUID = 240L;

    /** When pasting from the clipboard, split lines on linebreak or '&' */
    private static final String CLIPBOARD_LINE_DELIMITERS = "\n|&"; //$NON-NLS-1$

    /** When pasting from the clipboard, split parameters on tab or '=' */
    private static final String CLIPBOARD_ARG_DELIMITERS = "\t|="; //$NON-NLS-1$

    public static boolean testFunctors(){
        CompositeMetadataPanel instance = new CompositeMetadataPanel();
        instance.initializeTableModel();
        return instance.tableModel.checkFunctors(null,instance.getClass());
    }

    public CompositeMetadataPanel() {
        super(JMeterUtils.getResString("compositemetadatatable")); //$NON-NLS-1$
        init();
        clearBorderForMainPanel();
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)

        // register the right click menu
        JTable table = getTable();
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem variabilizeItem = new JMenuItem(JMeterUtils.getResString("transform_into_variable"));
        variabilizeItem.addActionListener(e -> transformNameIntoVariable());
        popupMenu.add(variabilizeItem);
        table.setComponentPopupMenu(popupMenu);
    }

    /**
     * replace the argument value of the selection with a variable
     * the variable name is derived from the parameter name
     */
    private void transformNameIntoVariable() {
        int[] rowsSelected = getTable().getSelectedRows();
        for (int selectedRow : rowsSelected) {
            String name = (String) tableModel.getValueAt(selectedRow, 0);
            if (StringUtils.isNotBlank(name)) {
                name = name.trim();
                name = name.replaceAll("\\$", "_");
                name = name.replaceAll("\\{", "_");
                name = name.replaceAll("\\}", "_");
                tableModel.setValueAt("${" + name + "}", selectedRow, 1);
            }
        }
    }

}
