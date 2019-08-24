package org.kostiskag.unitynetwork.tracker;

import org.junit.Test;
import org.junit.After;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class ReadPreferencesTest {
  @Test
  public void evaluatesExpression() throws IOException {
    var name = "testfile";
    var p = Path.of(name);
    Files.createFile(p);
    ReadTrackerPreferencesFile.GenerateFile(p);

    var r  = ReadTrackerPreferencesFile.ParseFile(p);

    assertEquals("UnityNetwork", r.netName);
    assertEquals(8000, r.auth);
    assertEquals("jdbc:sqlite:unity.db", r.databaseUrl);
    assertEquals("username", r.user);
    assertEquals("password", r.password);
    assertEquals(0, r.bncap);
    assertEquals(180, r.pingTime);
    assertEquals(true, r.gui);
    assertEquals(true, r.log);
    //System.out.println(r);
  }

  @After
  public void deleteFile() throws IOException {
    Files.delete(Path.of("testfile"));
  }
}