/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

/*
 * This file is generated by jOOQ.
 */
package io.harness.timescaledb.tables.pojos;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class WorkloadInfo implements Serializable {
  private static final long serialVersionUID = 1L;

  private String accountid;
  private String clusterid;
  private String workloadid;
  private String namespace;
  private String name;
  private Integer replicas;
  private OffsetDateTime createdat;
  private OffsetDateTime updatedat;
  private String type;

  public WorkloadInfo() {}

  public WorkloadInfo(WorkloadInfo value) {
    this.accountid = value.accountid;
    this.clusterid = value.clusterid;
    this.workloadid = value.workloadid;
    this.namespace = value.namespace;
    this.name = value.name;
    this.replicas = value.replicas;
    this.createdat = value.createdat;
    this.updatedat = value.updatedat;
    this.type = value.type;
  }

  public WorkloadInfo(String accountid, String clusterid, String workloadid, String namespace, String name,
      Integer replicas, OffsetDateTime createdat, OffsetDateTime updatedat, String type) {
    this.accountid = accountid;
    this.clusterid = clusterid;
    this.workloadid = workloadid;
    this.namespace = namespace;
    this.name = name;
    this.replicas = replicas;
    this.createdat = createdat;
    this.updatedat = updatedat;
    this.type = type;
  }

  /**
   * Getter for <code>public.workload_info.accountid</code>.
   */
  public String getAccountid() {
    return this.accountid;
  }

  /**
   * Setter for <code>public.workload_info.accountid</code>.
   */
  public WorkloadInfo setAccountid(String accountid) {
    this.accountid = accountid;
    return this;
  }

  /**
   * Getter for <code>public.workload_info.clusterid</code>.
   */
  public String getClusterid() {
    return this.clusterid;
  }

  /**
   * Setter for <code>public.workload_info.clusterid</code>.
   */
  public WorkloadInfo setClusterid(String clusterid) {
    this.clusterid = clusterid;
    return this;
  }

  /**
   * Getter for <code>public.workload_info.workloadid</code>.
   */
  public String getWorkloadid() {
    return this.workloadid;
  }

  /**
   * Setter for <code>public.workload_info.workloadid</code>.
   */
  public WorkloadInfo setWorkloadid(String workloadid) {
    this.workloadid = workloadid;
    return this;
  }

  /**
   * Getter for <code>public.workload_info.namespace</code>.
   */
  public String getNamespace() {
    return this.namespace;
  }

  /**
   * Setter for <code>public.workload_info.namespace</code>.
   */
  public WorkloadInfo setNamespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  /**
   * Getter for <code>public.workload_info.name</code>.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Setter for <code>public.workload_info.name</code>.
   */
  public WorkloadInfo setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Getter for <code>public.workload_info.replicas</code>.
   */
  public Integer getReplicas() {
    return this.replicas;
  }

  /**
   * Setter for <code>public.workload_info.replicas</code>.
   */
  public WorkloadInfo setReplicas(Integer replicas) {
    this.replicas = replicas;
    return this;
  }

  /**
   * Getter for <code>public.workload_info.createdat</code>.
   */
  public OffsetDateTime getCreatedat() {
    return this.createdat;
  }

  /**
   * Setter for <code>public.workload_info.createdat</code>.
   */
  public WorkloadInfo setCreatedat(OffsetDateTime createdat) {
    this.createdat = createdat;
    return this;
  }

  /**
   * Getter for <code>public.workload_info.updatedat</code>.
   */
  public OffsetDateTime getUpdatedat() {
    return this.updatedat;
  }

  /**
   * Setter for <code>public.workload_info.updatedat</code>.
   */
  public WorkloadInfo setUpdatedat(OffsetDateTime updatedat) {
    this.updatedat = updatedat;
    return this;
  }

  /**
   * Getter for <code>public.workload_info.type</code>.
   */
  public String getType() {
    return this.type;
  }

  /**
   * Setter for <code>public.workload_info.type</code>.
   */
  public WorkloadInfo setType(String type) {
    this.type = type;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final WorkloadInfo other = (WorkloadInfo) obj;
    if (accountid == null) {
      if (other.accountid != null)
        return false;
    } else if (!accountid.equals(other.accountid))
      return false;
    if (clusterid == null) {
      if (other.clusterid != null)
        return false;
    } else if (!clusterid.equals(other.clusterid))
      return false;
    if (workloadid == null) {
      if (other.workloadid != null)
        return false;
    } else if (!workloadid.equals(other.workloadid))
      return false;
    if (namespace == null) {
      if (other.namespace != null)
        return false;
    } else if (!namespace.equals(other.namespace))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (replicas == null) {
      if (other.replicas != null)
        return false;
    } else if (!replicas.equals(other.replicas))
      return false;
    if (createdat == null) {
      if (other.createdat != null)
        return false;
    } else if (!createdat.equals(other.createdat))
      return false;
    if (updatedat == null) {
      if (other.updatedat != null)
        return false;
    } else if (!updatedat.equals(other.updatedat))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.accountid == null) ? 0 : this.accountid.hashCode());
    result = prime * result + ((this.clusterid == null) ? 0 : this.clusterid.hashCode());
    result = prime * result + ((this.workloadid == null) ? 0 : this.workloadid.hashCode());
    result = prime * result + ((this.namespace == null) ? 0 : this.namespace.hashCode());
    result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
    result = prime * result + ((this.replicas == null) ? 0 : this.replicas.hashCode());
    result = prime * result + ((this.createdat == null) ? 0 : this.createdat.hashCode());
    result = prime * result + ((this.updatedat == null) ? 0 : this.updatedat.hashCode());
    result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("WorkloadInfo (");

    sb.append(accountid);
    sb.append(", ").append(clusterid);
    sb.append(", ").append(workloadid);
    sb.append(", ").append(namespace);
    sb.append(", ").append(name);
    sb.append(", ").append(replicas);
    sb.append(", ").append(createdat);
    sb.append(", ").append(updatedat);
    sb.append(", ").append(type);

    sb.append(")");
    return sb.toString();
  }
}
