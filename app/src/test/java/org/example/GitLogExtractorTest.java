/*
 * This source file was generated by the Gradle 'init' task
 */
package org.example;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class GitLogExtractorTest {
    @Test
    public void GitLogExtractor() {
        GitLogExtractor gitLogExtractor = new GitLogExtractor();
        assertThrows(IOException.class, gitLogExtractor::extractGitLog);
    }
}