package io.vacco.opt1x;

import com.google.gson.GsonBuilder;
import io.vacco.opt1x.dto.OtConfigOp;
import io.vacco.opt1x.impl.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import java.io.InputStreamReader;
import java.util.Objects;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class OtRenderTest {
  static {
    OtOptions.logLevel = OtOptions.LogLevel.trace;
    OtOptions.setFrom(new String[] {});

    it("Creates an intermediate map representation", () -> {
      var g = new GsonBuilder().setPrettyPrinting().create();
      var treeUrl = Objects.requireNonNull(OtRenderTest.class.getResource("/tree.json"));
      var op = (OtConfigOp) null;
      try (var ir = new InputStreamReader(treeUrl.openStream())) {
        op = g.fromJson(ir, OtConfigOp.class);
      }

      var map = OtRender.toMap(op.vars);
      OtOptions.log.info(g.toJson(map));
      OtOptions.log.info("=====================");
      OtOptions.log.info(OtToml.toToml(map));
      OtOptions.log.info("=====================");
      OtOptions.log.info(OtYaml.toYaml(map));
      OtOptions.log.info("=====================");
      OtOptions.log.info(OtProperties.toProperties(map));
    });
  }
}
