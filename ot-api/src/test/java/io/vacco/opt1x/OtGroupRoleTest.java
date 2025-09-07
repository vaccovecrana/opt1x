package io.vacco.opt1x;

import io.vacco.opt1x.schema.OtGroupRole;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class OtGroupRoleTest {
  static {
    it("Check group role inclusions", () -> {
      assertTrue(OtGroupRole.Member.includes(OtGroupRole.Member));
      assertFalse(OtGroupRole.Member.includes(OtGroupRole.Admin));
      assertTrue(OtGroupRole.Admin.includes(OtGroupRole.Member));
      assertTrue(OtGroupRole.Admin.includes(OtGroupRole.Admin));
    });
  }
}
