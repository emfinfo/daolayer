package tests;

import ch.emf.dao.JpaDaoAPI;
import ch.emf.dao.exceptions.JpaException;
import ch.emf.dao.filtering.Search;
import ch.emf.dao.filtering.Search2;
import ch.emf.dao.models.EntityInfo;
import ch.emf.dao.transactions.Transaction;
import ch.jcsinfo.datetime.DateTimeLib;
import ch.jcsinfo.file.FileHelper;
import ch.jcsinfo.system.StackTracer;
import ch.jcsinfo.util.ConvertLib;
import com.google.inject.Guice;
import com.google.inject.Injector;
import helpers.DbRebuilder;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import models.Activite;
import models.Canton;
import models.Conseil;
import models.Conseiller;
import models.EtatCivil;
import models.Parti;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Classe de test de la clasee JpaDao.
 *
 * @author jcstritt
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JpaDaoTest {
  private static final boolean IMPORT_DB = false; // importer la DB depuis le fichier csv (30-60s)
  private static final boolean SHOW_LIST = true; // voir un extrait des listes extraites
  private static final int LIST_MAXSIZE = 4; // le maximum d'entrées affichées pour les longues listes
  private static final String CHEMIN_DONNEES = "data";
  private static final String FICHIER_CONSEILLERS = "Ratsmitglieder_1848_FR_2020_12_09.csv";
  private static final String SCRIPT_DELETE_ALL = "db-delete-all.sql";
  private static final String SCRIPT_IMPORT_LOGINS = "db-import-logins.sql";

  private static JpaDaoAPI dao;
  private static int lastPk = -1;

  private static EtatCivil EC = null;
  private static Parti PS = null;
  private static Canton FR = null;
  private static Conseil CF = null;

  private static List<Activite> activites;


  /*
   * METHODES APPELEES AVANT ET APRES LES TESTS
   */
  @BeforeClass
  public static void setUpClass() {

    // teste d'un injection avec Guice
    Injector inj = Guice.createInjector(new GuiceModule());
    dao = inj.getInstance(JpaDaoAPI.class);
    try {
      System.out.println(dao.getVersion());
      dao.connect("parlementPU");

      // si ok et s'il faut importer de nouvelles données
      if (IMPORT_DB) {
        int n1 = dao.executeScript(FileHelper.normalizeFileName(CHEMIN_DONNEES + "/" + SCRIPT_DELETE_ALL));
        int n2 = dao.executeScript(FileHelper.normalizeFileName(CHEMIN_DONNEES + "/" + SCRIPT_IMPORT_LOGINS));
        DbRebuilder fileWrk = new DbRebuilder(dao);
        fileWrk.importerDonneesFichier(FileHelper.normalizeFileName(CHEMIN_DONNEES + "/" + FICHIER_CONSEILLERS));
      }

    } catch (JpaException ex) {
      System.out.println(ex.getMessage());
    }
  }


  @AfterClass
  public static void tearDownClass() {
    dao.disconnect();
  }


  /*
   * METHODES PRIVEES
   */
  private Conseiller getNewConseiller() {
    Conseiller c = new Conseiller();
    c.setActif(true);
    c.setDateNaissance(DateTimeLib.parseDate("1.1.1970"));
    c.setDateDeces(null);
    c.setPrenom("Jules");
    c.setNom("Tartampion");
    c.setSexe("m");
    c.setCitoyennete("Fribourg");
    c.setEtatCivil(EC);
    c.setParti(PS);
    c.setCanton(FR);
    return c;
  }


  /*
   * TESTS
   */
  @Test
  public void test01_isConnected() {
    StackTracer.printCurrentTestMethod();
    boolean ok = dao.isConnected();

    // on affiche le résultat
    StackTracer.printTestResult("Connected: ", ok);
    assertTrue(ok);
  }

  @Test
  public void test02_getSingleResult() {
    StackTracer.printCurrentTestMethod();

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      EC = dao.getSingleResult(EtatCivil.class, "abrev", "C");
      PS = dao.getSingleResult(Parti.class, "abrev", "PSS");
      FR = dao.getSingleResult(Canton.class, "abrev", "FR");
      CF = dao.getSingleResult(Conseil.class, "abrev", "CF");
      ok = EC != null && PS!=null && FR!=null && CF!=null;
    }

    // on affiche le résultat
    StackTracer.printTestResult("EtatCivil", EC, "Parti", PS, "Canton", FR, "Conseil", CF);
    assertTrue(ok);
  }

  @Test
  public void test03_getSingleResult_with_Search() {
    StackTracer.printCurrentTestMethod();
    final String NOM_RECHERCHE = "Berset";
    Conseiller c = null;

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      Search s = new Search(Conseiller.class);
      s.addFilterEqual("nom", NOM_RECHERCHE);
      s.addFilterAnd();
      s.addFilterEqual("prenom", "Alain");
      s.addFilterAnd();
      s.addFilterEqual("sexe", "m");
      s.addFilterAnd();
      s.addFilterEqual("canton", FR);
      c = dao.getSingleResult(s);
      ok = c != null && c.getNom().equals(NOM_RECHERCHE);
    }

    // on affiche le résultat
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Result", c, "Attached", dao.isMerged(c));
    assertTrue(ok);
  }

  @Test
  public void test04_create() {
    StackTracer.printCurrentTestMethod();
    Conseiller c = getNewConseiller();

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      ok = dao.create(c) == 1;
      lastPk = c.getPkConseiller();
    }

    // on affiche le résultat
    StackTracer.printTestResult("Conseiller", c, "PK", lastPk, "Attached", dao.isMerged(c));
    assertTrue(ok);
  }

  @Test
  public void test05_read() {
    StackTracer.printCurrentTestMethod();
    Conseiller c = null;

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      c = dao.read(Conseiller.class, lastPk, false, true);
      ok = c != null;
    }

    // on affiche le résultat
    StackTracer.printTestResult("Conseiller", c, "PK", lastPk, "Attached", dao.isMerged(c));
    assertTrue(ok);
  }

  @Test
  public void test06_update() {
    StackTracer.printCurrentTestMethod();
    Conseiller c = null;

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      c = dao.read(Conseiller.class, lastPk, false, false);
      if (c != null) {
        c.setPrenom("Juliette");
        ok = dao.update(c) == 1;
      }
    }
    StackTracer.printTestResult("Conseiller", c, "PK", lastPk, "Attached", dao.isMerged(c));
    assertTrue(ok);
  }

  @Test
  public void test07_delete() {
    StackTracer.printCurrentTestMethod();
    Conseiller c = null;

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      c = dao.read(Conseiller.class, lastPk, false, false);
      ok = dao.delete(Conseiller.class, lastPk) == 1;
    }
    StackTracer.printTestResult("Conseiller", c, "PK", lastPk, "Exist", dao.exists(Conseiller.class, lastPk));
    assertTrue(ok);
  }

  @Test
  public void test08_count() {
    StackTracer.printCurrentTestMethod();
    long count = 0;

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      count = dao.count(Conseiller.class);
      ok = count > 0;
    }
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Result", count + " (all people)");
    assertTrue(ok);
  }

  @Test
  public void test09_count() {
    StackTracer.printCurrentTestMethod();
    long count = 0;

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      count = dao.count(Conseiller.class, "actif", true);
      ok = count > 0;
    }
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Result", count + " (active people)");
    assertTrue(ok);
  }

  @Test
  public void test10_getMinIntValue() {
    StackTracer.printCurrentTestMethod();
    int minInt = 0;

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      minInt = dao.getMinIntValue(Conseiller.class, "pkConseiller");
      ok = minInt > 0;
    }
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Result", minInt + " (PK min)");
    assertTrue(ok);
  }

  @Test
  public void test11_getMaxIntValue() {
    StackTracer.printCurrentTestMethod();
    int maxInt = 0;

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      maxInt = dao.getMaxIntValue(Conseiller.class, "pkConseiller");
      ok = maxInt > 0;
    }
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Result", maxInt + " (PK max)");
    assertTrue(ok);
  }

  @Test
  public void test12_getPkMax() {
    StackTracer.printCurrentTestMethod();
    Object pkMax = null;

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      pkMax = dao.getPkMax(Conseiller.class);
      ok = pkMax != null;
    }

    // on affiche le résultat
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Result", pkMax);
    assertTrue(ok);
  }

  @Test
  public void test13_getList() {
    StackTracer.printCurrentTestMethod();
    int nb = 0;

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      // on appelle getList (sans filtrage)
      activites = dao.getList(Activite.class, "dateEntree");
      nb = activites.size();
      ok = nb > 0;
    }

    // on affiche le résultat
    StackTracer.printTestResult("Class", Activite.class.getSimpleName(), "Nb", nb + " (total)");
    if (ok && SHOW_LIST) {
      System.out.println();
      for (int i = 0; i < Math.min(LIST_MAXSIZE, nb); i++) {
        System.out.println("    " + activites.get(i));
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test14_getList() {
    StackTracer.printCurrentTestMethod();
    List<Conseiller> conseillers = new ArrayList<>();

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      // on appelle getList avec un filtrage simplifié (un seul champ peut être filtré à la fois)
      conseillers = dao.getList(Conseiller.class, "actif", true, "nom,prenom");
      ok = !conseillers.isEmpty();
    }

    // on affiche le résultat
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Nb", conseillers.size() + " (active people)");
    if (ok && SHOW_LIST) {
      System.out.println();
      for (int i = 0; i < Math.min(LIST_MAXSIZE, conseillers.size()); i++) {
        System.out.println("    " + conseillers.get(i));
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test15_getList_with_Search() {
    StackTracer.printCurrentTestMethod();
    List<Conseiller> conseillers = new ArrayList<>();

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      // on prépare une requête avec un objet Search
      Search s = new Search(Conseiller.class);
      s.addFilterEqual("parti", PS);
      s.addFilterAnd();
      s.addFilterEqual("canton", FR);
      s.addFilterAnd();
      s.addFilterEqual("actif", true);
      s.addSortAsc("nom", "prenom");

      // autre exemple pour le filtrage des données
//    Date d1 = DateTimeLib.stringToDate("01.01.1950");
//    Date d2 = DateTimeLib.stringToDate("31.12.1960");
//    s.addFilterBetween("dateNaissance", d1, d2);

      // on exécute la requête avec getList
      conseillers = dao.getList(s);
      ok = !conseillers.isEmpty();
    }

    // on affiche le résultat
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Nb", conseillers.size() + " (active people PS FR)");
    if (ok && SHOW_LIST) {
      System.out.println();
      for (int i = 0; i < conseillers.size(); i++) {
        System.out.println("    " + conseillers.get(i));
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test16_getList_with_Search2() {
    StackTracer.printCurrentTestMethod();
    List<Conseiller> conseillers = new ArrayList<>();

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      // avec Search2, il faut spécifier la requête JPQL
      Search2 search = new Search2("SELECT c FROM Conseiller c");

      // avec Search2, c'est des AND automatiquement entre les filtres
      search.addFilterEqual("c.actif", true);
      search.addFilterLike("c.nom", "B%");
      search.addSortFields("c.nom", "c.prenom");

      // on exécute la requête avec getList
      conseillers = dao.getList(search);

      // on traite le résultat
      ok = !conseillers.isEmpty();
    }

    // on affiche le résultat
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Nb", conseillers.size() + " (active people with name 'B*')");
    if (ok && SHOW_LIST) {
      System.out.println();
      for (int i = 0; i < Math.min(LIST_MAXSIZE, conseillers.size()); i++) {
        Conseiller c = conseillers.get(i);
        System.out.println("    " + c);
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test17_getList_with_Search2() {
    StackTracer.printCurrentTestMethod();
    List<Activite> activitesCF = new ArrayList<>();

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      // avec Search2, il faut donner la requête JPQL (avantage : jointure entre plusieurs entity beans possible)
      String jpql = "SELECT DISTINCT a FROM Activite a LEFT JOIN a.conseiller c WHERE a.conseiller=c";

      // avec Search2, c'est des AND automatiquement entre les filtres
      Search2 search = new Search2(jpql);
      search.addFilterEqual("c.actif", true);
      search.addFilterEqual("a.conseil", CF);
      search.addFilterIsNull("a.dateSortie");
      search.addSortFields("c.nom", "c.prenom");
      activitesCF = dao.getList(search);
      ok = !activitesCF.isEmpty();
    }

    // on affiche le résultat
    StackTracer.printTestResult("Classes", Conseiller.class.getSimpleName() + "+"
      + Activite.class.getSimpleName(), "Nb", activitesCF.size() + " (active people in federal council)");
    if (ok && SHOW_LIST) {
      System.out.println();
      for (int i = 0; i < activitesCF.size(); i++) {
        Activite a = activitesCF.get(i);
        System.out.println("    " + a.getConseiller() + " au " + a);
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test18_getList_paged() {
    StackTracer.printCurrentTestMethod();

    // on définit la pagination
    int pageSize = 1000;
    int lastPage = 0;

    // on prépare une requête Search
    Search search = new Search(Conseiller.class);
    search.addSortAsc("nom", "prenom");

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {

      // on définit la pagination
      long count = dao.count(search);
      lastPage = (int) (count / pageSize) + 1;

      // on contrôle la dernière page
      ok = lastPage > 1;
    }
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Last page", lastPage);

    // on lit page par page
    if (ok) {
      int totalSize = 0;
      for (int pageNumber = 1; pageNumber <= lastPage; pageNumber++) {
        int firstResult = (pageNumber - 1) * pageSize;
        search.setMaxResults(pageSize);
        search.setFirstResult(firstResult);
        List<Conseiller> conseillers = dao.getList(search);
        int size = conseillers.size();
        totalSize += size;
        if (size > 0 && SHOW_LIST) {
          if (pageNumber == 1) {
            System.out.println();
          }
          Conseiller c1 = conseillers.get(0);
          Conseiller cN = conseillers.get(size - 1);
          System.out.println("    page: " + pageNumber + ", size: " + size + ", " + c1 + " ... " + cN);
        }
      }

      // on contrôle le résultat
      ok = ok && (totalSize == dao.count(search));
    }
    assertTrue(ok);
  }

  @Test
  public void test19_getAggregateList_with_Search() {
    StackTracer.printCurrentTestMethod();
    List<Object> list = new ArrayList<>();

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      // on prépare la requête (nb de conseillers par parti qui dépasse 100 depuis 1848)
      Search search = new Search(Conseiller.class);
      search.addFields("parti", "count(*)");
      search.addGroupByField("parti");
      search.addHavingCondition("count(*) >= 100");
      search.addSortAsc("parti.nom");

      // on exécute la requête
      list = dao.getAggregateList(search);
      ok = !list.isEmpty();
    }

    // on affiche le résultat
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Nb", list.size()
      + " (political parties with more than 100 advisors since 1848)");
    if (ok && SHOW_LIST) {
      System.out.println();
      for (int i = 0; i < list.size(); i++) {
        Object[] objs = (Object[]) list.get(i);
        Parti p = (Parti) objs[0];
        long nb = (Long) objs[1];
        System.out.println("    " + p + " = " + nb);
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test20_getAggregateList_with_Search2() {
    StackTracer.printCurrentTestMethod();
    List<Object> list = new ArrayList<>();

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {

      // avec Search2, il faut donner la base de la requête JPQL (avantage : jointure entre plusieurs classes-entités possible)
      String jpql = "SELECT c.canton, count(distinct c) FROM Activite a LEFT JOIN a.conseiller c WHERE a.conseiller=c";

      // on prépare la requête (nb de conseillers fédéraux par canton)
      Search2 search = new Search2(jpql);
      search.addFilterEqual("a.conseil", CF);
      search.addGroupByField("c.canton");
      search.addSortFields("c.canton.abrev");

      // on exécute la requête
      list = dao.getAggregateList(search);
      ok = !list.isEmpty();
    }

    // on affiche le résultat
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Nb", list.size() + " (cantons with CF advisors)");
    if (ok && SHOW_LIST) {
      System.out.println();
      for (int i = 0; i < list.size(); i++) {
        Object[] objs = (Object[]) list.get(i);
        long nb = (Long) objs[1];
        System.out.println("    " + objs[0] + " = " + nb);
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test21_getList_with_native_sql() {
    StackTracer.printCurrentTestMethod();
    List<Conseiller> conseillers = new ArrayList<>();

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      // on prépare la requête SQL native
      int pk = FR.getPkCanton();
      String sql = "SELECT * FROM t_conseiller WHERE fkCanton=?1 AND actif=true ORDER BY nom,prenom";
      String[] params = {"" + pk};

      // on exécute la requête
      conseillers = dao.getList(sql, params, "ConseillerResult");
      ok = !conseillers.isEmpty();
    }

    // on affiche le résultat
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Nb", conseillers.size() + " (current advisors FR)");
    if (ok && SHOW_LIST) {
      System.out.println();
      for (Conseiller c : conseillers) {
        EtatCivil ec = dao.read(EtatCivil.class, c.getFkEtatCivilSQL(), false, true);
        Canton ct = dao.read(Canton.class, c.getFkCantonSQL(), false, true);
        Parti p = dao.read(Parti.class, c.getFkPartiSQL(), false, true);
        c.setParti(p);
        c.setCanton(ct);
        System.out.println("    " + c + " ("+c.getCanton() +"), "+ c.getParti());
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test22_executeCommand_with_native_sql() {
    StackTracer.printCurrentTestMethod();
    int n1 = 0;
    int n2 = 0;

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {

      // on prépare et exécute une première commande (modification de l'abréviation du canton)
      int pk = FR.getPkCanton();
      String sql = "UPDATE t_canton SET abrev='FF' WHERE pkCanton=" + pk;
      n1 = dao.executeCommand(sql);

      // on remet l'abréviation comme avant
      sql = "UPDATE t_canton SET abrev='" + FR.getAbrev() + "' WHERE pkCanton=" + pk;
      n2 = dao.executeCommand(sql);

      // on relit pour vérifier si le canton est comme avant
      Canton canton = dao.read(Canton.class, pk, false, true);

      // on traite les résultats
      ok = (n1 + n2 == 2) && (canton.getAbrev().equals("FR"));
    }

    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Updated", n1 + n2);
    assertTrue(ok);
  }

  @Test
  public void test23_deleteAll() {
    StackTracer.printCurrentTestMethod();
    int deleted = 0;

    // si une connexion valide est présente
    boolean ok = dao.isConnected() && activites != null;
    if (ok) {
      long before = dao.count(Activite.class);
      deleted = dao.deleteAll(Activite.class);
      long after = dao.count(Activite.class);
      ok = ok && (deleted > 0) && (after == 0);
    }

    // on affiche le résultat
    StackTracer.printTestResult("Class", Activite.class.getSimpleName(), "Deleted", deleted);
    assertTrue(ok);
  }

  @Test
  public void test24_insertList() {
    StackTracer.printCurrentTestMethod();
    int added = 0;

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      // on exécute d'abord une commande pour avoir l'autoincrément depuis 1
      int nb = dao.executeCommand("ALTER TABLE t_activite AUTO_INCREMENT=1");

      // on insert la liste
      added = dao.insertList(Activite.class, activites, true);
      ok = nb >= 0 && added > 0;
    }

    // on affiche le résultat
    StackTracer.printTestResult("Class", Activite.class.getSimpleName(), "Added", added);
    assertTrue(ok);
  }

  @Test
  public void test25_updateList() {
    StackTracer.printCurrentTestMethod();
    int updated = 0;

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {

      // on modifie l'ensemble des partis
      List<Parti> partis = dao.getList(Parti.class, "nom");
      for (Parti parti : partis) {
        parti.setNom(parti.getNom() + "_");
      }
      ok = partis != null && !partis.isEmpty();

      // on exécute la mise à jour
      int n1[] = dao.updateList(Parti.class, partis);

      // on remet comme avant
      for (Parti parti : partis) {
        parti.setNom(ConvertLib.replace(parti.getNom(), "_", "", 999));
      }
      int n2[] = dao.updateList(Parti.class, partis);

      // on traite le résultat
      ok = ok && n1[0] > 0 & n2[0] > 0;
      updated = n2[0];
    }

    StackTracer.printTestResult("Class", Parti.class.getSimpleName(), "Updated", updated);
    assertTrue(ok);
  }

  @Test
  public void test26_rollbackManualTransaction() {
    StackTracer.printCurrentTestMethod();
    String before = "";
    String after = "";

    // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {

      // on récupère le gestionnaire de transactions et on démarre une transaction manuelle
      Transaction tr = dao.getTransaction();
      tr.beginManualTransaction();

      // on lit le dernier conseiller et le dernier parti
      Conseiller c1 = dao.read(Conseiller.class, dao.getPkMax(Conseiller.class), false, true);
      Parti p1 = dao.read(Parti.class, dao.getPkMax(Parti.class), false, true);
      before = c1.getNom() + " & " + p1.getNom();

      // on ajoute un nouveau conseiller
      c1 = getNewConseiller();
      boolean ok1 = (dao.create(c1) == 1);

      // on ajoute un nouveau parti
      p1 = new Parti();
      p1.setNom("???");
      boolean ok2 = (dao.create(p1) == 1);

      // on rollback la transaction
      try {
        tr.rollbackManualTransaction();
      } catch (Exception e) {
        tr.finishManualTransaction();
      }

      // on relit le dernier conseiller et le dernier parti
      Conseiller c2 = dao.read(Conseiller.class, dao.getPkMax(Conseiller.class), false, true);
      Parti p2 = dao.read(Parti.class, dao.getPkMax(Parti.class), false, true);
      after = c2.getNom() + " & " + p2.getNom();

      // on teste l'assertion
      ok = ok1 && after.equals(before);
    }

    // on affiche le résultat
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Before", before, "After", after);
    assertTrue(ok);
  }

  @Test
  public void test27_getEntityInfo() {
    StackTracer.printCurrentTestMethod();
    EntityInfo ei = dao.getEntityInfo(Conseiller.class);
    boolean ok = ei != null;
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Info", ei);
    assertTrue(ok);
  }

  @Test
  public void test28_getEntityFields() {
    StackTracer.printCurrentTestMethod();

    // on récupère la liste des attributs dams l'entity-bean
    List<Field> list = dao.getEntityFields(Conseiller.class);
    boolean ok = false;
    int size = 0;
    if (list != null && !list.isEmpty()) {
      ok = true;
      size = list.size();
    }
    StackTracer.printTestResult("Class", Conseiller.class.getSimpleName(), "Nb", size);
    if (ok && SHOW_LIST) {
      System.out.println();
      for (Field field : list) {
        System.out.println("    " + field.getName());
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test29_getEntitiesMap() {
    StackTracer.printCurrentTestMethod();

    // on récupère une map des classes-entités
    Map<Class<?>, EntityInfo> map = dao.getEntitiesMap();
    boolean ok = !map.isEmpty();
    StackTracer.printTestResult("Nb", map.size());
    if (ok && SHOW_LIST) {
      System.out.println();
      for (Map.Entry<Class<?>, EntityInfo> entry : map.entrySet()) {
        System.out.println("    " + entry.getValue());
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test30_disconnect() {
    StackTracer.printCurrentTestMethod();
       // si une connexion valide est présente
    boolean ok = dao.isConnected();
    if (ok) {
      dao.disconnect();
      ok = !dao.isConnected();
    }
    StackTracer.printTestResult("Disconnected", ok);
    assertTrue(ok);
  }

}
