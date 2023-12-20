/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.setup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.evosuite.classpath.ClassPathHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by arcuri on 6/14/14.
 */
public class InheritanceTreeGeneratorTest {

    @Test
    public void canFindJDKData() {
        InheritanceTree it = InheritanceTreeGenerator.readJDKData();
        Assert.assertNotNull(it);
    }

    @Test
    public void createFromClassPathAndCheckSpringPetClinicBaseEntity() {
        // initialize classpath
        List<String> initClassPath = new ArrayList<>();
        String initCP = System.getProperty("user.dir") + "/target/test-classes";
        initClassPath.add(initCP);
        ClassPathHandler.getInstance().addElementToTargetProjectClassPath(initCP);


        String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
        List<String> classPath = Arrays.asList(cp.split(File.pathSeparator));

        InheritanceTree it = InheritanceTreeGenerator.createFromClassPath(classPath);
        Assert.assertNotNull(it);
        it.getAllClasses().stream().filter(c -> c.contains("BaseEntity"))
            .limit(30)
            .map(c -> it.getSubclasses(c) + " -> " + c + " -> " + it.getSuperclasses(c).toString())
            .forEach(c -> System.out.println(c));
    }

}
