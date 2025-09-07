/* ========================================================= */
/* ======== Generated file - do not modify directly ======== */
/* ========================================================= */

const doJsonIo = <I, O>(url: string, method: string, body: I,
                        headers: Map<string, string>, mediaType?: string): Promise<O> => {
  const options: any = {method, headers: {}}
  if (mediaType) {
    options.headers["Content-Type"] = mediaType
  }
  if (body) {
    options.body = body
  }
  headers.forEach((v, k) => options.headers[k] = v)
  return fetch(url, options)
    .then(response => Promise
      .resolve(response.json() as O)
      .catch(cause => Promise.reject({ response, cause }))
    )
}

/* ====================================== */
/* ============= RPC types ============== */
/* ====================================== */

export interface MtPage1<T, K1> {
  size?: number;
  items?: T[];
  nx1?: K1;
}

export interface OtAdminOp extends OtResult {
  key?: OtApiKey;
  keyGroup?: OtKeyGroup;
  keyGroupBind?: OtKeyGroup;
  ns?: OtNamespace;
  group?: OtGroup;
  groupNs?: OtGroupNs;
}

export interface OtApiKey {
  kid?: number;
  pKid?: number;
  name?: string;
  path?: string;
  hash?: string;
  leaf?: boolean;
  createUtcMs?: number;
  accessUtcMs?: number;
  metadata0?: string;
  metadata1?: string;
  metadata2?: string;
  metadata3?: string;
  oidcSub?: string;
  oidcEmail?: string;
}

export interface OtApiKeyOp extends OtResult {
  key?: OtApiKey;
  raw?: string;
}

export interface OtConfig {
  cid?: number;
  nsId?: number;
  name?: string;
  createUtcMs?: number;
}

export interface OtConfigOp extends OtResult {
  cfg?: OtConfig;
  encrypted?: boolean;
  vars?: OtVar[];
  key?: OtApiKey;
}

export interface OtGroup {
  gid?: number;
  pGid?: number;
  name?: string;
  path?: string;
  createUtcMs?: number;
}

export interface OtGroupNs {
  id?: number;
  gid?: number;
  nsId?: number;
  grantKid?: number;
  grantUtcMs?: number;
  read?: boolean;
  write?: boolean;
  manage?: boolean;
}

export const enum OtGroupRole {
  Member = "Member",
  Admin = "Admin",
}

export interface OtInitOp extends OtResult {
  rootApiKey?: string;
  shares?: string[];
}

export interface OtKeyAccess {
  group?: OtGroup;
  groups?: OtGroup[];
  groupTree?: OtGroup[];
  keys?: OtApiKey[];
  keyGroups?: OtKeyGroup[];
  keyGroupGrantKeys?: OtApiKey[];
  namespace?: OtNamespace;
  namespaces?: OtNamespace[];
  groupNamespaces?: OtGroupNs[];
  groupNamespaceGrantKeys?: OtApiKey[];
}

export interface OtKeyGroup {
  id?: number;
  kid?: number;
  gid?: number;
  role?: OtGroupRole;
  grantKid?: number;
  grantUtcMs?: number;
}

export interface OtList<T, N> extends OtResult {
  page?: MtPage1<T, N>;
}

export interface OtNamespace {
  nsId?: number;
  pNsId?: number;
  name?: string;
  path?: string;
  createUtcMs?: number;
}

export interface OtNode {
  nid?: number;
  cid?: number;
  label?: string;
  type?: OtNodeType;
  vid?: number;
  pNid?: number;
  itemIdx?: number;
}

export const enum OtNodeType {
  Object = "Object",
  Array = "Array",
  Value = "Value",
}

export interface OtResult {
  error?: string;
  validations?: OtValidation[];
}

export interface OtUnsealOp extends OtResult {
  loadedKeys?: number;
  ready?: boolean;
}

export interface OtValidation {
  name?: string;
  message?: string;
  key?: string;
  format?: string;
}

