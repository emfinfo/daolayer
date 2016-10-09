package tests;

import beans.Activite;
import beans.Canton;
import beans.Conseil;
import beans.Conseiller;
import beans.Parti;
import ch.emf.dao.EntityInfo;
import ch.emf.dao.JpaDaoAPI;
import ch.emf.dao.Transaction;
import ch.emf.dao.filtering.Search;
import ch.emf.dao.filtering.Search2;
import ch.jcsinfo.datetime.DateTimeLib;
import ch.jcsinfo.file.FileHelper;
import ch.jcsinfo.system.StackTracer;
import ch.jcsinfo.util.ConvertLib;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import workers.DbWorker;
import workers.DbWorkerAPI;
import workers.FileWorker;

/**
 * Test des principales fonctionnalités de la couche DAO sur une base de données
 * MySql. Les données pour monter la base se trouvent dans le dossier "data" de
 * ce projet.
 *
 * @author Jean-Claude Stritt
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JpaDaoTest {
  private static final String PU = "parlementPU";
  private static final String CHEMIN_DONNEES = "data";
  private static final String FICHIER_CONSEILLERS = "Ratsmitglieder_1848_FR_Windows_latin1.csv";
  private static final String SCRIPT_DELETE_ALL = "db-delete-all.sql";
  private static final String SCRIPT_IMPORT_LOGINS = "db-import-logins.sql";

  private static final boolean IMPORT_DB = false; // importer la DB depuis le fichier csv (env. 2 minutes)
  private static final boolean SHOW_LIST = true; // voir un extrait des listes extraites
  private static final int LIST_MAXSIZE = 4; // le maximum d'entrées affichées pour les longues listes

  private static DbWorkerAPI dbWrk = null;
  private static JpaDaoAPI dao = null; // juste là pour tester
  private static int lastPk = -1;

  private static Parti PS = null;
  private static Canton FR = null;
  private static Conseil CF = null;
  private static List<Activite> activites;

  /*
   * SETUP ET TEARDOWN (METHODES AVANT ET APRES LES TESTS)
   */
  @BeforeClass
  public static void setUpClass() throws Exception {
    System.out.println("\n>>> " + StackTracer.getCurrentClass() + " <<<");

    // ouvre la BD et récupère une instance sur le worker et la couche dao
    dbWrk = DbWorker.getInstance();
    dao = dbWrk.getDao(); // uniquement tests unitaires, dans une app réelle tout dans DbWorker

    // si la BD est ouverte
    if (dao.isOpen()) {
      System.out.println(dao.getVersion());
      if (IMPORT_DB) {
        int n1 = dbWrk.executerScript(FileHelper.normalizeFileName(CHEMIN_DONNEES + "/" + SCRIPT_DELETE_ALL));
        int n2 = dbWrk.executerScript(FileHelper.normalizeFileName(CHEMIN_DONNEES + "/" + SCRIPT_IMPORT_LOGINS));
        FileWorker fileWrk = new FileWorker();
        fileWrk.importerDonneesFichier(FileHelper.normalizeFileName(CHEMIN_DONNEES + "/" + FICHIER_CONSEILLERS));
      }
      PS = dbWrk.rechercherParti("Parti socialiste suisse");
      FR = dbWrk.rechercherCanton("FR");
      CF = dbWrk.rechercherConseil("CF");
    } else {
      System.out.println("ERREUR: LA BD N'A PAS PU ÊTRE OUVERTE !!!");
    }
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    dbWrk.fermerBD();
    System.out.println();
  }


  /*
   * METHODES PRIVEES
   */
  private Conseiller getNewConseiller() {
    Conseiller c = new Conseiller();
    c.setPrenom("Jules");
    c.setNom("Tartampion");
    c.setSexe("m");
    c.setOrigine("Fribourg");
    c.setDateNaissance(DateTimeLib.stringToDate("1.1.1970"));
    c.setDateDeces(null);
    c.setParti(PS);
    c.setCanton(FR);
    return c;
  }


  /*
   * TESTS
   */
  @Test
  public void test01_create() {
    StackTracer.printCurrentTestMethod();
    Conseiller c = getNewConseiller();
    boolean ok = dao.create(c) == 1;
    lastPk = c.getPkConseiller();
    StackTracer.printTestInfo(c, lastPk + " (pk)");
    assertTrue(ok);
  }

  @Test
  public void test02_read() {
    StackTracer.printCurrentTestMethod();
    Conseiller c = dao.read(Conseiller.class, lastPk, false, true);
    boolean ok = c != null;
    StackTracer.printTestInfo(lastPk + " (pk)", c +", attached: " + dao.isMerged(c));
    assertTrue(ok);
  }

  @Test
  public void test03_update() {
    StackTracer.printCurrentTestMethod();
    boolean ok = false;
    Conseiller c = dao.read(Conseiller.class, lastPk, false, false);
    if (c != null) {
      c.setPrenom("Juliette");
      ok = dao.update(c) == 1;
    }
    StackTracer.printTestInfo(lastPk + " (pk)", c);
    assertTrue(ok);
  }

  @Test
  public void test04_delete() {
    StackTracer.printCurrentTestMethod();
    Conseiller c = dao.read(Conseiller.class, lastPk, false, false);
    boolean ok = dao.delete(Conseiller.class, lastPk) == 1;
    StackTracer.printTestInfo(lastPk + " (pk)", ok);
    assertTrue(ok);
  }

  @Test
  public void test05_exists() {
    StackTracer.printCurrentTestMethod();
    boolean ok = dao.exists(Conseiller.class, 1);
    StackTracer.printTestInfo("1 (pk)", ok);
    assertTrue(ok);
  }

  @Test
  public void test06_notExists() {
    StackTracer.printCurrentTestMethod();
    boolean ok = !dao.exists(Conseiller.class, lastPk);
    StackTracer.printTestInfo(lastPk + " (pk)", ok);
    assertTrue(ok);
  }

  @Test
  public void test07_count() {
    StackTracer.printCurrentTestMethod();
    long count = dao.count(Conseiller.class);
    boolean ok = count > 0;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), count + " (total depuis 1848) ");
    assertTrue(ok);
  }

  @Test
  public void test08_count() {
    StackTracer.printCurrentTestMethod();
    long count = dao.count(Conseiller.class, "actif", true);
    boolean ok = count > 0;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), count + " (actifs)");
    assertTrue(ok);
  }

  @Test
  public void test09_getMinIntValue() {
    StackTracer.printCurrentTestMethod();
    int minInt = dao.getMinIntValue(Conseiller.class, "pkConseiller");
    boolean ok = minInt > 0;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), minInt + " (pk min)");
    assertTrue(ok);
  }

  @Test
  public void test10_getMaxIntValue() {
    StackTracer.printCurrentTestMethod();
    int maxInt = dao.getMaxIntValue(Conseiller.class, "pkConseiller");
    boolean ok = maxInt > 0;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), maxInt + " (pk max)");
    assertTrue(ok);
  }

  @Test
  public void test11_getPkMax() {
    StackTracer.printCurrentTestMethod();
    Object pkMax = dao.getPkMax(Conseiller.class);
    boolean ok = pkMax != null;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), pkMax + " (pk)");
    assertTrue(ok);
  }

  @Test
  public void test12_getSingleResult() {
    StackTracer.printCurrentTestMethod();
    final String CONSEIL_RECHERCHE = "CF";
    Conseil conseil = dao.getSingleResult(Conseil.class, "abrev", CONSEIL_RECHERCHE);
    boolean ok = conseil != null && conseil.getAbrev().equals(CONSEIL_RECHERCHE);
    StackTracer.printTestInfo(Conseil.class.getSimpleName(), conseil);
    assertTrue(ok);
  }

  @Test
  public void test13_getSingleResult_with_Search() {
    StackTracer.printCurrentTestMethod();
    final String NOM_RECHERCHE = "Berset";
    Search s = new Search(Conseiller.class);
    s.addFilterEqual("nom", NOM_RECHERCHE);
    s.addFilterAnd();
    s.addFilterEqual("prenom", "Alain");
    s.addFilterAnd();
    s.addFilterEqual("sexe", "m");
    s.addFilterAnd();
    s.addFilterEqual("canton", FR);
    Conseiller conseiller = dao.getSingleResult(s);
    boolean ok = conseiller != null && conseiller.getNom().equals(NOM_RECHERCHE);
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), conseiller);
    assertTrue(ok);
  }

  @Test
  public void test14_getList() {
    StackTracer.printCurrentTestMethod();

    // on appelle getList (sans filtrage)
    activites = dao.getList(Activite.class, "dateEntree");

    // on traite le résultat
    boolean ok = activites.size() > 0;
    StackTracer.printTestInfo(Activite.class.getSimpleName(), activites.size() + " (total)");
    if (ok && SHOW_LIST) {
      System.out.println();
      for (int i = 0; i < Math.min(LIST_MAXSIZE, activites.size()); i++) {
        System.out.println("    " + activites.get(i));
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test15_getList() {
    StackTracer.printCurrentTestMethod();

    // on appelle getList avec un filtrage simplifié (un seul champ peut être filtré à la fois)
    List<Conseiller> conseillers = dao.getList(Conseiller.class, "actif", true, "nom,prenom");

    // on traite le résultat
    boolean ok = conseillers.size() > 0;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), conseillers.size() + " (actifs)");
    if (ok && SHOW_LIST) {
      System.out.println();
      for (int i = 0; i < Math.min(LIST_MAXSIZE, conseillers.size()); i++) {
        System.out.println("    " + conseillers.get(i));
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test16_getList_with_Search() {
    StackTracer.printCurrentTestMethod();

    // on prépare une requête avec un objet Search
    Search search = new Search(Conseiller.class);
    search.addFilterEqual("parti", PS);
    search.addFilterAnd();
    search.addFilterEqual("canton", FR);
    search.addFilterAnd();
    search.addFilterEqual("actif", true);
    search.addSortAsc("nom", "prenom");

    // autre exemple pour le filtrage des données
//    Date d1 = DateTimeLib.stringToDate("01.01.1950");
//    Date d2 = DateTimeLib.stringToDate("31.12.1960");
//    search.addFilterBetween("dateNaissance", d1, d2);

    // on exécute la requête avec getList
    List<Conseiller> conseillers = dao.getList(search);

    // on traite le résultat
    boolean ok = conseillers.size() > 0;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), conseillers.size() + " (actifs PS FR)");
    if (ok && SHOW_LIST) {
      System.out.println();
      for (int i = 0; i < conseillers.size(); i++) {
        System.out.println("    " + conseillers.get(i));
      }
    }
    assertTrue(ok);

  }

  @Test
  public void test17_getList_with_Search2() {
    StackTracer.printCurrentTestMethod();

    // avec Search2, il faut spécifier la requête JPQL
    Search2 search = new Search2("SELECT c FROM Conseiller c");

    // avec Search2, c'est des AND automatiquement entre les filtres
    search.addFilterEqual("c.actif", true);
    search.addFilterLike("c.nom", "B%");
    search.addSortFields("c.nom", "c.prenom");

    // on exécute la requête avec getList
    List<Conseiller> conseillers = dao.getList(search);

    // on traite le résultat
    boolean ok = conseillers.size() > 0;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName() , conseillers.size() + " (actifs avec nom B*)");
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
  public void test18_getList_with_Search2() {
    StackTracer.printCurrentTestMethod();

    // avec Search2, il faut donner la requête JPQL (avantage : jointure entre plusieurs entity beans possible)
    String jpql = "SELECT distinct a FROM Activite a LEFT JOIN a.conseiller c WHERE a.conseiller=c";

    // avec Search2, c'est des AND automatiquement entre les filtres
    Search2 search = new Search2(jpql);
    search.addFilterEqual("c.actif", true);
    search.addFilterEqual("a.conseil", CF );
    search.addFilterIsNull("a.dateSortie");
    search.addSortFields("c.nom", "c.prenom");

    // on appelle getList
    List<Activite> activitesCF = dao.getList(search);

    // on traite la réponse
    boolean ok = activitesCF.size() > 0;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName() + "+"
      + Activite.class.getSimpleName(), activitesCF.size() + " (conseillers fédéraux actuels)");
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
  public void test19_getList_paged() {
    StackTracer.printCurrentTestMethod();

    // on prépare une requête Search
    Search search = new Search(Conseiller.class);
    search.addSortAsc("nom", "prenom");

    // on définit la pagination
    int pageSize = 1000;
    long count = dao.count(search);
    int lastPage = (int) (count / pageSize) + 1;

    // on contrôle la dernière page
    boolean ok = lastPage > 1;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), lastPage);

    // on lit page par page
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
    assertTrue(ok);
  }

  @Test
  public void test20_getAggregateList_with_Search() {
    StackTracer.printCurrentTestMethod();

    // on prépare la requête (nb de conseillers par parti qui dépasse 100 depuis 1848)
    Search search = new Search(Conseiller.class);
    search.addFields("parti", "count(*)");
    search.addGroupByField("parti");
    search.addHavingCondition("count(*) >= 100");
    search.addSortAsc("parti.nomParti");

    // on exécute la requête
    List<Object> list = dao.getAggregateList(search);

    // on traite le résultat
    boolean ok = list.size() > 0;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), list.size() +  " (partis avec plus de 100 c. depuis 1848)");
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
  public void test21_getAggregateList_with_Search2() {
    StackTracer.printCurrentTestMethod();

    // avec Search2, il faut donner la base de la requête JPQL (avantage : jointure entre plusieurs classes-entités possible)
    String jpql = "SELECT c.canton,count(a.pkActivite) FROM Activite a LEFT JOIN a.conseiller c WHERE a.conseiller=c";

    // on prépare la requête (nb de conseillers fédéraux par canton)
    Search2 search = new Search2(jpql);
    search.addFilterEqual("a.conseil", CF );
    search.addGroupByField("c.canton");
//    search.addHavingCondition("count(a.pkActivite) >= 4");
    search.addSortFields("c.canton.abrev");

    // on exécute la requête
    List<Object> list = dao.getAggregateList(search);

    // on traite le résultat
    boolean ok = list.size() > 0;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), list.size() + " (cantons avec CF)");
    if (ok && SHOW_LIST) {
      System.out.println();
      for (int i = 0; i < list.size(); i++) {
        Object[] objs = (Object[]) list.get(i);
        long nb = (Long) objs[1];
        System.out.println("    " + objs[0] + " = "+nb);
      }
    }
    assertTrue(ok);
  }



  @Test
  public void test22_getList_with_native_sql() {
    StackTracer.printCurrentTestMethod();

    // prépare la requête SQL native
    int pk = FR.getPkCanton();
    String sql = "SELECT * FROM t_conseiller WHERE fkCanton=?1 AND actif=true ORDER BY nom,prenom";
    String[] params = {"" + pk};

    // on exécute la requête
    List<Conseiller> conseillers = dao.getList(sql, params, "ConseillerResult");

    // on traite le résultat
    boolean ok = conseillers.size() > 0;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), conseillers.size() + " (conseillers actuels FR)");
    if (ok && SHOW_LIST) {

      // mise à jour des objets liés, puis affichage
      System.out.println();
      for (Conseiller c : conseillers) {
        Parti p = dao.read(Parti.class, c.getFkPartiSQL(), false, true);
        Canton ct = dao.read(Canton.class, c.getFkCantonSQL(), false, true);
        c.setParti(p);
        c.setCanton(ct);
        System.out.println("    " + c + " ("+c.getCanton() +"), "+ c.getParti());
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test23_executeCommand_with_native_sql() {
    StackTracer.printCurrentTestMethod();

    // on prépare et exécute une première commande (modification de l'abréviation du canton)
    int pk = FR.getPkCanton();
    String sql = "UPDATE t_canton SET abrev='FF' WHERE pkCanton=" + pk ;
    int n1 = dao.executeCommand(sql);

    // on remet l'abréviation comme avant
    sql = "UPDATE t_canton SET abrev='" + FR.getAbrev() + "' WHERE pkCanton=" + pk;
    int n2 = dao.executeCommand(sql);

    // on relit pour vérifier si le canton est comme avant
    Canton canton = dao.read(Canton.class, pk, false, true);

    // on traite les résultats
    boolean ok = (n1 + n2 == 2) && (canton.getAbrev().equals("FR"));
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), n1+n2);
    assertTrue(ok);
  }

  @Test
  public void test24_deleteAll() {
    StackTracer.printCurrentTestMethod();
    boolean ok = activites != null;
    long before = dao.count(Activite.class);
    int n = dao.deleteAll(Activite.class);
    long m = dao.count(Activite.class);
    ok = ok && (n >= 0) && (m == 0);
    StackTracer.printTestInfo(Activite.class.getSimpleName(), n);
    assertTrue(ok);
  }

  @Test
  public void test25_insertList() {
    StackTracer.printCurrentTestMethod();

    // on exécute d'abord une commande pour avoir l'autoincrément depuis 1
    int n1 = dao.executeCommand("ALTER TABLE t_activite AUTO_INCREMENT=1");

    // on insert la liste
    int n2 = dao.insertList(Activite.class, activites, true);

    // on vérifie le résultat
    boolean ok = n1 >= 0 && n2 > 0;
    StackTracer.printTestInfo(Activite.class.getSimpleName(), n2);
    assertTrue(ok);
  }

  @Test
  public void test26_updateList() {
    StackTracer.printCurrentTestMethod();

    // on modifie l'ensemble des partis
    List<Parti> partis = dao.getList(Parti.class, "nomParti");
    for (Parti parti : partis) {
      parti.setNomParti(parti.getNomParti() + "_");
    }
    boolean ok = partis != null && partis.size() > 0;

    // on exécute la mise à jour
    int n1[] = dao.updateList(Parti.class, partis);

    // on remet comme avant
    for (Parti parti : partis) {
      parti.setNomParti(ConvertLib.replace(parti.getNomParti(), "_", "", 999));
    }
    int n2[] = dao.updateList(Parti.class, partis);

    // on traite le résultat
    ok = ok && n1[0] > 0 & n2[0] > 0;
    StackTracer.printTestInfo(Parti.class.getSimpleName(), n2[0]);
    assertTrue(ok);
  }

  @Test
  public void test27_rollbackManualTransaction() {
    StackTracer.printCurrentTestMethod();

    // on récupère le gestionnaire de transactions et on démarre une transaction manuelle
    Transaction tr = dao.getTransaction();
    tr.beginManualTransaction();

    // on lit le dernier conseiller et le dernier parti
    Conseiller c1 = dao.read(Conseiller.class, dao.getPkMax(Conseiller.class), false, false);
    Parti p1 = dao.read(Parti.class, dao.getPkMax(Parti.class), false, false);
    String before = c1.getNom() + " & " + p1.getNomParti();

    // on ajoute un nouveau conseiller
    c1 = getNewConseiller();
    boolean ok1 = (dao.create(c1) == 1);

    // on ajoute un nouveau parti
    p1 = new Parti();
    p1.setNomParti("???");
    boolean ok2 = (dao.create(p1) == 1);

    // on rollback la transaction
    try {
      tr.rollbackManualTransaction();
    } catch (Exception e) {
      tr.finishManualTransaction();
    }

    // on relit le dernier conseiller et le dernier parti
    Conseiller c2 = dao.read(Conseiller.class, dao.getPkMax(Conseiller.class), false, false);
    Parti p2 = dao.read(Parti.class, dao.getPkMax(Parti.class), false, false);
    String after = c2.getNom() + " & " + p2.getNomParti();

    // on teste l'assertion
    boolean ok = ok1 && after.equals(before);
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), before + ((ok)?" = ":" != ") + after);
    assertTrue(ok);
  }

  @Test
  public void test28_getEntityInfo() {
    StackTracer.printCurrentTestMethod();
    EntityInfo ei = dao.getEntityInfo(Conseiller.class);
    boolean ok = ei != null;
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), ei);
    assertTrue(ok);
  }

  @Test
  public void test29_getEntityFields() {
    StackTracer.printCurrentTestMethod();

    // on récupère la liste des attributs dams l'entity-bean
    List<Field> list = dao.getEntityFields(Conseiller.class);
    boolean ok = false;
    int size = 0;
    if (list != null && list.size() > 0) {
      ok = true;
      size = list.size();
    }
    StackTracer.printTestInfo(Conseiller.class.getSimpleName(), size);
    if (ok && SHOW_LIST) {
      System.out.println();
      for (Field field : list) {
        System.out.println("    " + field.getName());
      }
    }
    assertTrue(ok);
  }

  @Test
  public void test30_getEntitiesMap() {
    StackTracer.printCurrentTestMethod();

    // on récupère une map des classes-entités
    Map<Class<?>, EntityInfo> map = dao.getEntitiesMap();
    boolean ok = !map.isEmpty();
    StackTracer.printTestInfo("JPA entities map", map.size());
    if (ok && SHOW_LIST) {
      System.out.println();
      for (Map.Entry<Class<?>, EntityInfo> entry : map.entrySet()) {
        System.out.println("  - " + entry.getValue());
      }
    }
    assertTrue(ok);
  }
}
