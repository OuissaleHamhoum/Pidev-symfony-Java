package edu.Loopi.interfaces;

import edu.Loopi.entities.Produit;
import java.util.List;

public interface IProduitService {
    void ajouterProduit(Produit p);
    void modifierProduit(Produit p);
    void supprimerProduit(int id);
    List<Produit> getProduitsParOrganisateur(int idOrganisateur);
}