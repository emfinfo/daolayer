# daolayer 5.1.2 - december 2016
A dao layer over JPA. With this library, you work with objects and lists of objects. No sql. Select queries can be made with a Search or Search2 object.

You can download and open this project in NetBeans 8.2. It's a Java 8 maven project. So, dependencies are loaded automaticly from maven central. There are some test classes where you can learn how to use this library.

In MacOS terminal or Windows console, you can start the "test" suite with a Maven command :

mvn test

Project documentation here :
http://jcstritt.emf-informatique.ch/doc/daolayer<br>

New in release 5.1.3 (5.1.2017) :
* Correct a bug in "inserList" and "updateList" (a flush is added to have a sorted list correct inserted).
* The tests are now adapted to new "parlement" database structure (version 2017)

New in release 5.1.2 (31.12.2016) :
* Correct a bug in initialisation of Transaction class : if a transaction is currently active, set autocommit flag to true immediatly. This preserve some tenancy properties stored in the current entity-manager transaction.
* Add NoTransactionJpa annotation in ch.emf.dao. It's for DbWorkerAPI classes to mark non JPA methods.

New in release 5.1.1 (29.12.2016) :
* Wwo "open" methods in JpaDaoAPI are now renamed "setEntityManager" when they accept an entity manager as parameter.

New in release 5.1.0 (11.10.2016) :
* New read method in API that can detach an object immediatly. This is better for standalone Java applications.
* Old read methods are deleted from API for better understanding of the new one.

New in release 5.0.37 (01.10.2016) :
* The "update" and "delete" methods returns now -1 if a concurrent access occurs. In JPA, you must add a @Version annotation on an "version" integer field in each class-entity where update or delete is possible.
* Change release numbering from MAJOR.MINOR to MAJOR.MINOR.PATCH.

New in release 5.36 (17.08.2016) :
* "getMaxStringValue" is now deleted from JpaDaoAPI.
* "getMinIntValue" is now added to JpaDaoAPI.
* "getMaxIntValue(Search)" is now renamed "getIntValue(Search)".
* Small bug correction in "Search2" (no more where key when no filter is added).

New in release 5.35 (23.04.2016) :
* "updateList(...)" in JpaDaoAPI now returns an integer array : [0]=number of updated objects, [1]=number of added objects.
