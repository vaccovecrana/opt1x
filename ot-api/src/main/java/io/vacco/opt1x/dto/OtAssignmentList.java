package io.vacco.opt1x.dto;

import io.vacco.opt1x.schema.OtApiKey;
import io.vacco.opt1x.schema.OtKeyNamespace;
import java.util.List;

public class OtAssignmentList extends OtList<OtKeyNamespace, Long> {

  public List<OtApiKey> apiKeys;

}
