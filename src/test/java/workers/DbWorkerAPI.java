package workers;

import ch.emf.dao.JpaDaoAPI;
import ch.emf.dao.Transaction;
import java.util.List;
import models.Activite;
import models.Canton;
import models.Conseil;
import models.Conseiller;
import models.EtatCivil;
import models.Groupe;
import models.Parti;

/**
 * Interface définissant toutes les opérations métier avec la BD.
 * Elles correspondent ici et à titre de démonstration aux méthodes nécessaires
 * pour créer la BD d'après le contenu d'un fichier CSV.
 *
 * @author jcstritt
 */
public interface DbWorkerAPI {

  // méthodes de recherche
  EtatCivil rechercherEtatCivil(String abrev);
  Canton rechercherCanton(String abrev);
  Parti rechercherParti(String abrev);
  Conseil rechercherConseil(String abrev);
  Groupe rechercherGroupe(String nomGroupe);
  Conseiller rechercherConseiller(Conseiller c);

  // ajout de données de listes
  int ajouterEtatsCivils(List<EtatCivil> etatsCivils);
  int ajouterCantons(List<Canton> cantons);
  int ajouterPartis(List<Parti> partis);
  int ajouterConseils(List<Conseil> conseils);
  int ajouterGroupes(List<Groupe> groupes);
  int ajouterConseillers(List<Conseiller> conseillers);

  // ajouts individuels
  int ajouterActivite(Activite activite);

  // fonctions génériques avec la base de données
  Transaction demarrerTransaction();
  void terminerTransaction(Transaction tr);
  int executerScript(String nomScript);
  void fermerBD();

  // pour récupérer une référence sur la sous-couche "dao" pour les tests unitaires
  JpaDaoAPI getDao();

}
