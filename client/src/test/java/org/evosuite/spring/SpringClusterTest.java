package org.evosuite.spring;

import org.junit.Test;

public class SpringClusterTest {

    @Test
    public void test0() {
        SpringCluster.getInstance().findResultMatcherGenerators();
        System.out.println(SpringCluster.getInstance());
    }

    @Test
    public void test1() {
        SpringCluster.getInstance().findRequestBuilderGenerators();
        System.out.println(SpringCluster.getInstance());
    }

    @Test
    public void test2() {
        SpringCluster.getInstance().findGlobalResultMatcherGenerators();
        System.out.println(SpringCluster.getInstance());
    }

    @Test
    public void test3() {
        SpringCluster.getInstance().findRequestBuilderGenerators();
        SpringCluster.getInstance().findResultMatcherGenerators();
        System.out.println(SpringCluster.getInstance());
    }
}
