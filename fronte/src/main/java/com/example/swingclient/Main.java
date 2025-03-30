package com.example.swingclient;

import org.apache.hc.core5.http.ParseException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;


import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.EventObject;
import java.util.List;
import java.io.IOException;

// Classe utilitaire pour un JPanel 
class RoundedPanel extends JPanel {
    private int cornerRadius;

    public RoundedPanel() {
        this(15, Color.WHITE); 
    }

    public RoundedPanel(int radius, Color bgColor) {
        super();
        cornerRadius = radius;
        setBackground(bgColor);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        int width = getWidth();
        int height = getHeight();
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Fond du panneau
        graphics.setColor(getBackground());
        graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
        // Bordure (facultatif – ici d'un gris très clair)
        graphics.setColor(new Color(220, 220, 220));
        graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
    }
}

public class Main extends JFrame {

    // Définition des colonnes : on ajoute une colonne Actions à la fin.
    private final String[] COLUMN_NAMES = {"N° Produit", "Produit", "Prix", "Quantité", "Montant", "Actions"};
    private MyTableModel tableModel;
    private JPanel chartPanelContainer;
    private JPanel pieChartPanelContainer; 
    // Composants pour les cartes supérieures (montant minimal, maximal, total)
    private JLabel lblMinPrice, lblMaxPrice, lblTotalPrice;
    // Composants pour les statistiques (chaque stat dans une « stat card »)
    private JLabel statTotalProducts, statTotalQuantity, statAveragePrice, statMostExpensive;

    // Liste des ventes récupérées via l'API
    private List<Vente> ventes;

    // Couleurs d'accent et de fond inspirées du design Material et Tailwind
    private final Color ACCENT = Color.decode("#3B82F6");
    private final Color BACKGROUND = new Color(243, 244, 246);
    private final Color STAT_BG = new Color(249, 250, 251);  // bg-gray-50

    public Main() {
        super("Gestion des Ventes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        
        // Ajouter cette ligne pour activer le LayeredPane
        getLayeredPane().setVisible(true);

        // Appliquer le Look & Feel Nimbus pour un style moderne
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Création des zones d'interface
        JPanel topCardsPanel = createTopCardsPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel bottomPanel = createBottomPanel();

        // Fond général inspiré de "bg-gray-100"
        topCardsPanel.setBackground(BACKGROUND);
        centerPanel.setBackground(BACKGROUND);
        bottomPanel.setBackground(BACKGROUND);

        // Panel principal avec padding de 2rem
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(32, 32, 32, 32));
        mainPanel.setBackground(BACKGROUND);

        mainPanel.add(topCardsPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Chargement initial des données
        refreshTable();
    }

    // --- Création des cartes supérieures (montant min, max, total) ---
    private JPanel createTopCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Carte "Montant Minimal"
        JPanel cardMin = createCard("Montant Minimal");
        JPanel innerMin = (JPanel) cardMin.getComponent(0);
        JPanel textPanelMin = (JPanel) innerMin.getComponent(1);
        lblMinPrice = (JLabel) textPanelMin.getComponent(1);

        // Carte "Montant Maximum"
        JPanel cardMax = createCard("Montant Maximum");
        JPanel innerMax = (JPanel) cardMax.getComponent(0);
        JPanel textPanelMax = (JPanel) innerMax.getComponent(1);
        lblMaxPrice = (JLabel) textPanelMax.getComponent(1);

        // Carte "Montant Total"
        JPanel cardTotal = createCard("Montant Total");
        JPanel innerTotal = (JPanel) cardTotal.getComponent(0);
        JPanel textPanelTotal = (JPanel) innerTotal.getComponent(1);
        lblTotalPrice = (JLabel) textPanelTotal.getComponent(1);

        panel.add(cardMin);
        panel.add(cardMax);
        panel.add(cardTotal);
        return panel;
    }

