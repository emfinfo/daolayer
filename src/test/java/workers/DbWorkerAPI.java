package workers;

import models.Canton;
import models.Conseil;
import models.Conseiller;
import models.Fonction;
import models.Groupe;
import models.Parti;
import ch.emf.dao.JpaDaoAPI;
import ch.emf.dao.Transaction;

/**
 * Interface définissant toutes les opérations métier avec la BD.
 * Elles correspondent ici et à titre de démonstration aux méthodes nécessaires 
 * pour créer la BD d'après le contenu d'un fichier CSV.
 *
 * @author jcstritt
 */
public interface DbWorkerAPI {

  // méthodes de recherche
  Canton rechercherCanton(String abrev);
  Parti rechercherParti(String nomParti);
  Conseil rechercherConseil(String abrev);
  Groupe rechercherGroupe(String nomGroupe);
  Fonction rechercherFonction(String nomFonction);
  Conseiller rechercherConseiller(Conseiller c);
  
  // méthodes d'ajout de données d'après le contenu d'un objet "Conseiller"
  int ajouterCanton(Conseiller c);
  int ajouterParti(Conseiller c);
  int ajouterConseil(Conseiller c);
  int ajouterGroupe(Conseiller c);
  int ajouterFonction(Conseiller c);
  int ajouterConseiller(Conseiller c);
  int ajouterActivite(Conseiller c);
  
  // fonctions génériques avec la base de données
  Transaction demarrerTransaction();
  void terminerTransaction(Transaction tr);
  int executerScript(String nomScript);
  void fermerBD();  

  // pour récupérer une référence sur la sous-couche "dao" pour les tests unitaires
  JpaDaoAPI getDao();
  
}
