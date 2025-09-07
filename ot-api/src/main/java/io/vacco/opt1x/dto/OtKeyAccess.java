package io.vacco.opt1x.dto;

import io.vacco.opt1x.schema.*;
import java.util.List;

public class OtKeyAccess {

  public OtGroup           group;                   // a target group
  public List<OtGroup>     groups;                  // child groups of `group`, or groups of each group/namespace binding
  public List<OtGroup>     groupTree;               // expanded child groups of each group/namespace binding

  public List<OtApiKey>    keys;                    // keys available for key/group bindings
  public List<OtKeyGroup>  keyGroups;               // key/group bindings of a target api key
  public List<OtApiKey>    keyGroupGrantKeys;       // which api keys granted each key/group binding

  public OtNamespace       namespace;               // a target namespace
  public List<OtNamespace> namespaces;              // child namespaces of `namespace`, or namespaces of each group/namespace binding
  public List<OtGroupNs>   groupNamespaces;         // group/namespace bindings in the target namespace
  public List<OtApiKey>    groupNamespaceGrantKeys; // which api keys granted each group/namespace binding

}
