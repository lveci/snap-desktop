package org.esa.snap.tango.internal;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Generates the {@link org.esa.snap.tango.TangoIcons} class.
 *
 * @author Norman Fomferra
 */
public class Generator {
    public static void main(String[] args) throws URISyntaxException, IOException {

        Path mainTemplateFile = Paths.get("src", "main", "resources", "org", "esa", "snap", "tango", "internal", "TangoIcons.vm");
        Path testTemplateFile = Paths.get("src", "main", "resources", "org", "esa", "snap", "tango", "internal", "TangoIconsTest.vm");

        Path mainClassFile = Paths.get("src", "main", "java", "org", "esa", "snap", "tango", "TangoIcons.java");
        Path testClassFile = Paths.get("src", "test", "java", "org", "esa", "snap", "tango", "TangoIconsTest.java");

        String mainTemplate = new String(Files.readAllBytes(mainTemplateFile));
        String testTemplate = new String(Files.readAllBytes(testTemplateFile));

        Files.createDirectories(mainClassFile.getParent());
        Files.createDirectories(testClassFile.getParent());


        File dir16 = Paths.get("src", "main", "resources","tango", "16x16").toFile();
        File dir22 = Paths.get("src", "main", "resources",  "tango", "22x22").toFile();
        File dir32 = Paths.get("src", "main", "resources", "tango", "32x32").toFile();

        StringBuilder mainCode = new StringBuilder();
        StringBuilder testCode = new StringBuilder();

        File[] subDirs = dir16.listFiles(File::isDirectory);
        for (File subDir : subDirs) {
            System.out.println("subDir = " + subDir);
            File[] iconFiles = subDir.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".png"));
            String dirName = subDir.getName();
            for (File iconFile : iconFiles) {
                String fileName = iconFile.getName();
                String methodName = dirName.toLowerCase() + "_" + fileName.substring(0, fileName.length() - 4).replace("-", "_");
                mainCode.append(String.format("    public static Icon %s(Res res) { return getIcon(\"%s\", res); }\n", methodName, fileName));
                testCode.append(String.format("        assertNotNull(TangoIcons.%s(TangoIcons.R16));\n", methodName));
                testCode.append(String.format("        assertNotNull(TangoIcons.%s(TangoIcons.R22));\n", methodName));
                testCode.append(String.format("        assertNotNull(TangoIcons.%s(TangoIcons.R32));\n", methodName));
                testCode.append("\n");
            }
        }

        mainTemplate = mainTemplate.replace("${code}", mainCode.toString());
        testTemplate = testTemplate.replace("${code}", testCode.toString());

        Files.write(mainClassFile, mainTemplate.getBytes(), StandardOpenOption.SYNC);
        Files.write(testClassFile, testTemplate.getBytes(), StandardOpenOption.SYNC);
    }
}
