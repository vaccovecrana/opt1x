package io.vacco.opt1x;

import com.google.gson.GsonBuilder;
import io.vacco.metolithe.changeset.*;
import io.vacco.metolithe.dao.MtDaoMapper;
import io.vacco.opt1x.schema.OtSchema;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import java.io.*;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class OtSchemaTest {
  static {
    it("Generates schema classes", () -> {
      var g = new GsonBuilder().setPrettyPrinting().create();
      var changeSet = new MtChangeSet().withChanges(
        new MtLogMapper(null).process(
          new MtMapper().build(OtSchema.Fmt, OtSchema.schema), MtLevel.TABLE_COMPACT
        )
      );
      try (var fw = new FileWriter("./src/main/resources/ot-schema.json")) {
        g.toJson(changeSet, fw);
      }
    });
    it("Generates schema DAOs", () -> {
      var daoDir = new File("./src/main/java");
      var pkg = "io.vacco.opt1x.dao";
      new MtDaoMapper().mapSchema(daoDir, pkg, OtSchema.Fmt, OtSchema.schema);
    });
  }
}
