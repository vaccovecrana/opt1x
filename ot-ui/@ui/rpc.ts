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

export interface OtApiKey {
  kid?: number;
  pKid?: number;
  name?: string;
  path?: string;
  hash?: string;
  role?: OtRole;
  metadata0?: string;
  metadata1?: string;
  metadata2?: string;
  metadata3?: string;
  createdAtUtcMs?: number;
  deletedAtUtcMs?: number;
}

export interface OtApiKeyOp extends OtResult {
  parentKid?: number;
  name?: string;
  role?: OtRole;
  key?: OtApiKey;
  raw?: string;
}

export interface OtAssignmentList extends OtList<OtKeyNamespace, number> {
  apiKeys?: OtApiKey[];
}

export interface OtConfig {
  cid?: number;
  nsId?: number;
  name?: string;
  createdAtUtcMs?: number;
}

export interface OtConfigOp extends OtResult {
  cfg?: OtConfig;
  encrypted?: boolean;
  vars?: OtVar[];
  key?: OtApiKey;
}

export interface OtInitOp extends OtResult {
  rootApiKey?: string;
  shares?: string[];
}

export interface OtKeyNamespace {
  id?: number;
  kid?: number;
  nsId?: number;
  grantKid?: number;
  writeAccess?: boolean;
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
  createdAtUtcMs?: number;
}

export interface OtNamespaceOp extends OtResult {
  namespace?: OtNamespace;
  keyNamespace?: OtKeyNamespace;
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

export const enum OtRole {
  Application = "Application",
  Auditor = "Auditor",
  Admin = "Admin",
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
  createdAtUtcMs?: number;
  deletedAtUtcMs?: number;
}

export interface OtValueOp extends OtResult {
  key?: OtApiKey;
  val?: OtValue;
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

export const apiV1NamespaceGet = (pageSize: number, next: string): Promise<OtList<OtNamespace, string>> => {
  let path = "/api/v1/namespace"
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

export const apiV1NamespaceKeyGet = (pageSize: number, nsId: number, next: number): Promise<OtAssignmentList> => {
  let path = "/api/v1/namespace/key"
  const qParams = new URLSearchParams()
  if (pageSize) {
    qParams.append("pageSize", pageSize.toString())
  }
  if (nsId) {
    qParams.append("nsId", nsId.toString())
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

export const apiV1NamespaceIdGet = (nsId: number): Promise<OtList<OtNamespace, string>> => {
  let path = "/api/v1/namespace/{nsId}"
  path = path.replace("{ nsId }".replace(/\s+/g, ""), nsId.toString())
  return doJsonIo(path, "GET",
      undefined
    ,
    new Map(),
    undefined
  )
}

export const apiV1NamespaceIdConfigGet = (nsId: number, pageSize: number, next: string): Promise<OtList<OtConfig, string>> => {
  let path = "/api/v1/namespace/{nsId}/config"
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

export const apiV1NamespaceIdConfigIdGet = (nsId: number, cid: number, encrypted: boolean): Promise<OtConfigOp> => {
  let path = "/api/v1/namespace/{nsId}/config/{cid}"
  path = path.replace("{ nsId }".replace(/\s+/g, ""), nsId.toString())
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

export const apiV1NamespaceIdConfigIdFmtGet = (nsId: number, cid: number, fmt: string, encrypted: boolean): Promise<Object> => {
  let path = "/api/v1/namespace/{nsId}/config/{cid}/{fmt}"
  path = path.replace("{ nsId }".replace(/\s+/g, ""), nsId.toString())
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

export const apiV1NamespaceIdValueGet = (nsId: number, pageSize: number, next: string): Promise<OtList<OtValue, string>> => {
  let path = "/api/v1/namespace/{nsId}/value"
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

export const apiV1ValueGet = (): Promise<OtValueOp> => {
  let path = "/api/v1/value"
  return doJsonIo(path, "GET",
      undefined
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

export const apiV1NamespacePost = (arg1: OtNamespaceOp): Promise<OtNamespaceOp> => {
  let path = "/api/v1/namespace"
  return doJsonIo(path, "POST",
      JSON.stringify(arg1)
    ,
    new Map(),
    undefined
  )
}

export const apiV1NamespaceKeyPost = (arg1: OtNamespaceOp): Promise<OtNamespaceOp> => {
  let path = "/api/v1/namespace/key"
  return doJsonIo(path, "POST",
      JSON.stringify(arg1)
    ,
    new Map(),
    undefined
  )
}

export const apiV1NamespaceIdConfigPost = (nsId: number, arg2: OtConfigOp): Promise<OtConfigOp> => {
  let path = "/api/v1/namespace/{nsId}/config"
  path = path.replace("{ nsId }".replace(/\s+/g, ""), nsId.toString())
  return doJsonIo(path, "POST",
      JSON.stringify(arg2)
    ,
    new Map(),
    undefined
  )
}

export const apiV1NamespaceIdConfigIdNodePost = (nsId: number, cid: number, arg3: OtConfigOp): Promise<OtConfigOp> => {
  let path = "/api/v1/namespace/{nsId}/config/{cid}/node"
  path = path.replace("{ nsId }".replace(/\s+/g, ""), nsId.toString())
  path = path.replace("{ cid }".replace(/\s+/g, ""), cid.toString())
  return doJsonIo(path, "POST",
      JSON.stringify(arg3)
    ,
    new Map(),
    undefined
  )
}

export const apiV1NamespaceIdValuePost = (nsId: number, arg2: OtValueOp): Promise<OtValueOp> => {
  let path = "/api/v1/namespace/{nsId}/value"
  path = path.replace("{ nsId }".replace(/\s+/g, ""), nsId.toString())
  return doJsonIo(path, "POST",
      JSON.stringify(arg2)
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