package org.example;

import io.quarkus.gizmo.ClassOutput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ClassWriter implements ClassOutput {
    private final File outFile;

    public ClassWriter(File outFile) {
        this.outFile = outFile;
    }

    @Override
    public void write(String s, byte[] bytes) {
        try {
            Files.write(outFile.toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