export interface OtValue {
  vid?: number;
  nsId?: number;
  name?: string;
  value?: string;
  type?: OtValueType;
  notes?: string;
  encrypted?: boolean;
  createUtcMs?: number;
}

export interface OtValueOp extends OtResult {
  key?: OtApiKey;
  val?: OtValue;
  valPage?: MtPage1<OtValue, string>;
  namespace?: OtNamespace;
  values?: OtValue[];
  namespaces?: OtNamespace[];
}

export const enum OtValueType {
  Number = "Number",
  String = "String",
  Boolean = "Boolean",
}

export interface OtVar {
  node?: OtNode;
  val?: OtValue;
}


/* ====================================== */
/* ============ RPC methods ============= */
/* ====================================== */

/*
Source controllers:

- io.vacco.opt1x.web.OtApiHdl

 */

export const apiV1GroupIdDelete = (gid: number): Promise<OtAdminOp> => {
  let path = "/api/v1/group/{gid}"
  path = path.replace("{ gid }".replace(/\s+/g, ""), gid.toString())
  return doJsonIo(path, "DELETE",
      undefined
    ,
    new Map(),
    undefined
  )
}

export const apiV1ConfigGet = (nsId: number, pageSize: number, next: string): Promise<OtList<OtConfig, string>> => {
  let path = "/api/v1/config"
  const qParams = new URLSearchParams()
  if (nsId) {
    qParams.append("nsId", nsId.toString())
  }
  if (pageSize) {
    qParams.append("pageSize", pageSize.toString())
  }
  if (next) {
    qParams.append("next", next.toString())
  }
  path = `${path}?${qParams.toString()}`
  return doJsonIo(path, "GET",
      undefined
    ,
    new Map(),
    undefined
  )
}

export const apiV1ConfigCidGet = (cid: number, encrypted: boolean): Promise<OtConfigOp> => {
  let path = "/api/v1/config/{cid}"
  path = path.replace("{ cid }".replace(/\s+/g, ""), cid.toString())
  const qParams = new URLSearchParams()
  if (encrypted) {
    qParams.append("encrypted", encrypted.toString())
  }
  path = `${path}?${qParams.toString()}`
  return doJsonIo(path, "GET",
      undefined
    ,
    new Map(),
    undefined
  )
}

export const apiV1ConfigIdFmtGet = (cid: number, fmt: string, encrypted: boolean): Promise<Object> => {
  let path = "/api/v1/config/{cid}/{fmt}"
  path = path.replace("{ cid }".replace(/\s+/g, ""), cid.toString())
  path = path.replace("{ fmt }".replace(/\s+/g, ""), fmt.toString())
  const qParams = new URLSearchParams()
  if (encrypted) {
    qParams.append("encrypted", encrypted.toString())
  }
  path = `${path}?${qParams.toString()}`
  return doJsonIo(path, "GET",
      undefined
    ,
    new Map(),
    undefined
  )
}

export const apiV1GroupGet = (): Promise<OtKeyAccess> => {
  let path = "/api/v1/group"
  return doJsonIo(path, "GET",
      undefined
    ,
    new Map(),
    undefined
  )
}

export const apiV1GroupIdGet = (gid: number): Promise<OtKeyAccess> => {
  let path = "/api/v1/group/{gid}"
  path = path.replace("{ gid }".replace(/\s+/g, ""), gid.toString())
  return doJsonIo(path, "GET",
      undefined
    ,
    new Map(),
    undefined
  )
}

export const apiV1InitGet = (): Promise<OtInitOp> => {
  let path = "/api/v1/init"
  return doJsonIo(path, "GET",
      undefined
    ,
    new Map(),
    undefined
  )
}

export const apiV1KeyGet = (pageSize: number, next: string): Promise<OtList<OtApiKey, string>> => {
  let path = "/api/v1/key"
  const qParams = new URLSearchParams()
  if (pageSize) {
    qParams.append("pageSize", pageSize.toString())
  }
  if (next) {
    qParams.append("next", next.toString())
  }
  path = `${path}?${qParams.toString()}`
  return doJsonIo(path, "GET",
      undefined
    ,
    new Map(),
    undefined
  )
}

