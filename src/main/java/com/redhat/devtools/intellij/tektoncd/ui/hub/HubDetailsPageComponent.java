/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.ui.hub;

import com.intellij.icons.AllIcons;
import com.intellij.ide.plugins.LinkPanel;
import com.intellij.ide.plugins.MultiPanel;
import com.intellij.ide.plugins.newui.VerticalLayout;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceData;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceVersionData;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.parser.MarkdownParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubDetailsPageComponent extends MultiPanel {
    private static final Logger logger = LoggerFactory.getLogger(HubDetailsPageComponent.class);

    private OpaquePanel myPanel;
    public static final Color MAIN_BG_COLOR = JBColor.namedColor("Plugins.background", new JBColor(() -> JBColor.isBright() ? UIUtil.getListBackground() : new Color(0x313335)));
    private JLabel myIconLabel, installBtn;
    private JPanel myNameAndButtons;
    private JLabel myNameComponent;
    public static final Color GRAY_COLOR = JBColor.namedColor("Label.infoForeground", new JBColor(Gray._120, Gray._135));
    public static final Color SEARCH_FIELD_BORDER_COLOR = JBColor.namedColor("Plugins.SearchField.borderColor", new JBColor(0xC5C5C5, 0x515151));

    private JComboBox versionsCmb;
    private JLabel myRating;
    private JEditorPane myDetailsComponent, myDescriptionComponent, myYamlComponent;
    private LinkPanel myHomePage;
    private JBPanelWithEmptyText myEmptyPanel;
    private JEditorPane myTopDescription;
    private HubModel model;

    public HubDetailsPageComponent(HubModel model) {
        this.model = model;
        createDetailsPanel();
        createEmptyPanel();
        select(1, true);
    }

    private void createEmptyPanel() {
        myEmptyPanel = new JBPanelWithEmptyText();
        myEmptyPanel.setBorder(new CustomLineBorder(SEARCH_FIELD_BORDER_COLOR, JBUI.insets(1, 0, 0, 0)));
        myEmptyPanel.setOpaque(true);
        myEmptyPanel.setBackground(MAIN_BG_COLOR);
        myEmptyPanel.getEmptyText().setText("Select Tekton Hub item to preview details");
    }

    private void createDetailsPanel() {
        myPanel = new OpaquePanel(new BorderLayout(0, JBUIScale.scale(32)), MAIN_BG_COLOR);
        myPanel.setBorder(new CustomLineBorder(new JBColor(0xC5C5C5, 0x515151), JBUI.insets(1, 0, 0, 0)) {
            @Override
            public Insets getBorderInsets(Component c) {
                return JBUI.insets(15, 0, 0, 0);
            }
        });

        createHeaderPanel().add(createCenterPanel());
        createBottomPanel();
    }

    @NotNull
    private JPanel createHeaderPanel() {
        JPanel header = new NonOpaquePanel(new BorderLayout(JBUIScale.scale(20), 0));
        //header.setBorder(JBUI.Borders.emptyRight(20));
        myPanel.add(header, BorderLayout.NORTH);

        myIconLabel = new JLabel();
        myIconLabel.setVerticalAlignment(SwingConstants.TOP);
        myIconLabel.setOpaque(false);
        header.add(myIconLabel, BorderLayout.WEST);

        return header;
    }

    @NotNull
    private JPanel createCenterPanel() {
        JPanel centerPanel = new NonOpaquePanel(new VerticalLayout(5));

        myNameComponent = new JLabel();
        myNameComponent.setFont(myNameComponent.getFont().deriveFont(Font.BOLD, 25));

        installBtn = new JLabel("Install", SwingConstants.CENTER);
        installBtn.setForeground(JBUI.CurrentTheme.Link.linkColor());
        installBtn.setPreferredSize(new Dimension(80, 60));
        Border outside = new MatteBorder(1, 1, 1, 1, JBUI.CurrentTheme.Link.linkColor());
        Border inside = new EmptyBorder(15, 0, 15, 10);
        CompoundBorder cb = BorderFactory.createCompoundBorder(inside, outside);
        CompoundBorder cb1 = BorderFactory.createCompoundBorder(cb, new EmptyBorder(0, 10, 0, 10));
        installBtn.setBorder(cb1);
        installBtn.setBackground(MAIN_BG_COLOR);


        myNameAndButtons = new JPanel(new BorderLayout());
        myNameAndButtons.setBackground(MAIN_BG_COLOR);
        myNameAndButtons.add(myNameComponent, BorderLayout.CENTER);
        myNameAndButtons.add(installBtn, BorderLayout.EAST);

        centerPanel.add(myNameAndButtons, VerticalLayout.FILL_HORIZONTAL);

        createTopDescriptionPanel(centerPanel);
        createMetricsPanel(centerPanel);

        return centerPanel;
    }

    private void createTopDescriptionPanel(@NotNull JPanel centerPanel) {
        // text field without horizontal margins
        myTopDescription = createJEditorPane();

        JPanel panel1 = new OpaquePanel(new VerticalLayout(5), MAIN_BG_COLOR);
        panel1.add(myTopDescription);
        centerPanel.add(panel1);
    }

    private void createMetricsPanel(@NotNull JPanel centerPanel) {
        versionsCmb = new ComboBox();

        myRating = new JLabel("", AllIcons.Plugins.Rating, SwingConstants.CENTER);
        myRating.setOpaque(false);
        myRating.setIconTextGap(2);
        myRating.setForeground(GRAY_COLOR);

        JPanel metricsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        metricsPanel.setBackground(MAIN_BG_COLOR);
        metricsPanel.add(myRating);
        metricsPanel.add(versionsCmb);
        centerPanel.add(metricsPanel);

    }

    private JBScrollPane createScrollPane(JPanel panel) {
        JBScrollPane scrollPane = new JBScrollPane(panel);
        scrollPane.getVerticalScrollBar().setValue(0);
        scrollPane.getVerticalScrollBar().setBackground(MAIN_BG_COLOR);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setValue(0);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private JEditorPane createJEditorPane() {
        JEditorPane editorPanel = new JEditorPane();
        HTMLEditorKit kit = UIUtil.getHTMLEditorKit();
        StyleSheet sheet = kit.getStyleSheet();
        sheet.addRule("ul {margin-left: 16px}"); // list-style-type: none;
        sheet.addRule("a {color: " + ColorUtil.toHtmlColor(JBUI.CurrentTheme.Link.linkColor()) + "}");
        editorPanel.setEditable(false);
        editorPanel.setOpaque(false);
        editorPanel.setBorder(null);
        editorPanel.setContentType("text/html");
        editorPanel.setEditorKit(kit);
        editorPanel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
        editorPanel.addHyperlinkListener(BrowserHyperlinkListener.INSTANCE);
        return editorPanel;
    }

    private JPanel createBottomTab(JEditorPane editorComponent) {
        JPanel panel = new OpaquePanel(new VerticalLayout(5));
        panel.setBorder(JBUI.Borders.empty(10, 10, 15, 15));
        panel.add(editorComponent);
        panel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
        return panel;
    }

    private void createBottomPanel() {
        //first tab - includes long description + link
        myDetailsComponent = createJEditorPane();
        JPanel detailsPanel = createBottomTab(myDetailsComponent);
        myHomePage = new LinkPanel(detailsPanel, true, null, null);

        // second tab - includes readme
        myDescriptionComponent = createJEditorPane();
        JPanel descriptionPanel = createBottomTab(myDescriptionComponent);

        // third tab - includes yaml
        myYamlComponent = createJEditorPane();
        JPanel yamlPanel = createBottomTab(myYamlComponent);

        JTabbedPane bottomTabs = new JBTabbedPane();
        bottomTabs.addTab("Details", createScrollPane(detailsPanel));
        bottomTabs.addTab("Description", createScrollPane(descriptionPanel));
        bottomTabs.addTab("Yaml", createScrollPane(yamlPanel));

        myPanel.add(bottomTabs, BorderLayout.CENTER);
    }

    @Override
    protected JComponent create(Integer key) {
        if (key == 0) {
            return myPanel;
        }
        if (key == 1) {
            return myEmptyPanel;
        }
        return super.create(key);
    }

    public void show(HubItem item, BiConsumer<HubItem, String> doInstallAction) {
        if (item == null) {
            select(1, true);
        } else {
            ResourceData resource = item.getResource();
            //edit all panel
            String nameItem = resource.getLatestVersion().getDisplayName().isEmpty() ? resource.getName() : resource.getLatestVersion().getDisplayName();
            myNameComponent.setText(nameItem);

            if (versionsCmb.getItemListeners().length > 0) {
                versionsCmb.removeItemListener(versionsCmb.getItemListeners()[0]);
            }
            versionsCmb.removeAllItems();
            model.getVersionsById(resource.getId()).forEach(version -> {
                versionsCmb.addItem(version);

            });
            versionsCmb.setSelectedIndex(versionsCmb.getItemCount() - 1);


            BasicComboBoxRenderer versionCmbRenderer = new BasicComboBoxRenderer()
            {
                public Component getListCellRendererComponent(
                        JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
                {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                    if (value instanceof ResourceVersionData)
                    {
                        ResourceVersionData version = (ResourceVersionData)value;
                        setText( version.getVersion() );
                    }

                    return this;
                }
            };
            versionsCmb.setRenderer(versionCmbRenderer);
            versionsCmb.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    ResourceVersionData versionSelected = (ResourceVersionData) e.getItem();
                    loadBottomTabs(versionSelected.getRawURL());
                }
            });

            installBtn.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) { }

                @Override
                public void mousePressed(MouseEvent e) {
                    String versionSelected = ((ResourceVersionData)versionsCmb.getSelectedItem()).getVersion();
                    Optional<String> rawURI = resource.getVersions().stream().filter(version -> version.getVersion().equalsIgnoreCase(versionSelected)).map(version -> version.getRawURL().toString()).findFirst();
                    if (rawURI.isPresent()) {
                        doInstallAction.accept(item, rawURI.get());
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) { }

                @Override
                public void mouseEntered(MouseEvent e) { }

                @Override
                public void mouseExited(MouseEvent e) { }
            });

            myRating.setText(resource.getRating().toString());
            myDetailsComponent.setText(resource.getLatestVersion().getDescription().replace("\n", "<br>") + "<br>Tags:<br>" + resource.getTags().stream().map(tag -> tag.getName()).collect(Collectors.joining(", ")));
            myHomePage.show(resource.getKind() + " Homepage", () -> {
                try {
                    Desktop.getDesktop().browse(resource.getLatestVersion().getWebURL());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            String description = resource.getLatestVersion().getDescription();
            description = description.indexOf("\n") > -1 ? description.substring(0, description.indexOf("\n")) : description;
            myTopDescription.setText(description);

            loadBottomTabs(resource.getLatestVersion().getRawURL());

            select(0, true);
        }
    }

    private void loadBottomTabs(URI rawURL) {
        loadYaml(rawURL);
        loadDescription(rawURL);
    }

    private void loadYaml(URI rawURI) {
        //TODO add loading icon
        myYamlComponent.setText("loading");

        ExecHelper.submit(() -> {
            try {
                String yaml = model.getContentByURI(rawURI.toString());
                myYamlComponent.setText("<pre>" + yaml + "</pre>");
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage());
            }
        });

    }

    private void loadDescription(URI rawURI) {
        //TODO add loading icon
        myDescriptionComponent.setText("");
        ExecHelper.submit(() -> {
            try {
                String readmeURI = rawURI.toString().substring(0, rawURI.toString().lastIndexOf("/")) + "/README.md";
                String text = model.getContentByURI(readmeURI);

                final MarkdownFlavourDescriptor flavour = new GFMFlavourDescriptor();
                final ASTNode parsedTree1 = new MarkdownParser(flavour).buildMarkdownTreeFromString(text);
                final String html = new HtmlGenerator(text, parsedTree1, flavour, false).generateHtml();

                myDescriptionComponent.setText(html);

            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage());
            }
        });
    }
}
