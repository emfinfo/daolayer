package tests;

import ch.emf.dao.JpaConnection;
import ch.emf.dao.JpaConnectionAPI;
import ch.jcsinfo.system.StackTracer;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Jean-Claude Stritt
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JpaConnectionTest {

  private static String PU = "parlementPU";
  private static JpaConnectionAPI jpaConn = null;
  private static boolean isHibernate;

  @BeforeClass
  public static void setUpClass() throws Exception {

    // pour Hibernate ...
    try {
      isHibernate = Class.forName("org.hibernate.ejb.HibernatePersistence") != null;
      URL url = JpaConnectionTest.class.getResource("META-INF/persistence.xml");
      System.out.println("persistence file: " + url);
      System.out.println("Is Hibernate: " + isHibernate);
//    HibernateUtil.getSessionFactory();
    } catch (Exception e) {
    }
    jpaConn = new JpaConnection();
    System.out.println("\n>>> " + StackTracer.getCurrentClass() + " <<<");
    System.out.println(jpaConn.getVersion());
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    jpaConn.deconnect();
    System.out.println();
  }

  private String getTestCurrentMethod() {
    StackTraceElement e[] = Thread.currentThread().getStackTrace();
    StackTraceElement trace = e[2];
    return "*** " + trace.getMethodName() + " --> ";
  }

  private String getApplicationPath() {
    String appPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
    try {
      appPath = URLDecoder.decode(appPath, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
    }
    if (appPath.endsWith("" + File.separatorChar)) {
      appPath = appPath.substring(0, appPath.length() - 1);
    }
    return appPath;
  }

  @Test
  public void test01_connect() {
    StackTracer.printCurrentTestMethod();
    boolean ok = jpaConn.connect(PU) != null;
    StackTracer.printTestInfo(PU, ok);
//    System.out.println(getTestCurrentMethod() + "ok=" + ok);
    assertTrue(ok);
  }

  @Test
  public void test02_deconnect() {
    StackTracer.printCurrentTestMethod();
    boolean ok1 = jpaConn.isConnected();
    jpaConn.deconnect();
    boolean ok2 = !jpaConn.isConnected();
    StackTracer.printTestInfo(PU, ok2);
    assertTrue(ok1 && ok2);
  }

  @Test
  public void test03_reconnect() {
    StackTracer.printCurrentTestMethod();
    jpaConn.connect(PU);
    boolean ok = jpaConn.reconnect() != null;
    StackTracer.printTestInfo(PU, ok);
    assertTrue(ok);
  }

}