export const apiV1NamespaceGet = (): Promise<OtKeyAccess> => {
  let path = "/api/v1/namespace"
  return doJsonIo(path, "GET",
      undefined
    ,
    new Map(),
    undefined
  )
}

export const apiV1NamespaceIdGet = (nsId: number): Promise<OtKeyAccess> => {
  let path = "/api/v1/namespace/{nsId}"
  path = path.replace("{ nsId }".replace(/\s+/g, ""), nsId.toString())
  return doJsonIo(path, "GET",
      undefined
    ,
    new Map(),
    undefined
  )
}

export const apiV1ValueGet = (): Promise<OtValueOp> => {
  let path = "/api/v1/value"
  return doJsonIo(path, "GET",
      undefined
    ,
    new Map(),
    undefined
  )
}

export const apiV1ValueNsIdGet = (nsId: number, pageSize: number, next: string): Promise<OtValueOp> => {
  let path = "/api/v1/value/{nsId}"
  path = path.replace("{ nsId }".replace(/\s+/g, ""), nsId.toString())
  const qParams = new URLSearchParams()
  if (pageSize) {
    qParams.append("pageSize", pageSize.toString())
  }
  if (next) {
    qParams.append("next", next.toString())
  }
  path = `${path}?${qParams.toString()}`
  return doJsonIo(path, "GET",
      undefined
    ,
    new Map(),
    undefined
  )
}

export const apiV1ConfigPost = (nsId: number, arg2: OtConfigOp): Promise<OtConfigOp> => {
  let path = "/api/v1/config"
  const qParams = new URLSearchParams()
  if (nsId) {
    qParams.append("nsId", nsId.toString())
  }
  path = `${path}?${qParams.toString()}`
  return doJsonIo(path, "POST",
      JSON.stringify(arg2)
    ,
    new Map(),
    undefined
  )
}

export const apiV1ConfigCidPost = (cid: number, arg2: OtConfigOp): Promise<OtConfigOp> => {
  let path = "/api/v1/config/{cid}"
  path = path.replace("{ cid }".replace(/\s+/g, ""), cid.toString())
  return doJsonIo(path, "POST",
      JSON.stringify(arg2)
    ,
    new Map(),
    undefined
  )
}

export const apiV1GroupPost = (arg1: OtAdminOp): Promise<OtAdminOp> => {
  let path = "/api/v1/group"
  return doJsonIo(path, "POST",
      JSON.stringify(arg1)
    ,
    new Map(),
    undefined
  )
}

export const apiV1KeyPost = (arg1: OtApiKeyOp): Promise<OtApiKeyOp> => {
  let path = "/api/v1/key"
  return doJsonIo(path, "POST",
      JSON.stringify(arg1)
    ,
    new Map(),
    undefined
  )
}

export const apiV1NamespacePost = (arg1: OtAdminOp): Promise<OtAdminOp> => {
  let path = "/api/v1/namespace"
  return doJsonIo(path, "POST",
      JSON.stringify(arg1)
    ,
    new Map(),
    undefined
  )
}

export const apiV1UnsealPost = (arg0: string): Promise<OtUnsealOp> => {
  let path = "/api/v1/unseal"
  return doJsonIo(path, "POST",
      JSON.stringify(arg0)
    ,
    new Map(),
    undefined
  )
}

export const apiV1ValueNsIdPost = (nsId: number, arg2: OtValueOp): Promise<OtValueOp> => {
  let path = "/api/v1/value/{nsId}"
  path = path.replace("{ nsId }".replace(/\s+/g, ""), nsId.toString())
  return doJsonIo(path, "POST",
      JSON.stringify(arg2)
    ,
    new Map(),
    undefined
  )
}