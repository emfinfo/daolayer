package tests;

import com.google.inject.AbstractModule;

public class GuiceModule extends AbstractModule {

  @Override
  protected void configure() {
//    bind(Connectable.class).to(ConnectWithPU.class);
//    bind(Connectable.class).toProvider(ConnectProvider.class);
//    bind(JpaDaoAPI.class).toProvider(DaoProvider.class);
  }

//  @Provides
//  public Connectable provideConnectable() {
//    return new ConnectWithPU("parlementPU");
//  }

}
