package workers;

import ch.emf.dao.JpaDao;
import ch.emf.dao.JpaDaoAPI;
import ch.emf.dao.Transaction;
import ch.emf.dao.filtering.Search;
import java.util.List;
import models.Activite;
import models.Canton;
import models.Conseil;
import models.Conseiller;
import models.EtatCivil;
import models.Groupe;
import models.Parti;

/**
 * Worker avec des méthodes pour gérer les actions "metier" de et vers la base de donnees.
 * Elles correspondent ici (à titre de démonstration) aux méthodes nécessaires
 * pour créer la BD d'après le contenu d'un fichier CSV.
 *
 * @author strittjc
 */
public class DbWorker implements DbWorkerAPI {
  private static final String APPLICATION_PU = "parlementPU";
  private static final String ABREV= "abrev";
  private static DbWorker instance = null;
  private JpaDaoAPI dao;

  // constructeur privée pour singleton
  private DbWorker() {

    // ouvre la base de données et sa couche dao
    dao = new JpaDao();
    dao.open(APPLICATION_PU);
  }

  // récupération d'une instance du singleton
  public synchronized static DbWorker getInstance() {
    if (instance == null) {
      instance = new DbWorker();
    }
    return instance;
  }

  /*
   * RECHERCHES
   */
  @Override
  public EtatCivil rechercherEtatCivil(String abrev) {
    return dao.getSingleResult(EtatCivil.class, ABREV, abrev);
  }

  @Override
  public Canton rechercherCanton(String abrev) {
    return dao.getSingleResult(Canton.class, ABREV, abrev);
  }

  @Override
  public Parti rechercherParti(String abrev) {
    return dao.getSingleResult(Parti.class, ABREV, abrev);
  }

  @Override
  public Conseil rechercherConseil(String abrev) {
    return dao.getSingleResult(Conseil.class, ABREV, abrev);
  }

  @Override
  public Groupe rechercherGroupe(String abrev) {
    return dao.getSingleResult(Groupe.class, ABREV, abrev);
  }

  @Override
  public Conseiller rechercherConseiller(Conseiller c) {
    Search s = new Search(Conseiller.class);
    s.addFilterEqual("nom", c.getNom());
    s.addFilterAnd();
    s.addFilterEqual("prenom", c.getPrenom());
    s.addFilterAnd();
    s.addFilterEqual("sexe", c.getSexe());
    s.addFilterAnd();
    s.addFilterEqual("canton", c.getCanton());
    return dao.getSingleResult(s);
  }


  /*
   * AJOUT DE LISTE DE DONNEES
   */
  @Override
  public int ajouterEtatsCivils(List<EtatCivil> etatsCivils) {
    return dao.insertList(EtatCivil.class, etatsCivils, false);
  }

  @Override
  public int ajouterCantons(List<Canton> cantons) {
    return dao.insertList(Canton.class, cantons, false);
  }

  @Override
  public int ajouterPartis(List<Parti> partis) {
    return dao.insertList(Parti.class, partis, false);
  }

  @Override
  public int ajouterConseils(List<Conseil> conseils) {
    return dao.insertList(Conseil.class,conseils, false);
  }

  @Override
  public int ajouterGroupes(List<Groupe> groupes) {
    return dao.insertList(Groupe.class, groupes, false);
  }

  @Override
  public int ajouterConseillers(List<Conseiller> conseillers) {
    return dao.insertList(Conseiller.class, conseillers, false);
  }

  @Override
  public int ajouterActivite(Activite activite) {
    Search s = new Search(Activite.class);
    s.addFilterEqual("dateEntree", activite.getDateEntree());
    s.addFilterAnd();
    s.addFilterEqual("dateSortie", activite.getDateSortie());
    s.addFilterAnd();
    s.addFilterEqual("conseiller", activite.getConseiller());
    s.addFilterAnd();
    s.addFilterEqual("conseil", activite.getConseil());
    s.addFilterAnd();
    s.addFilterEqual("groupe", activite.getGroupe());
    int n = 0;
    if (dao.getSingleResult(s) == null) {
      n = dao.create(activite);
    }
    return n;
  }

  /*
   * *** FONCTIONDE GENERIQUES SUR LA BD ****
   */
  @Override
  public Transaction demarrerTransaction() {
    Transaction tr = dao.getTransaction();
    tr.beginManualTransaction();
    return tr;
  }

  @Override
  public void terminerTransaction(Transaction tr) {
    try {
      tr.commitManualTransaction();
    } catch (Exception ex) {
    }
    tr.finishManualTransaction();
  }

  @Override
  public int executerScript(String nomScript) {
    return dao.executeScript(nomScript);
  }

  @Override
  public void fermerBD() {
    terminerTransaction(dao.getTransaction());
    dao.close();
  }

  @Override
  public JpaDaoAPI getDao() {
    return dao;
  }

}
