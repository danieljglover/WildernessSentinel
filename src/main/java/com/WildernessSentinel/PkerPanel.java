package com.WildernessSentinel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;

public class PkerPanel extends PluginPanel {
    private final IconTextField searchBar;
    private final JPanel listContainer;
    private final JLabel headerLabel;
    private final JLabel footerLabel;
    private final JPanel hotspotContainer;

    private Map<String, PkerInfo> currentPkers;
    private final WildernessSentinelPlugin plugin;
    private final WildernessSentinelConfig config;
    private final ScheduledExecutorService executorService;

    @Inject
    public PkerPanel(WildernessSentinelPlugin plugin, WildernessSentinelConfig config, ScheduledExecutorService executorService) {
        super(false);
        this.plugin = plugin;
        this.config = config;
        this.executorService = executorService;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Header
        headerLabel = new JLabel("Known PKers (0)");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));

        // Search bar
        searchBar = new IconTextField();
        searchBar.setIcon(IconTextField.Icon.SEARCH);
        searchBar.setPreferredSize(new Dimension(0, 30));
        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        searchBar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onSearch();
                } else {
                    filterList(searchBar.getText());
                }
            }
        });
        searchBar.addClearListener(this::onClearSearch);

        // Top panel (header + search)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        topPanel.add(headerLabel);
        topPanel.add(searchBar);
        add(topPanel, BorderLayout.NORTH);

        // List container in scroll pane
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Footer
        footerLabel = new JLabel("Community Mode");
        footerLabel.setForeground(Color.GRAY);
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));

        // Hotspot section
        hotspotContainer = new JPanel();
        hotspotContainer.setLayout(new BoxLayout(hotspotContainer, BoxLayout.Y_AXIS));
        hotspotContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        bottomPanel.add(hotspotContainer);
        bottomPanel.add(footerLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void updatePkerList(Map<String, PkerInfo> pkers) {
        this.currentPkers = pkers;
        headerLabel.setText("Known PKers (" + pkers.size() + ")");
        rebuildList(pkers);
    }

    private void rebuildList(Map<String, PkerInfo> pkers) {
        listContainer.removeAll();

        List<Map.Entry<String, PkerInfo>> sorted = new ArrayList<>(pkers.entrySet());
        sorted.sort(Comparator.<Map.Entry<String, PkerInfo>>comparingInt(e -> e.getValue().getReportCount()).reversed());

        boolean alternate = false;
        for (Map.Entry<String, PkerInfo> entry : sorted) {
            Color nameColor = PkerTierColors.getTextColor(entry.getValue().getReportCount(), config);
            listContainer.add(new PkerRow(entry.getKey(), entry.getValue(), alternate, nameColor));
            alternate = !alternate;
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private void filterList(String text) {
        if (currentPkers == null) return;
        if (text == null || text.isEmpty()) {
            rebuildList(currentPkers);
            headerLabel.setText("Known PKers (" + currentPkers.size() + ")");
            return;
        }
        String lower = text.toLowerCase();
        Map<String, PkerInfo> filtered = new java.util.HashMap<>();
        currentPkers.forEach((name, info) -> {
            if (name.contains(lower)) {
                filtered.put(name, info);
            }
        });
        rebuildList(filtered);
        headerLabel.setText("Known PKers (" + filtered.size() + "/" + currentPkers.size() + ")");
    }

    private void onSearch() {
        String text = searchBar.getText();
        if (text == null || text.isEmpty()) return;
        executorService.submit(() -> {
            PkerApiClient apiClient = plugin.getPkerApiClient();
            if (apiClient != null) {
                Map<String, PkerInfo> results = apiClient.searchPkers(text);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    rebuildList(results);
                    headerLabel.setText("Search: " + results.size() + " results");
                });
            }
        });
    }

    private void onClearSearch() {
        if (currentPkers != null) {
            rebuildList(currentPkers);
            headerLabel.setText("Known PKers (" + currentPkers.size() + ")");
        }
    }

    public void updateHotspots(java.util.List<HotspotEntry> hotspots) {
        hotspotContainer.removeAll();

        if (hotspots.isEmpty()) {
            hotspotContainer.revalidate();
            hotspotContainer.repaint();
            return;
        }

        // Separator
        javax.swing.JSeparator sep = new javax.swing.JSeparator();
        sep.setForeground(Color.GRAY);
        hotspotContainer.add(sep);

        // Header
        JLabel header = new JLabel("Wilderness Hotspots");
        header.setForeground(Color.WHITE);
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        hotspotContainer.add(header);

        for (HotspotEntry entry : hotspots) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(ColorScheme.DARK_GRAY_COLOR);
            row.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

            JLabel zoneLabel = new JLabel("Level " + entry.getZone());
            zoneLabel.setForeground(Color.LIGHT_GRAY);
            row.add(zoneLabel, BorderLayout.WEST);

            JLabel countLabel = new JLabel(entry.getPkerCount() + " PKers");
            Color countColor;
            if (entry.getPkerCount() >= 10) countColor = new Color(255, 50, 50);
            else if (entry.getPkerCount() >= 6) countColor = new Color(255, 165, 0);
            else if (entry.getPkerCount() >= 3) countColor = new Color(255, 255, 50);
            else countColor = new Color(100, 255, 100);
            countLabel.setForeground(countColor);
            row.add(countLabel, BorderLayout.EAST);

            hotspotContainer.add(row);
        }

        hotspotContainer.revalidate();
        hotspotContainer.repaint();
    }
}
