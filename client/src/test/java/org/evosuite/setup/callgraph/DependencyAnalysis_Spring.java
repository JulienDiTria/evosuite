package org.evosuite.setup.callgraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minidev.json.JSONUtil;
import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.setup.DependencyAnalysis;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DependencyAnalysis_Spring {

  @BeforeClass
  public static void initialize() {
    Properties.CRITERION = new Properties.Criterion[1];
    Properties.CRITERION[0] = Properties.Criterion.IBRANCH;
  }

  @Test
  public void testBaseEntity() throws ClassNotFoundException {
    Properties.LOG_LEVEL = "DEBUG";
    Properties.TARGET_CLASS = "com.examples.with.different.packagename.spring.petclinic.model.BaseEntity";

    // initialize classpath
    List<String> initClassPath = new ArrayList<>();
    String initCP = System.getProperty("user.dir") + "/target/test-classes/com/examples/with/different/packagename/spring";
    initClassPath.add(initCP);
    ClassPathHandler.getInstance().addElementToTargetProjectClassPath(initCP);

    String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
    List<String> classPath = new ArrayList<>();
    classPath.add(cp);

    // analyze class
    DependencyAnalysis.analyzeClass(
        "com.examples.with.different.packagename.spring.petclinic.model.BaseEntity",
        classPath);


    // check context
    String context2 = DependencyAnalysis
        .getCallGraph()
        .getAllContextsFromTargetClass(
            "com.examples.with.different.packagename.spring.petclinic.model.BaseEntity",
            "getId()I").toString();
    assertEquals(
        "[com.examples.with.different.packagename.spring.petclinic.model.BaseEntity:getId()I]",
        context2);
  }

  @Test
  public void testOwnerSave() throws ClassNotFoundException {
    Properties.LOG_LEVEL = "DEBUG";
    Properties.TARGET_CLASS = "com.examples.with.different.packagename.spring.petclinic.owner.OwnerRepository";

    // initialize classpath
    List<String> initClassPath = new ArrayList<>();
    String initCP = System.getProperty("user.dir") + "/target/test-classes/com/examples/with/different/packagename/spring";
    initClassPath.add(initCP);
    ClassPathHandler.getInstance().addElementToTargetProjectClassPath(initCP);

    String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
    List<String> classPath = new ArrayList<>();
    classPath.add(cp);

    // analyze class
    DependencyAnalysis.analyzeClass(
        "com.examples.with.different.packagename.spring.petclinic.owner.OwnerRepository",
        classPath);


    // check context
    String context2 = DependencyAnalysis
        .getCallGraph()
        .getAllContextsFromTargetClass(
            "com.examples.with.different.packagename.spring.petclinic.owner.OwnerRepository",
            "save(Lcom/examples/with/different/packagename/spring/petclinic/owner/Owner;)V").toString();
    assertEquals(
        "[com.examples.with.different.packagename.spring.petclinic.owner.OwnerRepository:save(Lcom/examples/with/different/packagename/spring/petclinic/owner/Owner;)V]",
        context2);

    CallGraph cg = DependencyAnalysis.getCallGraph();
    cg.getViewOfCurrentMethods().stream()
        .sorted(Comparator.comparing(CallGraphEntry::getClassName))
        .map(m -> m.getClassName() + " " + m.getMethodName() + cg.getCallsFromMethod(m).stream().map(g -> g.getClassName() + " " + g.getMethodName()).reduce("", (a, b) -> a + "\n\t" + b) + "\n")
        .forEach(System.out::println);
  }
}
