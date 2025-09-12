package io.vacco.opt1x;

import io.vacco.opt1x.spring.OtSpringApp;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import java.util.*;

import static io.vacco.opt1x.schema.OtNodeFormat.*;
import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class OtSpringAppTest {

  static {
    describe("OtSpringApp parsing", () -> {
      it("Parses valid JSON single profile", () -> {
        var app = OtSpringApp.parse("/spring/flooper/dev");
        assertEquals("flooper", app.name);
        assertEquals(Collections.singletonList("dev"), app.profiles);
        assertEquals(json, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Parses valid JSON multi-profile", () -> {
        var app = OtSpringApp.parse("/spring/flooper/dev,stage");
        assertEquals("flooper", app.name);
        assertEquals(Arrays.asList("dev", "stage"), app.profiles);
        assertEquals(json, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Parses valid JSON with explicit suffix", () -> {
        var app = OtSpringApp.parse("/spring/flooper/dev,stage,qa.json");
        assertEquals("flooper", app.name);
        assertEquals(Arrays.asList("dev", "stage", "qa"), app.profiles);
        assertEquals(json, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Parses valid application namespace with default profile", () -> {
        var app = OtSpringApp.parse("/spring/application/default");
        assertEquals("application", app.name);
        assertEquals(Collections.singletonList("default"), app.profiles);
        assertEquals(json, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Parses valid JSON with hyphenated application", () -> {
        var app = OtSpringApp.parse("/spring/my-app/dev,prod");
        assertEquals("my-app", app.name);
        assertEquals(Arrays.asList("dev", "prod"), app.profiles);
        assertEquals(json, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Parses valid YAML single profile", () -> {
        var app = OtSpringApp.parse("/spring/flooper/dev.yml");
        assertEquals("flooper", app.name);
        assertEquals(Collections.singletonList("dev"), app.profiles);
        assertEquals(yaml, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Parses valid YAML with .yaml suffix", () -> {
        var app = OtSpringApp.parse("/spring/flooper/dev.yaml");
        assertEquals("flooper", app.name);
        assertEquals(Collections.singletonList("dev"), app.profiles);
        assertEquals(yaml, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Parses valid YAML multi-profile", () -> {
        var app = OtSpringApp.parse("/spring/flooper/dev,stage.yml");
        assertEquals("flooper", app.name);
        assertEquals(Arrays.asList("dev", "stage"), app.profiles);
        assertEquals(yaml, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Parses valid Properties single profile", () -> {
        var app = OtSpringApp.parse("/spring/flooper-dev.properties");
        assertEquals("flooper", app.name);
        assertEquals(Collections.singletonList("dev"), app.profiles);
        assertEquals(props, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Parses valid Properties multi-profile", () -> {
        var app = OtSpringApp.parse("/spring/flooper-dev,stage.properties");
        assertEquals("flooper", app.name);
        assertEquals(Arrays.asList("dev", "stage"), app.profiles);
        assertEquals(props, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Parses valid JSON with label", () -> {
        var app = OtSpringApp.parse("/spring/flooper/dev/main");
        assertEquals("flooper", app.name);
        assertEquals(Collections.singletonList("dev"), app.profiles);
        assertEquals(json, app.format);
        assertEquals("main", app.label);
        System.out.println(app);
      });

      it("Parses valid JSON multi-profile with label", () -> {
        var app = OtSpringApp.parse("/spring/flooper/dev,stage/v1.0");
        assertEquals("flooper", app.name);
        assertEquals(Arrays.asList("dev", "stage"), app.profiles);
        assertEquals(json, app.format);
        assertEquals("v1.0", app.label);
        System.out.println(app);
      });

      it("Parses valid edge case with many profiles", () -> {
        var app = OtSpringApp.parse("/spring/flooper/dev,stage,qa,prod.yml");
        assertEquals("flooper", app.name);
        assertEquals(Arrays.asList("dev", "stage", "qa", "prod"), app.profiles);
        assertEquals(yaml, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Parses valid edge case with uppercase profile", () -> {
        var app = OtSpringApp.parse("/spring/flooper/DEV");
        assertEquals("flooper", app.name);
        assertEquals(Collections.singletonList("DEV"), app.profiles);
        assertEquals(json, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Parses valid edge case with profile named application", () -> {
        var app = OtSpringApp.parse("/spring/flooper/dev,stage,application");
        assertEquals("flooper", app.name);
        assertEquals(Arrays.asList("dev", "stage", "application"), app.profiles);
        assertEquals(json, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Throws on invalid path: missing segments", () -> {
        try {
          OtSpringApp.parse("/spring/");
          fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
          assertTrue(e.getMessage().contains("valid segments"));
        }
      });

      it("Throws on invalid path: missing profile", () -> {
        try {
          OtSpringApp.parse("/spring/flooper/");
          fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
          assertTrue(e.getMessage().contains("trailing"));
        }
      });

      it("Throws on invalid path: empty application", () -> {
        try {
          OtSpringApp.parse("/spring//dev");
          fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
          assertTrue(e.getMessage().contains("empty"));
        }
      });

      it("Throws on invalid path: no profile", () -> {
        try {
          OtSpringApp.parse("/spring/flooper");
          fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
          assertTrue(e.getMessage().contains("expected"));
        }
      });

      it("Throws on invalid path: invalid format suffix", () -> {
        try {
          OtSpringApp.parse("/spring/flooper/dev,stage.invalid");
          fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
          assertTrue(e.getMessage().contains("format"));
        }
      });

      it("Throws on invalid path: trailing slash", () -> {
        try {
          OtSpringApp.parse("/spring/flooper/dev/");
          fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
          assertTrue(e.getMessage().contains("trailing"));
        }
      });

      it("Invalid path: format with label", () -> {
        var app = OtSpringApp.parse("/spring/flooper/dev.yml/label");
        assertEquals("flooper", app.name);
        assertEquals(List.of("dev"), app.profiles);
        assertEquals(yaml, app.format);
        assertNotNull(app.label);
        System.out.println(app);
      });

      it("Throws on invalid path: missing /spring prefix", () -> {
        try {
          OtSpringApp.parse("/flooper/dev");
          fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
          assertTrue(e.getMessage().contains("Must start with /spring/"));
        }
      });

      it("Throws on invalid path: empty path", () -> {
        try {
          OtSpringApp.parse("");
          fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
          assertTrue(e.getMessage().contains("Must start with /spring/"));
        }
      });

      it("Invalid path: empty profile in list", () -> {
        var app = OtSpringApp.parse("/spring/flooper/,stage");
        assertEquals("flooper", app.name);
        assertEquals(List.of("stage"), app.profiles);
        assertEquals(json, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Invalid path: empty profile in middle", () -> {
        var app = OtSpringApp.parse("/spring/flooper/dev,,qa");
        assertEquals("flooper", app.name);
        assertEquals(List.of("dev", "qa"), app.profiles);
        assertEquals(json, app.format);
        assertNull(app.label);
        System.out.println(app);
      });

      it("Invalid path: empty profile in hyphenated Properties", () -> {
        var app = OtSpringApp.parse("/spring/flooper-dev-.properties");
        assertEquals("flooper", app.name);
        assertEquals(List.of("dev"), app.profiles);
        assertEquals(json, app.format);
        assertNull(app.label);
        System.out.println(app);
      });
    });
  }
}