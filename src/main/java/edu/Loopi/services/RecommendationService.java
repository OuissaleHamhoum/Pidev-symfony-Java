// RecommendationService.java
package edu.Loopi.services;

import edu.Loopi.entities.Produit;
import edu.Loopi.tools.MyConnection;
import edu.Loopi.entities.Feedback;
import edu.Loopi.entities.Favoris;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService {
    private Connection cnx = MyConnection.getInstance().getConnection();
    private ProduitService produitService = new ProduitService();
    private FeedbackService feedbackService = new FeedbackService();
    private FavorisService favorisService = new FavorisService();

    // ==================== TYPE 1: POUR VOUS (Content-Based) ====================
    public List<Produit> getRecommandationsPourVous(int userId, int limit) {
        Map<Integer, Double> scores = new HashMap<>();

        List<Produit> favoris = favorisService.getFavorisByUser(userId);

        if (favoris.isEmpty()) {
            return getRecommandationsPopulaires(limit);
        }

        Map<Integer, Double> categoryPreferences = new HashMap<>();
        Map<String, Double> keywordPreferences = new HashMap<>();

        for (Produit p : favoris) {
            categoryPreferences.merge(p.getIdCategorie(), 1.0, Double::sum);
            Set<String> keywords = extractKeywords(p);
            for (String kw : keywords) {
                keywordPreferences.merge(kw, 1.0, Double::sum);
            }
        }

        normalizeMap(categoryPreferences);
        normalizeMap(keywordPreferences);

        List<Produit> allProducts = produitService.getAll();
        Set<Integer> favorisIds = favoris.stream().map(Produit::getId).collect(Collectors.toSet());

        for (Produit p : allProducts) {
            if (favorisIds.contains(p.getId())) continue;

            double score = 0;
            score += categoryPreferences.getOrDefault(p.getIdCategorie(), 0.0) * 0.6;

            Set<String> productKeywords = extractKeywords(p);
            double keywordScore = 0;
            int matchCount = 0;

            for (String kw : productKeywords) {
                Double prefScore = keywordPreferences.get(kw);
                if (prefScore != null) {
                    keywordScore += prefScore;
                    matchCount++;
                }
            }

            if (matchCount > 0) {
                keywordScore = keywordScore / matchCount;
                score += keywordScore * 0.4;
            }

            if (score > 0) {
                scores.put(p.getId(), score);
            }
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> produitService.getProduitById(entry.getKey()))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    // ==================== TYPE 2: TENDANCE (Produits publiés ET notés cette semaine) ====================
    public List<Produit> getRecommandationsTendance(int limit) {
        Map<Integer, Double> scores = new LinkedHashMap<>();

        String query = """
            SELECT 
                p.id_produit,
                AVG(fb.note) as moyenne_semaine,
                COUNT(fb.id_feedback) as nb_avis_semaine,
                p.created_at
            FROM produit p
            JOIN feedback fb ON p.id_produit = fb.id_produit
            WHERE p.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
                AND fb.date_commentaire >= DATE_SUB(NOW(), INTERVAL 7 DAY)
            GROUP BY p.id_produit, p.created_at
            HAVING nb_avis_semaine >= 1
            ORDER BY moyenne_semaine DESC, nb_avis_semaine DESC, p.created_at DESC
            LIMIT ?
            """;

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, limit);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int productId = rs.getInt("id_produit");
                double moyenne = rs.getDouble("moyenne_semaine");
                int nbAvis = rs.getInt("nb_avis_semaine");

                double score = moyenne * Math.log(nbAvis + 1);

                Produit p = produitService.getProduitById(productId);
                if (p != null) {
                    scores.put(productId, score);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getRecommandationsTendance: " + e.getMessage());
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> produitService.getProduitById(entry.getKey()))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    // ==================== TYPE 3: LES MIEUX NOTÉS ====================
    public List<Produit> getRecommandationsMieuxNotes(int limit) {
        Map<Integer, Double> scores = new LinkedHashMap<>();

        String query = """
            SELECT 
                p.id_produit,
                AVG(fb.note) as avg_note,
                COUNT(fb.id_feedback) as nb_avis
            FROM produit p
            JOIN feedback fb ON p.id_produit = fb.id_produit
            GROUP BY p.id_produit
            HAVING nb_avis >= 1
            ORDER BY avg_note DESC, nb_avis DESC
            LIMIT ?
            """;

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, limit);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int productId = rs.getInt("id_produit");
                double avgNote = rs.getDouble("avg_note");
                int nbAvis = rs.getInt("nb_avis");

                double score = avgNote * Math.log(nbAvis + 1);

                Produit p = produitService.getProduitById(productId);
                if (p != null) {
                    scores.put(productId, score);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getRecommandationsMieuxNotes: " + e.getMessage());
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> produitService.getProduitById(entry.getKey()))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    // ==================== TYPE 4: NOUVEAUTÉS ====================
    // ==================== TYPE 4: NOUVEAUTÉS (Tous les produits de cette semaine) ====================
    public List<Produit> getRecommandationsNouveautes(int limit) {
        List<Produit> nouveautes = new ArrayList<>();

        // Produits de cette semaine (7 derniers jours)
        String query = """
        SELECT * FROM produit 
        WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
        ORDER BY created_at DESC
        LIMIT ?
        """;

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, limit);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Produit p = mapResultSetToProduit(rs);
                if (p != null) {
                    nouveautes.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getRecommandationsNouveautes: " + e.getMessage());
        }

        return nouveautes;
    }

    // ==================== UTILITAIRES ====================

    private Set<String> extractKeywords(Produit p) {
        Set<String> keywords = new HashSet<>();
        if (p == null) return keywords;

        String text = (p.getNom() + " " + p.getDescription()).toLowerCase();
        String[] words = text.replaceAll("[^a-zA-Z0-9\\s]", " ").split("\\s+");

        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "le", "la", "les", "un", "une", "des", "et", "ou", "de", "du",
                "avec", "pour", "dans", "sur", "est", "sont", "ce", "cet", "cette",
                "je", "tu", "il", "elle", "nous", "vous", "ils", "elles",
                "mon", "ton", "son", "ma", "ta", "sa", "mes", "tes", "ses",
                "notre", "votre", "leur", "nos", "vos", "leurs"
        ));

        for (String word : words) {
            word = word.trim();
            if (word.length() > 2 && !stopWords.contains(word)) {
                keywords.add(word);
            }
        }

        return keywords;
    }

    private void normalizeMap(Map<?, Double> map) {
        if (map.isEmpty()) return;

        double max = map.values().stream().max(Double::compare).orElse(1.0);
        if (max > 0) {
            double finalMax = max;
            map.replaceAll((k, v) -> v / finalMax);
        }
    }

    private Produit mapResultSetToProduit(ResultSet rs) throws SQLException {
        return new Produit(
                rs.getInt("id_produit"),
                rs.getString("nom_produit"),
                rs.getString("description"),
                rs.getString("image_produit"),
                rs.getInt("id_cat"),
                rs.getInt("id_user")
        );
    }

    public List<Produit> getRecommandationsPopulaires(int limit) {
        Map<Integer, Double> scores = new LinkedHashMap<>();

        String query = """
            SELECT p.id_produit,
                   COUNT(DISTINCT f.id_user) as favoris_count,
                   AVG(fb.note) as avg_note
            FROM produit p
            LEFT JOIN favoris f ON p.id_produit = f.id_produit
            LEFT JOIN feedback fb ON p.id_produit = fb.id_produit
            GROUP BY p.id_produit
            ORDER BY favoris_count DESC, avg_note DESC
            LIMIT ?
            """;

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, limit);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int productId = rs.getInt("id_produit");
                Produit p = produitService.getProduitById(productId);
                if (p != null) {
                    scores.put(productId, 1.0);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getRecommandationsPopulaires: " + e.getMessage());
        }

        return scores.keySet().stream()
                .map(id -> produitService.getProduitById(id))
                .filter(Objects::nonNull)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }
}