    // Création d'une carte supérieure avec fond blanc, coins arrondis et léger contour
    private JPanel createCard(String title) {
        JPanel card = new RoundedPanel(15, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel de gauche avec l'icône
        JPanel leftPanel = new RoundedPanel(15, new Color(37, 99, 235));
        leftPanel.setPreferredSize(new Dimension(48, 48));
        leftPanel.setLayout(new GridBagLayout()); // Utiliser GridBagLayout pour un centrage parfait
        
        // Utiliser des caractères Unicode pour les icônes
        JLabel iconLabel = new JLabel();
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Ajout du BOLD pour une meilleure visibilité
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        switch (title) {
            case "Montant Minimal":
                iconLabel.setText("▼");
                break;
            case "Montant Maximum":
                iconLabel.setText("▲");
                break;
            case "Montant Total":
                iconLabel.setText("Σ");
                break;
        }
        
        // Centrage parfait avec GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        leftPanel.add(iconLabel, gbc);

        // Panel de droite avec le titre et la valeur
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(107, 114, 128));
        
        JLabel valueLabel = new JLabel("0.00 €");
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(new Color(17, 24, 39));
        
        textPanel.add(titleLabel);
        textPanel.add(valueLabel);
        
        contentPanel.add(leftPanel, BorderLayout.WEST);
        contentPanel.add(textPanel, BorderLayout.CENTER);
        
        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    // --- Zone centrale : en-tête avec bouton "Ajouter" et tableau des ventes ---
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // En-tête avec titre et bouton "Ajouter"
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));

        JLabel lblTitle = new JLabel("Gestion des Ventes");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(37, 99, 235)); // Blue-600
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JButton addButton = createButton("Ajouter");
        addButton.setBackground(new Color(37, 99, 235)); // Blue-600
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> showAddDialog(this));
        headerPanel.add(addButton, BorderLayout.EAST);

        // Modèle personnalisé et JTable
        tableModel = new MyTableModel();
        JTable table = new JTable(tableModel);
        table.setRowHeight(48);
        table.setShowGrid(true);
        table.setGridColor(new Color(229, 231, 235));
        table.setSelectionBackground(new Color(243, 244, 246));
        table.setSelectionForeground(new Color(37, 99, 235));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setBackground(Color.WHITE);

        // Personnalisation de l'en-tête du tableau
        table.getTableHeader().setBackground(new Color(249, 250, 251)); // Gray-50
        table.getTableHeader().setForeground(new Color(107, 114, 128)); // Gray-500
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        table.getTableHeader().setReorderingAllowed(false);

        // Configuration des colonnes
        table.getColumn("N° Produit").setPreferredWidth(80);
        table.getColumn("Produit").setPreferredWidth(200);
        table.getColumn("Prix").setPreferredWidth(100);
        table.getColumn("Quantité").setPreferredWidth(100);
        table.getColumn("Montant").setPreferredWidth(120);
        table.getColumn("Actions").setPreferredWidth(120);

        // Colonne "Actions" avec renderer et éditeur personnalisés
        table.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        table.getColumn("Actions").setCellEditor(new ButtonEditor(table));

        // Personnalisation de la scrollbar
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setBackground(Color.WHITE);
        
        // Style de la scrollbar
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setBackground(new Color(243, 244, 246));
        verticalScrollBar.setForeground(new Color(107, 114, 128));
        verticalScrollBar.setBorder(BorderFactory.createEmptyBorder());
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBlockIncrement(64);
        
        // Personnalisation du thumb (la partie mobile de la scrollbar)
        verticalScrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(156, 163, 175); // Gray-400
                this.thumbDarkShadowColor = new Color(107, 114, 128); // Gray-500
                this.thumbLightShadowColor = new Color(156, 163, 175); // Gray-400
                this.thumbHighlightColor = new Color(156, 163, 175); // Gray-400
                this.trackColor = new Color(243, 244, 246); // Gray-100
                this.trackHighlightColor = new Color(229, 231, 235); // Gray-200
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });

        // Panel principal avec fond blanc et coins arrondis
        RoundedPanel mainPanel = new RoundedPanel(15, Color.WHITE);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(mainPanel, BorderLayout.CENTER);
        return panel;
    }

    // --- Zone inférieure : graphique et statistiques en "stat cards" ---
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Panel pour les graphiques
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        chartsPanel.setBackground(Color.WHITE);

        // Graphique en barres à gauche
        chartPanelContainer = new JPanel(new BorderLayout());
        chartPanelContainer.setPreferredSize(new Dimension(500, 300));
        updateChart();

        // Graphique camembert à droite
        pieChartPanelContainer = new JPanel(new BorderLayout());
        pieChartPanelContainer.setPreferredSize(new Dimension(500, 300));
        updatePieChart();

        chartsPanel.add(chartPanelContainer);
        chartsPanel.add(pieChartPanelContainer);

        // Statistiques à droite : création de stat cards similaires aux div HTML
        JPanel statsPanel = new JPanel(new GridLayout(4, 1, 0, 15));
        statsPanel.setPreferredSize(new Dimension(300, 300));
        statsPanel.setBackground(Color.WHITE);

        // Chaque "stat card" aura un fond blanc, padding et coins arrondis
        JPanel statCard1 = createStatCard("Nombre total de produits", "0");
        JPanel statCard2 = createStatCard("Quantité totale en stock", "0");
        JPanel statCard3 = createStatCard("Prix moyen", "0 Ar");
        JPanel statCard4 = createStatCard("Produit le plus cher", "-");

        // On récupère les labels pour mise à jour ultérieure
        JPanel contentPanel1 = (JPanel) statCard1.getComponent(0);
        JPanel contentPanel2 = (JPanel) statCard2.getComponent(0);
        JPanel contentPanel3 = (JPanel) statCard3.getComponent(0);
        JPanel contentPanel4 = (JPanel) statCard4.getComponent(0);

        statTotalProducts = (JLabel) contentPanel1.getComponent(1);
        statTotalQuantity = (JLabel) contentPanel2.getComponent(1);
        statAveragePrice = (JLabel) contentPanel3.getComponent(1);
        statMostExpensive = (JLabel) contentPanel4.getComponent(1);

        statsPanel.add(statCard1);
        statsPanel.add(statCard2);
        statsPanel.add(statCard3);
        statsPanel.add(statCard4);

        panel.add(chartsPanel, BorderLayout.CENTER);
        panel.add(statsPanel, BorderLayout.EAST);
        return panel;
    }

    // Méthode pour créer une "stat card" avec fond blanc et coins arrondis
    private JPanel createStatCard(String title, String value) {
        RoundedPanel statCard = new RoundedPanel(15, Color.WHITE);
        statCard.setLayout(new BorderLayout());
        statCard.setPreferredSize(new Dimension(280, 70));
        statCard.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Panel pour l'icône et le texte
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setOpaque(false);

        // Icône système
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(24, 24));
        iconLabel.setOpaque(false);
        
        // Définir l'icône système en fonction du titre
        switch (title) {
            case "Nombre total de produits":
                iconLabel.setIcon(UIManager.getIcon("FileView.directoryIcon"));
                break;
            case "Quantité totale en stock":
                iconLabel.setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
                break;
            case "Prix moyen":
                iconLabel.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
                break;
            case "Produit le plus cher":
                iconLabel.setIcon(UIManager.getIcon("FileView.computerIcon"));
                break;
        }

        // Texte avec titre et valeur
        JLabel lbl = new JLabel("<html><p style='margin:0; font-size:12px; color:gray;'>"
                + title + "</p><p style='margin:0; font-size:18px; font-weight:bold; color:"
                + toHex(ACCENT) + ";'>" + value + "</p></html>");

        contentPanel.add(iconLabel, BorderLayout.WEST);
        contentPanel.add(lbl, BorderLayout.CENTER);

        // Ajouter l'effet de survol
        statCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                statCard.setBackground(new Color(250, 250, 250));
                statCard.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                statCard.setBackground(Color.WHITE);
                statCard.repaint();
            }
        });

        statCard.add(contentPanel, BorderLayout.CENTER);
        return statCard;
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    // --- Rafraîchissement du tableau, statistiques et graphique ---
    private void refreshTable() {
        try {
            ventes = ApiClient.getVentes();
            tableModel.setVentes(ventes);
            updateStatistics();
            updateChart();
            updatePieChart(); // Mise à jour du graphique camembert
        } catch (IOException | ParseException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la récupération des données : " + ex.getMessage());
        }
    }

    private void updateStatistics() {
        if (ventes == null || ventes.isEmpty()) {
            lblMinPrice.setText("0 Ar");
            lblMaxPrice.setText("0 Ar");
            lblTotalPrice.setText("0 Ar");
            statTotalProducts.setText("<html><p style='margin:0; font-size:12px; color:gray;'>Nombre total de produits</p><p style='margin:0; font-size:18px; font-weight:bold; color:" + toHex(ACCENT) + ";'>0</p></html>");
            statTotalQuantity.setText("<html><p style='margin:0; font-size:12px; color:gray;'>Quantité totale en stock</p><p style='margin:0; font-size:18px; font-weight:bold; color:" + toHex(ACCENT) + ";'>0</p></html>");
            statAveragePrice.setText("<html><p style='margin:0; font-size:12px; color:gray;'>Prix moyen</p><p style='margin:0; font-size:18px; font-weight:bold; color:" + toHex(ACCENT) + ";'>0 Ar</p></html>");
            statMostExpensive.setText("<html><p style='margin:0; font-size:12px; color:gray;'>Produit le plus cher</p><p style='margin:0; font-size:18px; font-weight:bold; color:" + toHex(ACCENT) + ";'>-</p></html>");
            return;
        }
        double min = ventes.stream().mapToDouble(Vente::getMontant).min().orElse(0);
        double max = ventes.stream().mapToDouble(Vente::getMontant).max().orElse(0);
        double total = ventes.stream().mapToDouble(Vente::getMontant).sum();
        int totalProducts = ventes.size();
        int totalQuantity = ventes.stream().mapToInt(Vente::getQuantite).sum();
        double average = total / totalProducts;
        Vente mostExpensive = ventes.stream().max((v1, v2) -> Double.compare(v1.getMontant(), v2.getMontant())).orElse(null);
        DecimalFormat df = new DecimalFormat("#.##");

        lblMinPrice.setText(df.format(min) + " Ar");
        lblMaxPrice.setText(df.format(max) + " Ar");
        lblTotalPrice.setText(df.format(total) + " Ar");

        statTotalProducts.setText("<html><p style='margin:0; font-size:12px; color:gray;'>Nombre total de produits</p><p style='margin:0; font-size:18px; font-weight:bold; color:" + toHex(ACCENT) + ";'>" + totalProducts + "</p></html>");
        statTotalQuantity.setText("<html><p style='margin:0; font-size:12px; color:gray;'>Quantité totale en stock</p><p style='margin:0; font-size:18px; font-weight:bold; color:" + toHex(ACCENT) + ";'>" + totalQuantity + "</p></html>");
        statAveragePrice.setText("<html><p style='margin:0; font-size:12px; color:gray;'>Prix moyen</p><p style='margin:0; font-size:18px; font-weight:bold; color:" + toHex(ACCENT) + ";'>" + df.format(average) + " Ar</p></html>");
        statMostExpensive.setText("<html><p style='margin:0; font-size:12px; color:gray;'>Produit le plus cher</p><p style='margin:0; font-size:18px; font-weight:bold; color:" + toHex(ACCENT) + ";'>" + (mostExpensive != null ? mostExpensive.getDesign() : "-") + "</p></html>");
    }

    private void updateChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (ventes != null) {
            for (Vente v : ventes) {
                dataset.addValue(v.getMontant(), "Montant", v.getDesign());
            }
        }
        JFreeChart chart = ChartFactory.createBarChart("Statistiques des Ventes", "Produit", "Montant (Ar)", dataset);
        
        // Personnalisation du graphique
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(new Color(229, 231, 235)); // Gris clair pour la grille
        
        // Couleur bleue pour les barres
        plot.getRenderer().setSeriesPaint(0, new Color(37, 99, 235)); // Blue-600
        
        // Personnalisation du titre
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 20));
        chart.getTitle().setPaint(new Color(37, 99, 235)); // Blue-600
        
        // Personnalisation des axes
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        // Couleurs des labels des axes
        plot.getDomainAxis().setLabelPaint(new Color(107, 114, 128)); // Gray-500
        plot.getRangeAxis().setLabelPaint(new Color(107, 114, 128)); // Gray-500
        
        chartPanelContainer.removeAll();
        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(500, 300));
        chartPanelContainer.add(cp, BorderLayout.CENTER);
        chartPanelContainer.validate();
    }

    private void updatePieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        if (ventes != null) {
            for (Vente v : ventes) {
                dataset.setValue(v.getDesign(), v.getMontant());
            }
        }

        // Création du graphique camembert 3D
        JFreeChart pieChart = ChartFactory.createPieChart3D(
            "Répartition des Ventes",
            dataset,
            true, // Légende
            true, // Tooltips
            false // URLs
        );

        // Personnalisation du graphique
        pieChart.setBackgroundPaint(Color.WHITE);
        pieChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 20));
        pieChart.getTitle().setPaint(new Color(37, 99, 235)); // Blue-600

        // Personnalisation de la légende
        pieChart.getLegend().setBackgroundPaint(Color.WHITE);
        pieChart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Personnalisation du plot
        PiePlot3D plot = (PiePlot3D) pieChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(new Color(229, 231, 235));
        plot.setLabelShadowPaint(new Color(229, 231, 235));

        // Couleurs personnalisées pour les sections
        plot.setSectionPaint(0, new Color(37, 99, 235));  // Blue-600
        plot.setSectionPaint(1, new Color(239, 68, 68));  // Red-500
        plot.setSectionPaint(2, new Color(16, 185, 129)); // Green-500
        plot.setSectionPaint(3, new Color(245, 158, 11)); // Yellow-500
        plot.setSectionPaint(4, new Color(139, 92, 246)); // Purple-500

        // Effet 3D
        plot.setForegroundAlpha(0.8f);
        plot.setDarkerSides(true);

        // Mise à jour du panel
        pieChartPanelContainer.removeAll();
        ChartPanel cp = new ChartPanel(pieChart);
        cp.setPreferredSize(new Dimension(500, 300));
        pieChartPanelContainer.add(cp, BorderLayout.CENTER);
        pieChartPanelContainer.validate();
    }

    // --- Boutons personnalisés inspirés du design HTML (fond ACCENT et texte blanc) ---
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    // --- Boîtes de dialogue pour Ajouter / Modifier une vente ---
    private void showAddDialog(JFrame parent) {
        JDialog dialog = createStyledDialog(parent, "Ajouter une vente", 400, 400);
        RoundedPanel mainPanel = (RoundedPanel) dialog.getContentPane().getComponent(0);

        // Panel pour les champs avec padding
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;

        // Création des champs avec icônes
        JTextField numProduitField = createStyledTextField("N° Produit");
        JTextField designField = createStyledTextField("Produit");
        JTextField prixField = createStyledTextField("Prix");
        JTextField quantiteField = createStyledTextField("Quantité");

        // Style des labels
        JLabel[] labels = {
            createStyledLabel("N° Produit:"),
            createStyledLabel("Produit:"),
            createStyledLabel("Prix:"),
            createStyledLabel("Quantité:")
        };

        // Ajout des champs avec labels
        gbc.gridx = 0;
        gbc.gridy = 0;
        fieldsPanel.add(labels[0], gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldsPanel.add(numProduitField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        fieldsPanel.add(labels[1], gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldsPanel.add(designField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        fieldsPanel.add(labels[2], gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldsPanel.add(prixField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        fieldsPanel.add(labels[3], gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldsPanel.add(quantiteField, gbc);

        // Panel pour les boutons avec plus de padding
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton cancelButton = createButton("Annuler");
        cancelButton.setBackground(new Color(239, 68, 68)); 
        cancelButton.setForeground(Color.WHITE);
        JButton confirmButton = createButton("Confirmer");
        confirmButton.setBackground(new Color(37, 99, 235));
        confirmButton.setForeground(Color.WHITE);

        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> dialog.dispose());

        confirmButton.addActionListener(e -> {
            try {
                Vente vente = new Vente();
                vente.setNumProduit(numProduitField.getText());
                vente.setDesign(designField.getText());
                vente.setPrix(Double.parseDouble(prixField.getText()));
                vente.setQuantite(Integer.parseInt(quantiteField.getText()));

                ApiClient.addVente(vente);
                refreshTable();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private void showEditDialog(JFrame parent, Vente vente) {
        if (vente == null) {
            JOptionPane.showMessageDialog(parent, "Veuillez sélectionner une ligne à modifier.");
            return;
        }

        JDialog dialog = createStyledDialog(parent, "Modifier une vente", 400, 400);
        RoundedPanel mainPanel = (RoundedPanel) dialog.getContentPane().getComponent(0);

        // Panel pour les champs avec padding
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;

        // Création des champs avec icônes
        JTextField numProduitField = createStyledTextField("N° Produit");
        JTextField designField = createStyledTextField("Produit");
        JTextField prixField = createStyledTextField("Prix");
        JTextField quantiteField = createStyledTextField("Quantité");

        // Pré-remplir les champs
        numProduitField.setText(vente.getNumProduit());
        designField.setText(vente.getDesign());
        prixField.setText(String.valueOf(vente.getPrix()));
        quantiteField.setText(String.valueOf(vente.getQuantite()));

        // Style des labels
        JLabel[] labels = {
            createStyledLabel("N° Produit:"),
            createStyledLabel("Produit:"),
            createStyledLabel("Prix:"),
            createStyledLabel("Quantité:")
        };

        // Ajout des champs avec labels
        gbc.gridx = 0;
        gbc.gridy = 0;
        fieldsPanel.add(labels[0], gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldsPanel.add(numProduitField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        fieldsPanel.add(labels[1], gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldsPanel.add(designField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        fieldsPanel.add(labels[2], gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldsPanel.add(prixField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        fieldsPanel.add(labels[3], gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fieldsPanel.add(quantiteField, gbc);

        // Panel pour les boutons avec plus de padding
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton cancelButton = createButton("Annuler");
        cancelButton.setBackground(new Color(239, 68, 68)); 
        cancelButton.setForeground(Color.WHITE);
        JButton confirmButton = createButton("Confirmer");
        confirmButton.setBackground(new Color(37, 99, 235));
        confirmButton.setForeground(Color.WHITE);

        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);

        mainPanel.add(fieldsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> dialog.dispose());

        confirmButton.addActionListener(e -> {
            try {
                vente.setNumProduit(numProduitField.getText());
                vente.setDesign(designField.getText());
                vente.setPrix(Double.parseDouble(prixField.getText()));
                vente.setQuantite(Integer.parseInt(quantiteField.getText()));

                ApiClient.updateVente(vente);
                refreshTable();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur : " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    // --- Modèle de tableau personnalisé ---
    class MyTableModel extends AbstractTableModel {
        private List<Vente> ventes;

        public void setVentes(List<Vente> ventes) {
            this.ventes = ventes;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return ventes == null ? 0 : ventes.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Vente v = ventes.get(rowIndex);
            switch (columnIndex) {
                case 0: return v.getNumProduit();
                case 1: return v.getDesign();
                case 2: return v.getPrix() + " Ar";
                case 3: return v.getQuantite();
                case 4: return v.getMontant() + " Ar";
                case 5: return "Actions";
                default: return "";
            }
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 5;
        }

        // Méthode pour supprimer une ligne
        public void removeRow(int row) {
            if (ventes != null && row >= 0 && row < ventes.size()) {
                ventes.remove(row);
                fireTableRowsDeleted(row, row);
            }
        }
    }

    // --- Renderer pour la colonne "Actions" avec boutons ---
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnEdit = new JButton();
        private final JButton btnDelete = new JButton();

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            
            // Charger les icônes
            ImageIcon editIcon = new ImageIcon(getClass().getResource("/edit.png"));
            ImageIcon deleteIcon = new ImageIcon(getClass().getResource("/delete.png"));
            
            // Redimensionner les icônes
            Image editImage = editIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            Image deleteImage = deleteIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            
            btnEdit.setIcon(new ImageIcon(editImage));
            btnDelete.setIcon(new ImageIcon(deleteImage));

            // Style commun pour les boutons
            for (JButton btn : new JButton[]{btnEdit, btnDelete}) {
                btn.setPreferredSize(new Dimension(32, 32));
                btn.setBorderPainted(false);
                btn.setContentAreaFilled(false);
                btn.setFocusPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            add(btnEdit);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // --- Éditeur pour la colonne "Actions" ---
    class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        private final JButton btnEdit = new JButton();
        private final JButton btnDelete = new JButton();
        private JTable table;

        public ButtonEditor(JTable table) {
            this.table = table;
            
            // Charger les icônes
            ImageIcon editIcon = new ImageIcon(getClass().getResource("/edit.png"));
            ImageIcon deleteIcon = new ImageIcon(getClass().getResource("/delete.png"));
            
            // Redimensionner les icônes
            Image editImage = editIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            Image deleteImage = deleteIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            
            btnEdit.setIcon(new ImageIcon(editImage));
            btnDelete.setIcon(new ImageIcon(deleteImage));

            // Style commun pour les boutons
            for (JButton btn : new JButton[]{btnEdit, btnDelete}) {
                btn.setPreferredSize(new Dimension(32, 32));
                btn.setBorderPainted(false);
                btn.setContentAreaFilled(false);
                btn.setFocusPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            btnEdit.addActionListener(this);
            btnDelete.addActionListener(this);

            panel.add(btnEdit);
            panel.add(btnDelete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped();
            int row = table.getSelectedRow();
            if (row < 0) return;
            Vente v = ventes.get(row);
            if (e.getSource() == btnEdit) {
                showEditDialog(Main.this, v);
            } else if (e.getSource() == btnDelete) {
                int confirm = JOptionPane.showConfirmDialog(Main.this,
                        "Voulez-vous vraiment supprimer cette vente ?", "Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        ApiClient.deleteVente(v.getNumProduit());
                        refreshTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(Main.this, "Erreur : " + ex.getMessage());
                    }
                }
            }
        }
    }

    // Méthode pour créer une modale avec animation
    private JDialog createStyledDialog(JFrame parent, String title, int width, int height) {
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(parent);
        dialog.setUndecorated(true);

        // Créer un panel de flou pour l'interface principale
        JPanel blurPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 100)); 
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        blurPanel.setOpaque(false);
        blurPanel.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        parent.getLayeredPane().add(blurPanel, Integer.valueOf(1));

        // Panel principal avec fond blanc et coins arrondis
        RoundedPanel mainPanel = new RoundedPanel(15, Color.WHITE);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // En-tête avec titre et bouton de fermeture
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(37, 99, 235));

        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        closeButton.setForeground(new Color(107, 114, 128));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Méthode pour fermer proprement la modale
        Runnable closeDialog = () -> {
            // Animation de fermeture
            Timer timer = new Timer(20, new ActionListener() {
                float opacity = 1.0f;
                float scale = 1.0f;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (opacity > 0.0f) {
                        opacity -= 0.2f;
                        scale -= 0.01f;
                        dialog.setOpacity(opacity);
                        dialog.setSize((int)(width * scale), (int)(height * scale));
                        dialog.setLocationRelativeTo(parent);
                    } else {
                        // Supprimer le blurPanel et mettre à jour l'interface
                        parent.getLayeredPane().remove(blurPanel);
                        parent.getLayeredPane().validate();
                        parent.getLayeredPane().repaint();
                        dialog.dispose();
                        ((Timer) e.getSource()).stop();
                    }
                }
            });
            timer.start();
        };

        // Ajouter un WindowListener pour gérer la fermeture de la modale
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDialog.run();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                // S'assurer que le blurPanel est supprimé même si la modale est fermée différemment
                parent.getLayeredPane().remove(blurPanel);
                parent.getLayeredPane().validate();
                parent.getLayeredPane().repaint();
            }
        });

        // Gestion de la fermeture avec la touche Escape
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        dialog.getRootPane().registerKeyboardAction(e -> closeDialog.run(), "ESCAPE", escapeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(closeButton, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        dialog.add(mainPanel);

        // Animation d'ouverture
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                dialog.setOpacity(0.0f);
                blurPanel.setVisible(true);
                Timer timer = new Timer(20, new ActionListener() {
                    float opacity = 0.0f;
                    float scale = 0.95f;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (opacity < 1.0f) {
                            opacity += 0.2f;
                            scale += 0.01f;
                            dialog.setOpacity(opacity);
                            dialog.setSize((int)(width * scale), (int)(height * scale));
                            dialog.setLocationRelativeTo(parent);
                        } else {
                            ((Timer) e.getSource()).stop();
                        }
                    }
                });
                timer.start();
            }
        });

        return dialog;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(107, 114, 128));
        label.setPreferredSize(new Dimension(100, 45));
        return label;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(200, 45));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Style du champ de texte avec bordure et coins arrondis
        field.setBorder(new RoundedBorder(8, new Color(229, 231, 235), 10, 15)); 
        field.setBackground(Color.WHITE);
        field.setCaretColor(new Color(37, 99, 235));
        
        // Ajout du placeholder
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
                // Ajouter une bordure bleue lors du focus
                field.setBorder(new RoundedBorder(8, new Color(37, 99, 235), 10, 15));
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
                // Retourner à la bordure grise
                field.setBorder(new RoundedBorder(8, new Color(229, 231, 235), 10, 15));
            }
        });
        
        return field;
    }

    // Classe pour créer une bordure arrondie avec padding
    private class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color color;
        private int top, left, bottom, right;

        public RoundedBorder(int radius, Color color, int top, int left) {
            this.radius = radius;
            this.color = color;
            this.top = top;
            this.left = left;
            this.bottom = top;
            this.right = left;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(top, left, bottom, right);
        }
    }

    // --- Méthode principale ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main dashboard = new Main();
            dashboard.setVisible(true);
        });
    }
}
