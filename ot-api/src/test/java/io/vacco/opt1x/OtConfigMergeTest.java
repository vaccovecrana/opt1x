package io.vacco.opt1x;

import com.google.gson.Gson;
import io.vacco.opt1x.dto.OtConfigOp;
import io.vacco.opt1x.impl.*;
import io.vacco.opt1x.schema.OtNamespace;
import io.vacco.opt1x.spring.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import java.io.InputStreamReader;
import java.util.*;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class OtConfigMergeTest {

  private static Gson g = new Gson();

  private static OtConfigOp load(String u) throws Exception {
    var cmd = (OtConfigOp) null;
    var url = Objects.requireNonNull(OtConfigMergeTest.class.getResource(u));
    try (var fr = new InputStreamReader(url.openStream())) {
      cmd = g.fromJson(fr, OtConfigOp.class);
    }
    return cmd;
  }

  private static void print(OtConfigOp op) {
    System.out.println("------------------------");
    var idx = new OtConfigOpIdx(op);
    for (var e : idx.cmdIdx.entrySet()) {
      System.out.println(e);
    }
  }

  private static void printYaml(OtConfigOp op) {
    System.out.println("========================");
    System.out.println(OtYaml.toYaml(OtRender.toMap(op.vars)));
  }

  static {
    it("Merges config trees", () -> {
      var base = load("/myapp-base.json");
      var dev  = load("/myapp-dev.json");
      var prod = load("/myapp-prod.json");
      var confs = new OtConfigOp[] { base, dev, prod };
      for (var cmd : confs) {
        print(cmd);
      }
      printYaml(base);
      var devMerged = OtConfigOpMerge.merge(List.of(base, dev));
      print(devMerged);
      printYaml(devMerged);
      var prodMerged = OtConfigOpMerge.merge(List.of(base, prod));
      print(prodMerged);
      printYaml(prodMerged);
    });
    it("Creates JSON Spring envelopes", () -> {
      var base = load("/myapp-base.json");
      var prod = load("/myapp-prod.json");
      var app = OtSpringApp.parse("/spring/flooper/base,prod");
      var ns = OtNamespace.namespace(null, "flooper");
      ns.path = "/o1x/flooper";
      var env = new OtSpringEnvelope(app, ns, List.of(base, prod));
      env.populate();
      System.out.println(g.toJson(app));
    });
  }
}
