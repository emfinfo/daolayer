package workers;

import models.Activite;
import models.Canton;
import models.Conseil;
import models.Conseiller;
import models.Fonction;
import models.Groupe;
import models.Parti;
import ch.emf.dao.JpaDao;
import ch.emf.dao.JpaDaoAPI;
import ch.emf.dao.Transaction;
import ch.emf.dao.filtering.Search;

/**
 * Worker avec des méthodes pour gérer les actions "metier" de et vers la base de donnees.
 * Elles correspondent ici (à titre de démonstration) aux méthodes nécessaires 
 * pour créer la BD d'après le contenu d'un fichier CSV.
 * 
 * @author strittjc
 */
public class DbWorker implements DbWorkerAPI {
  private static final String APPLICATION_PU = "parlementPU";
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
  public Canton rechercherCanton(String abrev) {
    return dao.getSingleResult(Canton.class, "abrev", abrev);
  }

  @Override
  public Parti rechercherParti(String nomParti) {
    return dao.getSingleResult(Parti.class, "nomParti", nomParti);
  }

  @Override
  public Conseil rechercherConseil(String abrev) {
    return dao.getSingleResult(Conseil.class, "abrev", abrev);
  }

  @Override
  public Groupe rechercherGroupe(String nomGroupe) {
    return dao.getSingleResult(Groupe.class, "nomGroupe", nomGroupe);
  }

  @Override
  public Fonction rechercherFonction(String nomFonction) {
    return dao.getSingleResult(Fonction.class, "nomFonction", nomFonction);
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
   * AJOUT DE DONNEES D'APRES LES DONNEES DANS UN BEAN CONSEILLER
   */
  @Override
  public int ajouterCanton(Conseiller c) {
    int n = 0;
    Canton canton = c.getCanton();
    Canton ctRech = rechercherCanton(canton.getAbrev());
    if (ctRech == null) {
      n = dao.create(canton);
//      System.out.println("Canton: " + canton.getAbrev() + ", pk=" + canton.getPkCanton());
      c.setCanton(canton);
    } else {
      c.setCanton(ctRech);
    }
    return n;
  }

  @Override
  public int ajouterParti(Conseiller c) {
    int n = 0;
    Parti parti = c.getParti();
    Parti ptRech = rechercherParti(parti.getNomParti());
    if (ptRech == null) {
      n = dao.create(parti);
//      System.out.println("Parti: " + parti.getNomParti() + ", pk=" + parti.getPkParti());
      c.setParti(parti);
    } else {
      c.setParti(ptRech);
    }
    return n;
  }  

  @Override
  public int ajouterConseil(Conseiller c) {
    int n = 0;
    Conseil conseil = c.getConseil();
    Conseil coRech = rechercherConseil(conseil.getAbrev());
    if (coRech == null) {
      n = dao.create(conseil);
//      System.out.println("Conseil: " + conseil.getAbrev() + ", pk=" + conseil.getPkConseil());
      c.setConseil(conseil);
    } else {
      c.setConseil(coRech);
    }
    return n;
  }

  @Override
  public int ajouterGroupe(Conseiller c) {
    int n = 0;
    Groupe groupe = c.getGroupe();
    if (groupe != null) {
      Groupe grRech = rechercherGroupe(groupe.getNomGroupe());
      if (grRech == null) {
        n = dao.create(groupe);
//      System.out.println("Groupe: " + groupe.getNomGroupe() + ", pk=" + groupe.getPkGroupe());
        c.setGroupe(groupe);
      } else {
        c.setGroupe(grRech);
      }
    }
    return n;
  } 
  
  @Override
  public int ajouterFonction(Conseiller c) {
    int n = 0;
    Fonction fonction = c.getFonction();
    if (fonction != null) {
      Fonction fctRech = rechercherFonction(fonction.getNomFonction());
      if (fctRech == null) {
        n = dao.create(fonction);
//      System.out.println("Fonction: " + fonction.getNomFonction() + ", pk=" + fonction.getPkFonction());
        c.setFonction(fonction);
      } else {
        c.setFonction(fctRech);
      }
    }
    return n;
  }
  
  @Override
  public int ajouterConseiller(Conseiller c) {
    int n = 0;
    Conseiller consRech = rechercherConseiller(c);
    if (consRech == null) {
      n = dao.create(c);
      System.out.println("Conseiller: " + c); // + ", pk=" + c.getPkConseiller());
    } else {
      c.setPkConseiller(consRech.getPkConseiller());
      if (c.isActif() != consRech.isActif()) {
        consRech.setActif(consRech.isActif() || c.isActif());
        dao.update(consRech);
      }
    }
    return n;
  } 
  
  @Override
  public int ajouterActivite(Conseiller c) {
    int n;
    Activite activite = c.getActivite();
    activite.setConseil(c.getConseil());
    activite.setGroupe(c.getGroupe());
    activite.setFonction(c.getFonction());
    activite.setConseiller(c);
    n = dao.create(activite);
//    System.out.println("Activite: " + activite + " pk=" + activite.getPkActivite());
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
