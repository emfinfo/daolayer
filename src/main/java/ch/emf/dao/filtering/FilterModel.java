package ch.emf.dao.filtering;

import java.lang.reflect.Field;

/**
 * Cette classe permet de créer une liste de beans mémorisant des "champs de
 * filtrage" issus d'un "entity bean". Cette liste est généralement
 * la source de données d'une "combobox" qui permet le choix d'un champ de
 * filtrage.
 *
 * @author Jean-Claude Stritt
 *
 * @hidden
 */
public class FilterModel  {
  int viewIdx;
  String viewName;
  Field field;

  public FilterModel(int viewIdx, String viewName, Field field) {
    this.viewIdx = viewIdx;
    this.viewName = viewName;
    this.field = field;
  }

  public int getViewIdx() {
    return viewIdx;
  }

  public void setViewIdx( int viewIdx ) {
    this.viewIdx = viewIdx;
  }

  public String getViewName() {
    return viewName;
  }

  public void setViewName(String viewName) {
    this.viewName = viewName;
  }

  public Field getField() {
    return field;
  }

  public void setField(Field fld) {
    this.field = fld;
  }


  @Override
  public String toString() {
    return viewName;
  }

}
