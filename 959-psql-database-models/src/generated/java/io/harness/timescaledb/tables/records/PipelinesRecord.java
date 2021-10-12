/*
 * This file is generated by jOOQ.
 */
package io.harness.timescaledb.tables.records;

import io.harness.timescaledb.tables.Pipelines;

import org.jooq.Field;
import org.jooq.Record9;
import org.jooq.Row9;
import org.jooq.impl.TableRecordImpl;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class PipelinesRecord extends TableRecordImpl<PipelinesRecord>
    implements Record9<String, String, String, String, String, String, Boolean, Long, Long> {
  private static final long serialVersionUID = 1L;

  /**
   * Setter for <code>public.pipelines.id</code>.
   */
  public PipelinesRecord setId(String value) {
    set(0, value);
    return this;
  }

  /**
   * Getter for <code>public.pipelines.id</code>.
   */
  public String getId() {
    return (String) get(0);
  }

  /**
   * Setter for <code>public.pipelines.account_id</code>.
   */
  public PipelinesRecord setAccountId(String value) {
    set(1, value);
    return this;
  }

  /**
   * Getter for <code>public.pipelines.account_id</code>.
   */
  public String getAccountId() {
    return (String) get(1);
  }

  /**
   * Setter for <code>public.pipelines.org_identifier</code>.
   */
  public PipelinesRecord setOrgIdentifier(String value) {
    set(2, value);
    return this;
  }

  /**
   * Getter for <code>public.pipelines.org_identifier</code>.
   */
  public String getOrgIdentifier() {
    return (String) get(2);
  }

  /**
   * Setter for <code>public.pipelines.project_identifier</code>.
   */
  public PipelinesRecord setProjectIdentifier(String value) {
    set(3, value);
    return this;
  }

  /**
   * Getter for <code>public.pipelines.project_identifier</code>.
   */
  public String getProjectIdentifier() {
    return (String) get(3);
  }

  /**
   * Setter for <code>public.pipelines.identifier</code>.
   */
  public PipelinesRecord setIdentifier(String value) {
    set(4, value);
    return this;
  }

  /**
   * Getter for <code>public.pipelines.identifier</code>.
   */
  public String getIdentifier() {
    return (String) get(4);
  }

  /**
   * Setter for <code>public.pipelines.name</code>.
   */
  public PipelinesRecord setName(String value) {
    set(5, value);
    return this;
  }

  /**
   * Getter for <code>public.pipelines.name</code>.
   */
  public String getName() {
    return (String) get(5);
  }

  /**
   * Setter for <code>public.pipelines.deleted</code>.
   */
  public PipelinesRecord setDeleted(Boolean value) {
    set(6, value);
    return this;
  }

  /**
   * Getter for <code>public.pipelines.deleted</code>.
   */
  public Boolean getDeleted() {
    return (Boolean) get(6);
  }

  /**
   * Setter for <code>public.pipelines.created_at</code>.
   */
  public PipelinesRecord setCreatedAt(Long value) {
    set(7, value);
    return this;
  }

  /**
   * Getter for <code>public.pipelines.created_at</code>.
   */
  public Long getCreatedAt() {
    return (Long) get(7);
  }

  /**
   * Setter for <code>public.pipelines.last_updated_at</code>.
   */
  public PipelinesRecord setLastUpdatedAt(Long value) {
    set(8, value);
    return this;
  }

  /**
   * Getter for <code>public.pipelines.last_updated_at</code>.
   */
  public Long getLastUpdatedAt() {
    return (Long) get(8);
  }

  // -------------------------------------------------------------------------
  // Record9 type implementation
  // -------------------------------------------------------------------------

  @Override
  public Row9<String, String, String, String, String, String, Boolean, Long, Long> fieldsRow() {
    return (Row9) super.fieldsRow();
  }

  @Override
  public Row9<String, String, String, String, String, String, Boolean, Long, Long> valuesRow() {
    return (Row9) super.valuesRow();
  }

  @Override
  public Field<String> field1() {
    return Pipelines.PIPELINES.ID;
  }

  @Override
  public Field<String> field2() {
    return Pipelines.PIPELINES.ACCOUNT_ID;
  }

  @Override
  public Field<String> field3() {
    return Pipelines.PIPELINES.ORG_IDENTIFIER;
  }

  @Override
  public Field<String> field4() {
    return Pipelines.PIPELINES.PROJECT_IDENTIFIER;
  }

  @Override
  public Field<String> field5() {
    return Pipelines.PIPELINES.IDENTIFIER;
  }

  @Override
  public Field<String> field6() {
    return Pipelines.PIPELINES.NAME;
  }

  @Override
  public Field<Boolean> field7() {
    return Pipelines.PIPELINES.DELETED;
  }

  @Override
  public Field<Long> field8() {
    return Pipelines.PIPELINES.CREATED_AT;
  }

  @Override
  public Field<Long> field9() {
    return Pipelines.PIPELINES.LAST_UPDATED_AT;
  }

  @Override
  public String component1() {
    return getId();
  }

  @Override
  public String component2() {
    return getAccountId();
  }

  @Override
  public String component3() {
    return getOrgIdentifier();
  }

  @Override
  public String component4() {
    return getProjectIdentifier();
  }

  @Override
  public String component5() {
    return getIdentifier();
  }

  @Override
  public String component6() {
    return getName();
  }

  @Override
  public Boolean component7() {
    return getDeleted();
  }

  @Override
  public Long component8() {
    return getCreatedAt();
  }

  @Override
  public Long component9() {
    return getLastUpdatedAt();
  }

  @Override
  public String value1() {
    return getId();
  }

  @Override
  public String value2() {
    return getAccountId();
  }

  @Override
  public String value3() {
    return getOrgIdentifier();
  }

  @Override
  public String value4() {
    return getProjectIdentifier();
  }

  @Override
  public String value5() {
    return getIdentifier();
  }

  @Override
  public String value6() {
    return getName();
  }

  @Override
  public Boolean value7() {
    return getDeleted();
  }

  @Override
  public Long value8() {
    return getCreatedAt();
  }

  @Override
  public Long value9() {
    return getLastUpdatedAt();
  }

  @Override
  public PipelinesRecord value1(String value) {
    setId(value);
    return this;
  }

  @Override
  public PipelinesRecord value2(String value) {
    setAccountId(value);
    return this;
  }

  @Override
  public PipelinesRecord value3(String value) {
    setOrgIdentifier(value);
    return this;
  }

  @Override
  public PipelinesRecord value4(String value) {
    setProjectIdentifier(value);
    return this;
  }

  @Override
  public PipelinesRecord value5(String value) {
    setIdentifier(value);
    return this;
  }

  @Override
  public PipelinesRecord value6(String value) {
    setName(value);
    return this;
  }

  @Override
  public PipelinesRecord value7(Boolean value) {
    setDeleted(value);
    return this;
  }

  @Override
  public PipelinesRecord value8(Long value) {
    setCreatedAt(value);
    return this;
  }

  @Override
  public PipelinesRecord value9(Long value) {
    setLastUpdatedAt(value);
    return this;
  }

  @Override
  public PipelinesRecord values(String value1, String value2, String value3, String value4, String value5,
      String value6, Boolean value7, Long value8, Long value9) {
    value1(value1);
    value2(value2);
    value3(value3);
    value4(value4);
    value5(value5);
    value6(value6);
    value7(value7);
    value8(value8);
    value9(value9);
    return this;
  }

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  /**
   * Create a detached PipelinesRecord
   */
  public PipelinesRecord() {
    super(Pipelines.PIPELINES);
  }

  /**
   * Create a detached, initialised PipelinesRecord
   */
  public PipelinesRecord(String id, String accountId, String orgIdentifier, String projectIdentifier, String identifier,
      String name, Boolean deleted, Long createdAt, Long lastUpdatedAt) {
    super(Pipelines.PIPELINES);

    setId(id);
    setAccountId(accountId);
    setOrgIdentifier(orgIdentifier);
    setProjectIdentifier(projectIdentifier);
    setIdentifier(identifier);
    setName(name);
    setDeleted(deleted);
    setCreatedAt(createdAt);
    setLastUpdatedAt(lastUpdatedAt);
  }
}